package me.developes.humming.server.host

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.*
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.*
import me.developes.humming.server.*
import me.developes.humming.sdui.common.*
import me.developes.humming.sdui.studio.TargetCapabilities
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction
import java.nio.charset.CharacterCodingException
import java.security.MessageDigest
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicBoolean

/** T20 adapter only: data is lost on close/restart. No database claim or implicit disk storage. */
class VolatileDraftRepository : DraftRepository {
    private val drafts = ConcurrentHashMap<DocumentKey, StoredDraft>()
    override suspend fun create(draft: StoredDraft) = drafts.putIfAbsent(draft.key, draft) == null
    override suspend fun find(key: DocumentKey) = drafts[key]
}
fun interface RequestAuthenticator { fun authenticate(authorization: String?): Actor? }
class BearerAuthenticator(token: String, private val actor: Actor) : RequestAuthenticator {
    private val expected = token.toByteArray(Charsets.UTF_8)
    init { require(token.length in 32..256 && token.all { it.code in 33..126 }) }
    override fun authenticate(authorization: String?): Actor? {
        val supplied = authorization?.takeIf { it.startsWith("Bearer ") && it.length <= 263 }?.substring(7) ?: return null
        return actor.takeIf { MessageDigest.isEqual(expected, supplied.toByteArray(Charsets.UTF_8)) }
    }
}

