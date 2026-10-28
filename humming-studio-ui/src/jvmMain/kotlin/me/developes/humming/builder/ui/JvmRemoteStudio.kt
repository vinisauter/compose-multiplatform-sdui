package me.developes.humming.builder.ui

import kotlinx.coroutines.*
import kotlinx.serialization.json.*
import me.developes.humming.sdui.common.*
import me.developes.humming.sdui.studio.*
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.URI
import java.net.http.*
import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction
import java.nio.file.*
import java.nio.channels.FileChannel
import java.time.Duration
import java.util.concurrent.*
import java.util.concurrent.Flow
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Local desktop opt-in. This client owns its HTTP resources and never writes the bearer into recovery. */
class JvmRemoteStudioApi(origin: String, identity: RemoteDocument, private val token: String) : RemoteStudioApi, RemoteStudioCollaborationApi, AutoCloseable {
    private val client: HttpClient
    private val endpoint: URI
    private val json = Json { encodeDefaults = true; classDiscriminator = "operationType" }
    init {
        val uri = URI(origin)
        require(uri.scheme == "http" && uri.host in setOf("127.0.0.1", "localhost") && uri.port in 1..65535 &&
            uri.userInfo == null && uri.query == null && uri.fragment == null && uri.path in setOf("", "/"))
        require(token.length in 32..256 && token.all { it.code in 33..126 })
        require(listOf(identity.tenant, identity.document).all { Regex("[A-Za-z0-9][A-Za-z0-9_-]{0,127}").matches(it) })
        endpoint = URI(uri.scheme, null, uri.host, uri.port, "/v1/tenants/${identity.tenant}/documents/${identity.document}", null, null)
        client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).followRedirects(HttpClient.Redirect.NEVER).build()
    }
    private data class Response(val status: Int, val body: JsonObject?)
    private suspend fun request(command: EditCommand?): Response = requestAt(if (command == null) "" else "/commands",
        if (command == null) "GET" else "POST", command?.let { boundedCanonicalJson(json.encodeToJsonElement(EditCommand.serializer(), it), 2_000_000) })
    private suspend fun requestAt(suffix: String, method: String, source: String? = null): Response = try {
        val request = HttpRequest.newBuilder(URI(endpoint.toString() + suffix))
            .timeout(Duration.ofSeconds(10)).header("Authorization", "Bearer $token").header("Content-Type", "application/json")
        request.method(method, source?.let(HttpRequest.BodyPublishers::ofString) ?: HttpRequest.BodyPublishers.noBody())
        val response = client.sendAsync(request.build(), HttpResponse.BodyHandler { BoundedBody() }).await()
        val type = response.headers().firstValue("Content-Type").orElse("").substringBefore(';').trim().lowercase()
        val body = if (type == "application/json") try {
            val text = Charsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(response.body())).toString()
            parseBoundedJson(text, PayloadLimits(maxInputChars = 8_000_000, maxPropertyChars = 4_000_000, maxJsonValues = 150_000)) as? JsonObject
        } catch (_: PayloadException) { null } catch (_: java.nio.charset.CharacterCodingException) { null } else null
        Response(response.statusCode(), body)
    } catch (_: IOException) { Response(503, null) }
    private fun reason(response: Response): RemotePause = when (response.status) {
        401, 403 -> RemotePause.UNAUTHORIZED
        409 -> RemotePause.CONFLICT
        in 500..599 -> RemotePause.OFFLINE
        else -> RemotePause.REJECTED
    }
    override suspend fun read(): RemoteStudioRead {
        val response = request(null)
        val source = response.body?.get("document") as? JsonObject
        return if (response.status == 200 && source != null) RemoteStudioRead.Document(source.toString())
            else RemoteStudioRead.Failed(reason(response))
    }
    override suspend fun submit(command: EditCommand): RemoteStudioSubmit {
        return submitted(request(command))
    }
    private fun submitted(response: Response): RemoteStudioSubmit {
        val source = response.body?.get("document") as? JsonObject
        val applied = (response.body?.get("appliedRevision") as? JsonPrimitive)?.takeUnless { it.isString }?.longOrNull
        return if (response.status == 200 && source != null && applied != null) RemoteStudioSubmit.Acknowledged(source.toString(), applied)
            else RemoteStudioSubmit.Failed(reason(response))
    }
    override suspend fun updates(afterRevision: Long): DraftUpdateFeed? {
        require(afterRevision >= 0)
        val response = requestAt("/updates/$afterRevision", "GET")
        if (response.status != 200 || response.body == null) return null
        return DraftUpdateFeedCodec().decode(response.body.toString()).value
    }
    override suspend fun heartbeat(): DraftPresenceList? {
        val response = requestAt("/presence", "POST")
        if (response.status != 200 || response.body == null) return null
        val value = try { json.decodeFromJsonElement(DraftPresenceList.serializer(), response.body) }
        catch (_: kotlinx.serialization.SerializationException) { return null }
        return value.takeIf { it.members.size <= 64 && it.members.all { member -> member.actorId.isNotBlank() && member.actorId.length <= 128 && member.expiresAtMillis > 0 } }
    }
    override suspend fun leave() { requestAt("/presence/leave", "POST") }
    override suspend fun undo(command: RemoteUndoCommand): RemoteStudioSubmit = submitted(requestAt("/undo", "POST",
        boundedCanonicalJson(json.encodeToJsonElement(RemoteUndoCommand.serializer(), command), 4096)))
    override fun close() { client.shutdownNow(); check(client.awaitTermination(Duration.ofSeconds(5))) { "Remote Studio HTTP client did not stop" } }
    private class BoundedBody : HttpResponse.BodySubscriber<ByteArray> {
        private val body = CompletableFuture<ByteArray>()
        private val bytes = ByteArrayOutputStream()
        private var subscription: Flow.Subscription? = null
        override fun getBody(): CompletionStage<ByteArray> = body
        override fun onSubscribe(value: Flow.Subscription) { subscription = value; value.request(1) }
        override fun onNext(items: List<ByteBuffer>) {
            for (item in items) {
                if (item.remaining() > 16_000_000 - bytes.size()) { subscription?.cancel(); body.completeExceptionally(IOException("Remote body limit")); return }
                val chunk = ByteArray(item.remaining()); item.get(chunk); bytes.write(chunk)
            }
            subscription?.request(1)
        }
        override fun onError(failure: Throwable) { body.completeExceptionally(failure) }
        override fun onComplete() { body.complete(bytes.toByteArray()) }
    }
    private suspend fun <T> CompletableFuture<T>.await(): T = suspendCancellableCoroutine { continuation ->
        continuation.invokeOnCancellation { cancel(true) }
        whenComplete { value, error ->
            if (error == null) continuation.resume(value)
            else {
                var failure = error
                while ((failure is CompletionException || failure is ExecutionException) && failure.cause != null) failure = failure.cause!!
                continuation.resumeWithException(failure)
            }
        }
    }
}

