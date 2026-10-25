package me.developes.humming.server.host

import kotlinx.coroutines.suspendCancellableCoroutine
import me.developes.humming.server.*
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.URI
import java.net.http.*
import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction
import java.time.Duration
import java.util.Base64
import java.util.concurrent.*
import java.util.concurrent.Flow
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Endpoint/client are host configuration; no payload may choose URL, verb, headers or credentials. */
class RegisteredHttpDataSource(private val client: HttpClient, private val endpoint: URI,
    private val timeoutMillis: Long = 2_000, private val maxBytes: Int = 65_536) : RegisteredDataSource {
    init {
        require(endpoint.scheme in setOf("http", "https") && endpoint.host != null && endpoint.userInfo == null && endpoint.fragment == null)
        require(client.followRedirects() == HttpClient.Redirect.NEVER)
        require(timeoutMillis in 1..120_000 && maxBytes in 1..1_000_000)
    }
    override suspend fun load(context: DataSourceContext): SourceData {
        fun safe(value: String) = Base64.getUrlEncoder().withoutPadding().encodeToString(value.toByteArray(Charsets.UTF_8))
        val request = HttpRequest.newBuilder(endpoint).timeout(Duration.ofMillis(timeoutMillis)).GET()
            .header("Accept", "application/json").header("X-Humming-Tenant", context.document.tenant)
            .header("X-Humming-Actor", safe(context.identity.actor.id)).header("X-Humming-Session", safe(context.identity.session))
            .build()
        return try {
            val response = client.sendAsync(request, HttpResponse.BodyHandler { BoundedSubscriber(maxBytes) }).awaitResponse()
            if (response.statusCode() !in 200..299) return SourceData.Failure(DataFailure.HTTP_STATUS)
            val contentType = response.headers().firstValue("Content-Type").orElse("").substringBefore(';').trim().lowercase()
            if (contentType != "application/json" && !(contentType.startsWith("application/") && contentType.endsWith("+json")))
                return SourceData.Failure(DataFailure.INVALID_RESPONSE)
            val text = try { Charsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(response.body())).toString() }
            catch (_: java.nio.charset.CharacterCodingException) { return SourceData.Failure(DataFailure.INVALID_RESPONSE) }
            SourceData.Success(text)
        } catch (_: HttpTimeoutException) { SourceData.Failure(DataFailure.TIMEOUT)
        } catch (_: BodyLimitException) { SourceData.Failure(DataFailure.BODY_LIMIT)
        } catch (_: IOException) { SourceData.Failure(DataFailure.IO) }
    }
    private class BodyLimitException : IOException()
    private class BoundedSubscriber(private val limit: Int) : HttpResponse.BodySubscriber<ByteArray> {
        private val result = CompletableFuture<ByteArray>()
        private val bytes = ByteArrayOutputStream()
        private var subscription: Flow.Subscription? = null
        override fun getBody(): CompletionStage<ByteArray> = result
        override fun onSubscribe(value: Flow.Subscription) { subscription = value; value.request(1) }
        override fun onNext(items: List<ByteBuffer>) {
            for (item in items) {
                if (item.remaining() > limit - bytes.size()) {
                    subscription?.cancel(); result.completeExceptionally(BodyLimitException()); return
                }
                val chunk = ByteArray(item.remaining()); item.get(chunk); bytes.write(chunk)
            }
            subscription?.request(1)
        }
        override fun onError(failure: Throwable) { result.completeExceptionally(failure) }
        override fun onComplete() { result.complete(bytes.toByteArray()) }
    }
    private suspend fun <T> CompletableFuture<T>.awaitResponse(): T = suspendCancellableCoroutine { continuation ->
        continuation.invokeOnCancellation { cancel(true) }
        whenComplete { value, failure ->
            if (failure == null) continuation.resume(value)
            else {
                var cause = failure
                while ((cause is CompletionException || cause is ExecutionException) && cause.cause != null) cause = cause.cause!!
                continuation.resumeWithException(cause)
            }
        }
    }
}
