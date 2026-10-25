package me.developes.humming.server.host

import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.*
import me.developes.humming.server.*
import java.net.InetSocketAddress
import java.net.URI
import java.net.http.HttpClient
import java.util.Base64
import java.util.concurrent.*
import kotlin.test.*

class RegisteredHttpDataSourceTest {
    private val context = DataSourceContext(DataIdentity(Actor("actor", emptySet()), "session"), DocumentKey("tenant", "document"), "a".repeat(64))
    private fun service(check: (HttpServer, HttpClient) -> Unit, handler: (com.sun.net.httpserver.HttpExchange) -> Unit) {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 8)
        val executor = Executors.newFixedThreadPool(2)
        server.executor = executor; server.createContext("/") { exchange -> try { handler(exchange) } finally { exchange.close() } }; server.start()
        try { HttpClient.newHttpClient().use { client -> check(server, client) } }
        finally { server.stop(0); executor.shutdownNow(); assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS)) }
    }
    private fun endpoint(server: HttpServer, path: String = "/") = URI("http://127.0.0.1:${server.address.port}$path")

    @Test fun realGetUsesFixedEndpointAndHostIdentityWithoutFollowingRedirects() = service({ server, client ->
        runBlocking {
            val source = RegisteredHttpDataSource(client, endpoint(server))
            assertEquals("""{"name":"service value"}""", assertIs<SourceData.Success>(source.load(context)).json)
            assertEquals(DataFailure.HTTP_STATUS, assertIs<SourceData.Failure>(RegisteredHttpDataSource(client, endpoint(server, "/redirect")).load(context)).code)
        }
    }) { exchange ->
        assertEquals("GET", exchange.requestMethod)
        assertEquals("tenant", exchange.requestHeaders.getFirst("X-Humming-Tenant"))
        assertEquals("session", String(Base64.getUrlDecoder().decode(exchange.requestHeaders.getFirst("X-Humming-Session")), Charsets.UTF_8))
        assertNull(exchange.requestHeaders.getFirst("Authorization"))
        if (exchange.requestURI.path == "/redirect") {
            exchange.responseHeaders.set("Location", "/never"); exchange.sendResponseHeaders(302, -1)
        } else {
            val bytes = """{"name":"service value"}""".toByteArray()
            exchange.responseHeaders.set("Content-Type", "application/json; charset=utf-8"); exchange.sendResponseHeaders(200, bytes.size.toLong()); exchange.responseBody.write(bytes)
        }
    }
    @Test fun streamingLimitMimeAndUtf8AreCheckedWithRealResponses() = service({ server, client ->
        runBlocking {
            for ((path, expected) in listOf("/large" to DataFailure.BODY_LIMIT, "/mime" to DataFailure.INVALID_RESPONSE, "/utf8" to DataFailure.INVALID_RESPONSE, "/error" to DataFailure.HTTP_STATUS)) {
                assertEquals(expected, assertIs<SourceData.Failure>(RegisteredHttpDataSource(client, endpoint(server, path), maxBytes = 128).load(context)).code, path)
            }
        }
    }) { exchange ->
        val path = exchange.requestURI.path
        val bytes = when (path) { "/large" -> ByteArray(10_000) { 32 }; "/utf8" -> byteArrayOf(0xc3.toByte(), 0x28); else -> "{}".toByteArray() }
        exchange.responseHeaders.set("Content-Type", if (path == "/mime") "text/plain" else "application/json")
        exchange.sendResponseHeaders(if (path == "/error") 503 else 200, bytes.size.toLong())
        try { exchange.responseBody.write(bytes) } catch (_: java.io.IOException) { }
    }
    @Test fun actualHttpTimeoutAndCallerCancellationTerminateTheAwait() {
        val entered = CountDownLatch(1); val release = CountDownLatch(1)
        service({ server, client ->
            runBlocking {
                assertEquals(DataFailure.TIMEOUT, assertIs<SourceData.Failure>(RegisteredHttpDataSource(client, endpoint(server), timeoutMillis = 50).load(context)).code)
                val pending = launch { RegisteredHttpDataSource(client, endpoint(server), timeoutMillis = 5_000).load(context) }
                delay(50); pending.cancelAndJoin(); assertTrue(pending.isCancelled)
                release.countDown()
            }
        }) { exchange ->
            entered.countDown(); release.await(3, TimeUnit.SECONDS)
            try { exchange.sendResponseHeaders(200, 2); exchange.responseBody.write("{}".toByteArray()) } catch (_: java.io.IOException) { }
        }
    }
}