/** File replacement is atomic on the selected filesystem; failures block the next network command. */
class JvmRemoteStudioRecovery(private val path: Path) : RemoteStudioRecovery {
    override suspend fun read(): RecoveryRead = withContext(Dispatchers.IO) {
        try {
            if (!Files.exists(path)) RecoveryRead.Saved(null)
            else Files.newInputStream(path).use { input ->
                val bytes = input.readNBytes(32_000_001)
                if (bytes.size > 32_000_000) RecoveryRead.Failed
                else try { RecoveryRead.Saved(Charsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes)).toString()) }
                catch (_: java.nio.charset.CharacterCodingException) { RecoveryRead.Failed }
            }
        } catch (_: IOException) { RecoveryRead.Failed }
    }
    override suspend fun save(source: String): Boolean = write(path, source)
    override suspend fun archive(source: String): Boolean = write(path.resolveSibling(path.fileName.toString() + ".conflict-${java.util.UUID.randomUUID()}.json"), source)
    private suspend fun write(target: Path, source: String): Boolean = withContext(Dispatchers.IO) {
        if (source.length > 8_000_000) return@withContext false
        val bytes = source.toByteArray(Charsets.UTF_8)
        if (bytes.size > 32_000_000) return@withContext false
        val parent = target.toAbsolutePath().parent
        var temporary: Path? = null
        try {
            Files.createDirectories(parent)
            temporary = Files.createTempFile(parent, "humming-checkpoint-", ".tmp")
            FileChannel.open(temporary, StandardOpenOption.WRITE).use { channel ->
                val buffer = ByteBuffer.wrap(bytes)
                while (buffer.hasRemaining()) { currentCoroutineContext().ensureActive(); channel.write(buffer) }
                channel.force(true)
            }
            currentCoroutineContext().ensureActive()
            Files.move(temporary, target.toAbsolutePath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
            true
        } catch (_: IOException) { false }
        finally { temporary?.let { Files.deleteIfExists(it) } }
    }
}
