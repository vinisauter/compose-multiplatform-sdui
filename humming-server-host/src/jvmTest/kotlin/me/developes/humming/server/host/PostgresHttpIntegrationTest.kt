package me.developes.humming.server.host

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.*
import me.developes.humming.server.*
import me.developes.humming.server.postgres.*
import me.developes.humming.sdui.common.initialNodeCatalog
import me.developes.humming.sdui.studio.*
import java.net.URI
import java.net.http.*
import java.time.Duration
import java.util.UUID
import kotlin.test.*

class PostgresHttpIntegrationTest {
    @Test fun httpEditsSurviveHostReplacementAndRetryWithoutRepeatingTheMutation() {
        val url = requireNotNull(System.getenv("HUMMING_TEST_DB_URL"))
        val uri = URI(url.removePrefix("jdbc:"))
        require(uri.host in setOf("127.0.0.1", "localhost") && uri.path == "/humming_test")
        val catalog = initialNodeCatalog()
        fun repository() = PostgresDraftRepository(postgresDataSource(url,
            requireNotNull(System.getenv("HUMMING_TEST_DB_USER")), requireNotNull(System.getenv("HUMMING_TEST_DB_PASSWORD"))), catalog)
        val key = DocumentKey("http", "doc-${UUID.randomUUID()}")
        val actor = Actor("http-author", Permission.entries.map { DocumentGrant(key, it) }.toSet())
        val token = UUID.randomUUID().toString()
        fun host(): DraftHttpHost {
            val repo = repository()
            runBlocking { repo.migrate() }
            return DraftHttpHost(DraftApplication(repo, ExactGrantAuthorizer, catalog, TargetCapabilities(catalog.capabilities)),
                BearerAuthenticator(token, actor), editing = DraftEditingApplication(repo, ExactGrantAuthorizer))
        }
        val path = "/v1/tenants/${key.tenant}/documents/${key.document}"
        val source = AuthoringDocumentCodec(catalog).encode(LegacyAuthoringCodec(catalog).importDocument(
            """{"id":"label","component":"layout:text","properties":{"text":"before"}}""", key.document).requireValue()).requireValue()
        val command = """{"commandId":"http-edit","expectedRevision":0,"operations":[{"operationType":"putProperty","nodeId":"label","name":"text","value":"after restart"}]}"""
        HttpClient.newHttpClient().use { client ->
            fun request(host: DraftHttpHost, method: String, suffix: String = "", body: String = ""): HttpResponse<String> =
                client.send(HttpRequest.newBuilder(URI("http://127.0.0.1:${host.port}$path$suffix"))
                    .timeout(Duration.ofSeconds(10)).header("Authorization", "Bearer $token")
                    .header("Content-Type", "application/json").method(method, HttpRequest.BodyPublishers.ofString(body)).build(),
                    HttpResponse.BodyHandlers.ofString())
            host().use {
                assertEquals(201, request(it, "POST", body = source).statusCode())
                val edit = request(it, "POST", "/commands", command)
                assertEquals(200, edit.statusCode())
                assertEquals(JsonPrimitive(1), Json.parseToJsonElement(edit.body()).jsonObject["revision"])
            }
            host().use {
                val read = request(it, "GET")
                assertEquals(200, read.statusCode())
                assertEquals(JsonPrimitive(1), Json.parseToJsonElement(read.body()).jsonObject["revision"])
                assertTrue(read.body().contains("after restart"))
                val replay = Json.parseToJsonElement(request(it, "POST", "/commands", command).body()).jsonObject
                assertEquals(JsonPrimitive(true), replay["replayed"])
                assertEquals(JsonPrimitive(1), replay["appliedRevision"])
                val conflict = request(it, "POST", "/commands", command.replace("http-edit", "another"))
                assertEquals(409, conflict.statusCode())
            }
        }
    }
}
