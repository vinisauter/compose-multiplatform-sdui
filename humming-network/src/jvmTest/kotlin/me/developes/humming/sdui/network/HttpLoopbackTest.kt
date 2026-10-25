package me.developes.humming.sdui.network

import com.sun.net.httpserver.HttpServer
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.*
import me.developes.humming.sdui.common.*
import java.net.InetSocketAddress
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.*

class HttpLoopbackTest {
    @Test fun realLoopbackGetPostErrorsTimeoutRedirectAndConcurrentRequests() = runBlocking<Unit> {
        val executor = Executors.newCachedThreadPool()
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        val getCount = AtomicInteger()
        server.executor = executor
        server.createContext("/") { exchange ->
            val path = exchange.requestURI.path
            val raw = exchange.requestBody.use { it.readAllBytes().decodeToString() }
            if (path == "/slow") Thread.sleep(250)
            val status = when (path) { "/status" -> 503; "/redirect" -> 302; else -> 200 }
            if (path == "/redirect") exchange.responseHeaders.add("Location", "/echo")
            val body = when (path) {
                "/bad" -> "invalid JSON"
                else -> if (exchange.requestMethod == "POST") raw else "{\"method\":\"GET\"}"
            }.encodeToByteArray()
            if (path == "/echo" && exchange.requestMethod == "GET") getCount.incrementAndGet()
            exchange.responseHeaders.add("Content-Type", "application/json")
            exchange.sendResponseHeaders(status, body.size.toLong())
            exchange.responseBody.use { it.write(body) }
        }
        server.start()
        val client = HttpClient(OkHttp) { followRedirects = false }
        val base = "http://127.0.0.1:${server.address.port}"
        try {
            val endpoints = listOf("echo", "status", "bad", "slow", "redirect").associateWith { HttpEndpoint("$base/$it", setOf("GET", "POST")) }
            val transport = SduiHttpTransport(client, endpoints)
            assertEquals(parseSduiValue("""{"method":"GET"}"""), assertIs<HttpOutcome.Success>(transport.request("echo")).body)
            val post = parseSduiValue("""{"id":"0007","amount":1.2300}""")
            assertEquals(post, assertIs<HttpOutcome.Success>(transport.request("echo", "POST", post)).body)
            assertEquals(HttpOutcome.Failure("NETWORK_HTTP_STATUS", 503), transport.request("status"))
            assertEquals(HttpOutcome.Failure("NETWORK_JSON", 200), transport.request("bad"))
            assertEquals(HttpOutcome.Failure("NETWORK_HTTP_STATUS", 302), transport.request("redirect"))
            assertEquals(1, getCount.get())
            assertEquals(HttpOutcome.Failure("NETWORK_TIMEOUT"), SduiHttpTransport(client, endpoints, HttpPolicy(timeoutMillis = 30)).request("slow"))
            val concurrent = (1..8).map { index -> async {
                val body = SduiValue.ObjectValue(mapOf("id" to SduiValue.Text("000$index")))
                assertEquals(body, assertIs<HttpOutcome.Success>(transport.request("echo", "POST", body)).body)
            } }
            concurrent.awaitAll()
            assertIs<HttpOutcome.Success>(transport.request("echo"))
        } finally {
            client.close(); server.stop(0); executor.shutdownNow()
        }
    }
}