/** A bounded local development host. Not an Internet-facing deployment or a production authentication service. */
class DraftHttpHost(
    private val application: DraftApplication,
    private val authenticator: RequestAuthenticator,
    port: Int = 0,
    private val maxBodyBytes: Int = 2_000_000,
    private val operationTimeoutMillis: Long = 10_000,
    private val onUnexpectedFailure: (Throwable) -> Unit = { System.err.println("Draft host failed: ${it::class.simpleName}") },
) : AutoCloseable {
    private val job = SupervisorJob()
    private val closed = AtomicBoolean()
    private val executor = ThreadPoolExecutor(4, 4, 0, TimeUnit.MILLISECONDS, ArrayBlockingQueue(32),
        ThreadFactory { task -> Thread(task, "humming-local-http").apply { isDaemon = true } }, ThreadPoolExecutor.AbortPolicy())
    private val server: HttpServer
    val port: Int get() = server.address.port
    init {
        require(port in 0..65535 && maxBodyBytes in 1..8_000_000 && operationTimeoutMillis in 1..120_000)
        server = HttpServer.create(InetSocketAddress("127.0.0.1", port), 32)
        server.executor = executor
        server.createContext("/") { exchange ->
            try {
                runBlocking(job + Dispatchers.Unconfined) { withTimeout(operationTimeoutMillis) { handle(exchange) } }
            } catch (_: TimeoutCancellationException) {
                try { respond(exchange, 504, error("OPERATION_TIMEOUT")) } catch (_: java.io.IOException) { }
            } catch (_: CancellationException) {
                // Closing the host cancels requests; no fake payload failure or response after cancellation.
            } catch (_: java.io.IOException) {
                // A disconnected client or host shutdown cannot receive another response.
            } catch (failure: Exception) {
                onUnexpectedFailure(failure)
                try { respond(exchange, 500, error("INTERNAL_ERROR")) } catch (_: java.io.IOException) { }
            } finally { exchange.close() }
        }
        server.start()
    }

    private suspend fun handle(exchange: HttpExchange) {
        val authorization = exchange.requestHeaders["Authorization"]
        val actor = authenticator.authenticate(authorization?.singleOrNull())
        if (actor == null) { respond(exchange, 401, error("UNAUTHENTICATED")); return }
        if (exchange.requestURI.rawQuery != null) { respond(exchange, 400, error("INVALID_ROUTE")); return }
        val route = Regex("/v1/tenants/([A-Za-z0-9][A-Za-z0-9_-]{0,127})/documents/([A-Za-z0-9][A-Za-z0-9_-]{0,127})(/validate)?")
            .matchEntire(exchange.requestURI.rawPath)
        if (route == null) { respond(exchange, 404, error("ROUTE_NOT_FOUND")); return }
        val key = DocumentKey(route.groupValues[1], route.groupValues[2])
        val validation = route.groupValues[3].isNotEmpty()
        val method = exchange.requestMethod
        if ((validation && method != "POST") || (!validation && method !in setOf("GET", "POST"))) {
            exchange.responseHeaders.set("Allow", if (validation) "POST" else "GET, POST")
            respond(exchange, 405, error("METHOD_NOT_ALLOWED")); return
        }
        val permission = if (validation) Permission.VALIDATE else if (method == "GET") Permission.READ else Permission.CREATE
        if (!application.isAllowed(actor, key, permission)) { respond(exchange, 403, error("FORBIDDEN")); return }
        val result = when {
            validation -> application.validate(actor, key)
            method == "GET" -> application.read(actor, key)
            else -> {
                // Authorization is checked by the application before parsing, but the transport must bound wire input first.
                val contentType = exchange.requestHeaders.getFirst("Content-Type")?.lowercase()?.replace(" ", "")
                if (contentType !in setOf("application/json", "application/json;charset=utf-8")) {
                    respond(exchange, 415, error("JSON_REQUIRED")); return
                }
                val bytes = exchange.requestBody.readNBytes(maxBodyBytes + 1)
                if (bytes.size > maxBodyBytes) { respond(exchange, 413, error("BODY_LIMIT")); return }
                val source = try {
                    Charsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                        .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes)).toString()
                } catch (_: CharacterCodingException) { respond(exchange, 400, error("INVALID_UTF8")); return }
                application.create(actor, key, source)
            }
        }
        currentCoroutineContext().ensureActive()
        when (result) {
            is ApplicationResult.Failure -> respond(exchange, when (result.code) {
                ApplicationError.FORBIDDEN -> 403
                ApplicationError.NOT_FOUND -> 404
                ApplicationError.ALREADY_EXISTS -> 409
                else -> 422
            }, buildJsonObject {
                put("code", result.code.name)
                put("diagnostics", JsonArray(result.diagnostics.map(::diagnostic)))
            })
            is ApplicationResult.Success<*> -> when (val value = result.value) {
                is StoredDraft -> respond(exchange, if (method == "POST") 201 else 200, buildJsonObject {
                    put("revision", value.revision)
                    put("document", Json.parseToJsonElement(value.source))
                })
                is DraftValidation -> respond(exchange, 200, buildJsonObject {
                    put("valid", value.valid); put("diagnostics", JsonArray(value.diagnostics.map(::diagnostic)))
                })
                else -> kotlin.error("Unknown application result")
            }
        }
    }

    private fun diagnostic(value: NodeDiagnostic) = buildJsonObject {
        put("code", value.code); put("message", value.message); put("operation", value.operation)
        put("path", value.path); put("severity", value.severity.name)
        value.nodeId?.let { put("nodeId", it) }; value.documentId?.let { put("documentId", it) }
    }
    private fun error(code: String) = buildJsonObject { put("code", code) }
    private fun respond(exchange: HttpExchange, status: Int, body: JsonObject) {
        val bytes = body.toString().toByteArray(Charsets.UTF_8)
        exchange.responseHeaders.set("Content-Type", "application/json; charset=utf-8")
        exchange.responseHeaders.set("Cache-Control", "no-store")
        exchange.sendResponseHeaders(status, bytes.size.toLong())
        exchange.responseBody.write(bytes)
    }
    override fun close() {
        if (!closed.compareAndSet(false, true)) return
        job.cancel()
        server.stop(0)
        executor.shutdownNow()
        check(executor.awaitTermination(5, TimeUnit.SECONDS)) { "Local HTTP workers did not stop" }
    }
}

fun main() {
    val token = requireNotNull(System.getenv("HUMMING_LOCAL_TOKEN")) { "Set HUMMING_LOCAL_TOKEN (32..256 printable characters)" }
    val key = DocumentKey("local", "example")
    val actor = Actor("local-author", Permission.entries.map { DocumentGrant(key, it) }.toSet())
    val catalog = initialNodeCatalog()
    val application = DraftApplication(VolatileDraftRepository(), ExactGrantAuthorizer, catalog,
        TargetCapabilities(catalog.capabilities))
    val host = DraftHttpHost(application, BearerAuthenticator(token, actor))
    Runtime.getRuntime().addShutdownHook(Thread { host.close() })
    println("Volatile draft host: http://127.0.0.1:${host.port}/v1/tenants/local/documents/example (no durable storage)")
    CountDownLatch(1).await()
}
