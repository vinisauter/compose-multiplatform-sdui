package me.developes.humming.server.host

import kotlinx.coroutines.*
import kotlinx.serialization.json.*
import me.developes.humming.server.*
import me.developes.humming.sdui.common.*
import me.developes.humming.sdui.studio.*
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import kotlin.test.*

class DraftHttpHostTest {
    private val key = DocumentKey("tenant-a", "document")
    private val catalog = initialNodeCatalog()
    private fun actor() = Actor("author", Permission.entries.map { DocumentGrant(key, it) }.toSet())
    private fun source() = AuthoringDocumentCodec(catalog).encode(LegacyAuthoringCodec(catalog).importDocument(
        """{"id":"label","component":"layout:text","properties":{"text":"Actual HTTP"}}""", key.document).requireValue()).requireValue()

    private class Client(val port: Int, val token: String) : AutoCloseable {
        val http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build()
        fun request(method: String = "GET", path: String = "/v1/tenants/tenant-a/documents/document",
                    body: ByteArray = byteArrayOf(), authorization: String? = "Bearer $token", contentType: String = "application/json"): HttpResponse<String> {
            val request = HttpRequest.newBuilder(URI("http://127.0.0.1:$port$path")).timeout(Duration.ofSeconds(5))
                .header("Content-Type", contentType)
            authorization?.let { request.header("Authorization", it) }
            return http.send(request.method(method, HttpRequest.BodyPublishers.ofByteArray(body)).build(), HttpResponse.BodyHandlers.ofString())
        }
        override fun close() { http.close() }
    }
    private fun withHost(repository: DraftRepository = VolatileDraftRepository(), target: TargetCapabilities = TargetCapabilities(catalog.capabilities),
                         maxBodyBytes: Int = 2_000_000, check: (Client, DraftHttpHost) -> Unit) {
        val token = UUID.randomUUID().toString()
        val app = DraftApplication(repository, ExactGrantAuthorizer, catalog, target)
        DraftHttpHost(app, BearerAuthenticator(token, actor()), maxBodyBytes = maxBodyBytes).use { host ->
            Client(host.port, token).use { client -> check(client, host) }
        }
    }

    @Test fun realHttpCreateReadValidateConflictAndTenantDocumentAuthorization() = withHost { client, _ ->
        assertEquals(401, client.request(authorization = null).statusCode())
        assertEquals(401, client.request(authorization = "Bearer wrong").statusCode())
        assertEquals(404, client.request().statusCode())
        val created = client.request("POST", body = source().toByteArray())
        assertEquals(201, created.statusCode())
        assertEquals("no-store", created.headers().firstValue("Cache-Control").orElse(null))
        val read = client.request()
        assertEquals(200, read.statusCode())
        assertEquals(Json.parseToJsonElement(created.body()), Json.parseToJsonElement(read.body()))
        val validation = client.request("POST", "/v1/tenants/tenant-a/documents/document/validate")
        assertEquals(200, validation.statusCode())
        assertEquals(JsonPrimitive(true), Json.parseToJsonElement(validation.body()).jsonObject["valid"])
        assertEquals(409, client.request("POST", body = source().toByteArray()).statusCode())
        assertEquals(403, client.request(path = "/v1/tenants/tenant-b/documents/document").statusCode())
        assertEquals(403, client.request("POST", "/v1/tenants/tenant-a/documents/other", "{".toByteArray(), contentType = "text/plain").statusCode())
        assertEquals(405, client.request("DELETE").statusCode())
    }

    @Test fun boundedUtf8InputAndCompilerDiagnosticsTravelThroughRealHttp() = withHost(target = TargetCapabilities(emptyMap()), maxBodyBytes = 4096) { client, _ ->
        assertEquals(415, client.request("POST", body = source().toByteArray(), contentType = "text/plain").statusCode())
        assertEquals(400, client.request("POST", body = byteArrayOf(0xc3.toByte(), 0x28)).statusCode())
        assertEquals(413, client.request("POST", body = ByteArray(4097) { 32 }).statusCode())
        val malformed = client.request("POST", body = "{".toByteArray())
        assertEquals(422, malformed.statusCode())
        assertTrue(Json.parseToJsonElement(malformed.body()).jsonObject.getValue("diagnostics").jsonArray.isNotEmpty())
        assertEquals(201, client.request("POST", body = source().toByteArray()).statusCode())
        val diagnostics = Json.parseToJsonElement(client.request("POST", "/v1/tenants/tenant-a/documents/document/validate").body()).jsonObject
        assertEquals(JsonPrimitive(false), diagnostics["valid"])
        assertTrue(diagnostics.getValue("diagnostics").jsonArray.any { it.jsonObject["code"] == JsonPrimitive("UNSUPPORTED_CAPABILITY") && it.jsonObject["nodeId"] == JsonPrimitive("label") })
    }

