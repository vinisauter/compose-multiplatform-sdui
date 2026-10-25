package me.developes.humming.sdui.network

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.http.content.OutgoingContent
import kotlinx.coroutines.*
import kotlin.test.*
import me.developes.humming.sdui.common.SduiValue
import java.util.concurrent.atomic.AtomicInteger

class HttpTransportTest {
    private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")
    private fun endpoint(path: String = "ok") = HttpEndpoint("https://local.invalid/$path", setOf("GET", "POST"))

    @Test fun realMethodsTypedBodyAndEndpointAllowlistUseTheSameClient() = runBlocking<Unit> {
        val requests = mutableListOf<String>()
        val client = HttpClient(MockEngine { request ->
            requests += request.method.value
            if (request.method == HttpMethod.Post) assertEquals("{\"id\":\"0007\"}",
                (request.body as OutgoingContent.ByteArrayContent).bytes().decodeToString())
            respond("{\"ok\":true}", headers = jsonHeaders)
        }) { followRedirects = false }
        try {
            val transport = SduiHttpTransport(client, mapOf("echo" to endpoint()))
            assertIs<HttpOutcome.Success>(transport.request("echo"))
            assertIs<HttpOutcome.Success>(transport.request("echo", "POST", me.developes.humming.sdui.common.parseSduiValue("""{"id":"0007"}""")))
            assertEquals(HttpOutcome.Failure("NETWORK_ENDPOINT"), transport.request("https://unregistered.invalid"))
            assertEquals(HttpOutcome.Failure("NETWORK_METHOD"), transport.request("echo", "DELETE"))
            assertEquals(listOf("GET", "POST"), requests)
        } finally { client.close(); client.engine.close() }
    }

    @Test fun statusMalformedJsonContentTypeAndResponseLimitHaveSafeCodes() = runBlocking<Unit> {
        val client = HttpClient(MockEngine { request -> when (request.url.encodedPath) {
            "/status" -> respond("private response", HttpStatusCode.ServiceUnavailable, jsonHeaders)
            "/bad" -> respond("not JSON", headers = jsonHeaders)
            "/large" -> respond("\"${"x".repeat(200)}\"", headers = jsonHeaders)
            "/html" -> respond("<html>private</html>", headers = headersOf(HttpHeaders.ContentType, "text/html"))
            "/utf8" -> respond(byteArrayOf(34, 0xc3.toByte(), 40, 34), headers = jsonHeaders)
            else -> respond("", HttpStatusCode.NoContent)
        } }) { followRedirects = false }
        try {
            val transport = SduiHttpTransport(client, listOf("status", "bad", "large", "html", "empty", "utf8").associateWith { endpoint(it) }, HttpPolicy(maxResponseBytes = 64))
            assertEquals(HttpOutcome.Failure("NETWORK_HTTP_STATUS", 503), transport.request("status"))
            assertEquals(HttpOutcome.Failure("NETWORK_JSON", 200), transport.request("bad"))
            assertEquals(HttpOutcome.Failure("NETWORK_RESPONSE_LIMIT", 200), transport.request("large"))
            assertEquals(HttpOutcome.Failure("NETWORK_CONTENT_TYPE", 200), transport.request("html"))
            assertEquals(HttpOutcome.Failure("NETWORK_JSON", 200), transport.request("utf8"))
            assertEquals(HttpOutcome.Success(204, me.developes.humming.sdui.common.SduiValue.Null), transport.request("empty"))
        } finally { client.close(); client.engine.close() }
    }

    @Test fun ownedTimeoutReturnsFailureParentCancellationPropagatesAndProgrammingErrorsEscape() = runBlocking<Unit> {
        val calls = AtomicInteger()
        val secondEntered = CompletableDeferred<Unit>()
        val client = HttpClient(MockEngine { request ->
            if (request.url.encodedPath == "/bug") error("programming defect")
            if (request.url.encodedPath == "/io") throw kotlinx.io.IOException("private connection details")
            if (calls.incrementAndGet() == 2) secondEntered.complete(Unit)
            awaitCancellation()
        }) { followRedirects = false }
        try {
            val short = SduiHttpTransport(client, mapOf("slow" to endpoint("slow")), HttpPolicy(timeoutMillis = 50))
            assertEquals(HttpOutcome.Failure("NETWORK_TIMEOUT"), short.request("slow"))
            val transport = SduiHttpTransport(client, mapOf("slow" to endpoint("slow"), "bug" to endpoint("bug"), "io" to endpoint("io")))
            val request = async { transport.request("slow") }
            secondEntered.await(); request.cancelAndJoin()
            assertTrue(request.isCancelled)
            assertFailsWith<IllegalStateException> { transport.request("bug") }
            assertEquals(HttpOutcome.Failure("NETWORK_IO"), transport.request("io"))
        } finally { client.close(); client.engine.close() }
    }

    @Test fun hostMustDisableRedirectsAndCannotRegisterCredentialUrls() {
        val client = HttpClient(MockEngine { respond("{}", headers = jsonHeaders) })
        try { assertFailsWith<IllegalArgumentException> { SduiHttpTransport(client, mapOf("echo" to endpoint())) } }
        finally { client.close(); client.engine.close() }
        assertFailsWith<IllegalArgumentException> { HttpEndpoint("https://user:password@example.invalid/path") }
        assertFailsWith<IllegalArgumentException> { HttpEndpoint("file:///tmp/data") }
    }

    @Test fun requestBudgetsApplyBeforeSerializationAndPreserveExactSmallBoundaries() = runBlocking<Unit> {
        var calls = 0
        val client = HttpClient(MockEngine { calls++; respond("{}", headers = jsonHeaders) }) { followRedirects = false }
        try {
            val transport = SduiHttpTransport(client, mapOf("echo" to endpoint()), HttpPolicy(maxRequestBytes = 7))
            assertIs<HttpOutcome.Success>(transport.request("echo", "POST", SduiValue.ObjectValue(mapOf("a" to SduiValue.NumberValue("1")))))
            assertEquals(HttpOutcome.Failure("NETWORK_REQUEST_LIMIT"), transport.request("echo", "POST", SduiValue.Text("x".repeat(1000))))
            assertEquals(HttpOutcome.Failure("NETWORK_REQUEST_LIMIT"), transport.request("echo", "POST", SduiValue.Text("\n".repeat(4))))
            var deep: SduiValue = SduiValue.Null
            repeat(65) { deep = SduiValue.ArrayValue(listOf(deep)) }
            assertEquals(HttpOutcome.Failure("NETWORK_REQUEST_LIMIT"), transport.request("echo", "POST", deep))
            assertEquals(1, calls)
            assertEquals(HttpOutcome.Failure("NETWORK_GET_BODY"), transport.request("echo", body = SduiValue.Null))
            assertEquals(1, calls)
        } finally { client.close(); client.engine.close() }
    }
}
