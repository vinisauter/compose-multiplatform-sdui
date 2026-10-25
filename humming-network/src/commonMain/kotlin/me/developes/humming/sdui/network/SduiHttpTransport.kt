package me.developes.humming.sdui.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRedirect
import io.ktor.client.plugins.pluginOrNull
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.*
import io.ktor.utils.io.readBuffer
import kotlinx.io.IOException
import kotlinx.io.readByteArray
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import me.developes.humming.sdui.common.*
import kotlin.text.CharacterCodingException

class HttpEndpoint(val url: String, methods: Set<String> = setOf("GET"), headers: Map<String, String> = emptyMap()) {
    val methods = methods.toSet()
    val headers = headers.toMap()
    init {
        val parsed = Url(url)
        require(parsed.protocol.name in setOf("http", "https") && parsed.user.isNullOrEmpty() && parsed.password.isNullOrEmpty() && parsed.fragment.isEmpty())
        require(methods.isNotEmpty() && methods.all { it in setOf("GET", "POST") })
        require(headers.all { (name, value) -> name.isNotBlank() && (name + value).none { it == '\r' || it == '\n' } })
    }
}
data class HttpPolicy(val timeoutMillis: Long = 10_000, val maxRequestBytes: Int = 65_536, val maxResponseBytes: Int = 1_048_576) {
    init { require(timeoutMillis in 1..120_000 && maxRequestBytes in 1..8_388_608 && maxResponseBytes in 1..8_388_608) }
}
sealed interface HttpOutcome {
    data class Success(val status: Int, val body: SduiValue) : HttpOutcome
    data class Failure(val code: String, val status: Int? = null) : HttpOutcome
}

/** Fixed host endpoints, no payload URL/headers, no retries and no client allocation/close per request. */
class SduiHttpTransport(private val client: HttpClient, endpoints: Map<String, HttpEndpoint>, private val policy: HttpPolicy = HttpPolicy()) {
    private val endpoints = endpoints.toMap()
    init {
        require(client.pluginOrNull(HttpRedirect) == null) { "Host HttpClient must use followRedirects=false" }
        require(endpoints.keys.all { it.isNotBlank() })
    }
    suspend fun request(endpointName: String, methodName: String = "GET", body: SduiValue = SduiValue.Missing): HttpOutcome {
        currentCoroutineContext().ensureActive()
        val endpoint = endpoints[endpointName] ?: return HttpOutcome.Failure("NETWORK_ENDPOINT")
        if (methodName !in endpoint.methods) return HttpOutcome.Failure("NETWORK_METHOD")
        if (methodName == "GET" && body !== SduiValue.Missing) return HttpOutcome.Failure("NETWORK_GET_BODY")
        if (body !== SduiValue.Missing && !bodyWithinBudget(body)) return HttpOutcome.Failure("NETWORK_REQUEST_LIMIT")
        val bytes = body.takeUnless { it === SduiValue.Missing }?.toJsonValue()?.toString()?.encodeToByteArray()
        if (bytes != null && bytes.size > policy.maxRequestBytes) return HttpOutcome.Failure("NETWORK_REQUEST_LIMIT")
        return withTimeoutOrNull(policy.timeoutMillis) {
            try {
                client.prepareRequest(endpoint.url) {
                    method = HttpMethod.parse(methodName)
                    expectSuccess = false
                    accept(ContentType.Application.Json)
                    endpoint.headers.forEach { (name, value) -> header(name, value) }
                    if (bytes != null) { contentType(ContentType.Application.Json); setBody(bytes) }
                }.execute { response ->
                    val status = response.status.value
                    if (status !in 200..299) return@execute HttpOutcome.Failure("NETWORK_HTTP_STATUS", status)
                    if (status == 204) return@execute HttpOutcome.Success(status, SduiValue.Null)
                    val type = response.contentType()
                    if (type == null || type.contentType != "application" || !(type.contentSubtype == "json" || type.contentSubtype.endsWith("+json")))
                        return@execute HttpOutcome.Failure("NETWORK_CONTENT_TYPE", status)
                    val raw = response.bodyAsChannel().readBuffer(policy.maxResponseBytes.toLong() + 1).readByteArray()
                    if (raw.size > policy.maxResponseBytes) return@execute HttpOutcome.Failure("NETWORK_RESPONSE_LIMIT", status)
                    try {
                        HttpOutcome.Success(status, parseSduiValue(raw.decodeToString(throwOnInvalidSequence = true), PayloadLimits(maxInputChars = policy.maxResponseBytes)))
                    } catch (error: PayloadException) { HttpOutcome.Failure("NETWORK_JSON", status) }
                    catch (error: CharacterCodingException) { HttpOutcome.Failure("NETWORK_JSON", status) }
                }
            } catch (error: IOException) {
                currentCoroutineContext().ensureActive()
                HttpOutcome.Failure("NETWORK_IO")
            }
        } ?: HttpOutcome.Failure("NETWORK_TIMEOUT")
    }

    /** Bound traversal and potential serialization allocation before calling the recursive JSON codec. */
    private fun bodyWithinBudget(body: SduiValue): Boolean {
        val pending = ArrayDeque<Pair<SduiValue, Int>>().apply { add(body to 1) }
        var characters = 0L
        var nodes = 0
        while (pending.isNotEmpty()) {
            val (value, depth) = pending.removeLast()
            if (++nodes > 10_000 || depth > 64) return false
            when (value) {
                is SduiValue.ObjectValue -> {
                    if (value.values.size > 10_000) return false
                    characters += 2 + (value.values.size - 1).coerceAtLeast(0).toLong()
                    for ((key, child) in value.values) {
                        characters += key.length.toLong() + 3
                        pending.add(child to depth + 1)
                    }
                }
                is SduiValue.ArrayValue -> {
                    if (value.values.size > 10_000) return false
                    characters += 2 + (value.values.size - 1).coerceAtLeast(0).toLong()
                    value.values.forEach { pending.add(it to depth + 1) }
                }
                is SduiValue.Text -> characters += value.value.length.toLong() + 2
                is SduiValue.NumberValue -> characters += value.source.length
                is SduiValue.BooleanValue -> characters += if (value.value) 4 else 5
                SduiValue.Null -> characters += 4
                SduiValue.Missing -> return false
            }
            if (characters > policy.maxRequestBytes || pending.size > 10_000) return false
        }
        return true
    }
}