    @Test fun concurrentCreatesUseAtomicInsertAndVolatileRestartLosesDataExplicitly() {
        withHost { client, _ ->
            val attempts = (1..8).map { CompletableFuture.supplyAsync { client.request("POST", body = source().toByteArray()).statusCode() } }
                .map { it.get(10, TimeUnit.SECONDS) }
            assertEquals(1, attempts.count { it == 201 })
            assertEquals(7, attempts.count { it == 409 })
        }
        withHost { client, _ -> assertEquals(404, client.request().statusCode()) }
    }

    @Test fun repositoryDefectsAre500WithOriginalFailureAndCloseCancelsSuspendedRequests() {
        val failure = IllegalStateException("repository defect")
        val errors = java.util.concurrent.CopyOnWriteArrayList<Throwable>()
        val token = UUID.randomUUID().toString()
        val repository = object : DraftRepository {
            override suspend fun create(draft: StoredDraft): Boolean = throw failure
            override suspend fun find(key: DocumentKey): StoredDraft? = throw failure
        }
        val application = DraftApplication(repository, ExactGrantAuthorizer, catalog, TargetCapabilities(catalog.capabilities))
        DraftHttpHost(application, BearerAuthenticator(token, actor()), onUnexpectedFailure = errors::add).use { host ->
            Client(host.port, token).use { client ->
                val response = client.request()
                assertEquals(500, response.statusCode())
                assertFalse(response.body().contains("repository defect"))
                assertTrue(generateSequence(errors.single()) { it.cause }.any { it === failure })
            }
        }
        val entered = CompletableDeferred<Unit>()
        val cancelled = CompletableDeferred<Unit>()
        val waiting = object : DraftRepository {
            override suspend fun create(draft: StoredDraft): Boolean = false
            override suspend fun find(key: DocumentKey): StoredDraft? {
                entered.complete(Unit)
                try { awaitCancellation() } finally { cancelled.complete(Unit) }
            }
        }
        val host = DraftHttpHost(DraftApplication(waiting, ExactGrantAuthorizer, catalog, TargetCapabilities(catalog.capabilities)), BearerAuthenticator(token, actor()))
        try {
            Client(host.port, token).use { client ->
                val future = CompletableFuture.runAsync { runCatching { client.request() } }
                runBlocking { withTimeout(5000) { entered.await() } }
                host.close()
                runBlocking { withTimeout(5000) { cancelled.await() } }
                future.get(5, TimeUnit.SECONDS)
            }
        } finally { host.close() }
    }

    @Test fun suspendedRepositoryTimeoutIs504AndReleasesItsResources() {
        val cleaned = CompletableDeferred<Unit>()
        val repository = object : DraftRepository {
            override suspend fun create(draft: StoredDraft) = false
            override suspend fun find(key: DocumentKey): StoredDraft? {
                try { awaitCancellation() } finally { cleaned.complete(Unit) }
            }
        }
        val token = UUID.randomUUID().toString()
        val app = DraftApplication(repository, ExactGrantAuthorizer, catalog, TargetCapabilities(catalog.capabilities))
        DraftHttpHost(app, BearerAuthenticator(token, actor()), operationTimeoutMillis = 50).use { host ->
            Client(host.port, token).use { client ->
                val response = client.request()
                assertEquals(504, response.statusCode())
                assertEquals(JsonPrimitive("OPERATION_TIMEOUT"), Json.parseToJsonElement(response.body()).jsonObject["code"])
                assertTrue(cleaned.isCompleted)
            }
        }
    }
}
