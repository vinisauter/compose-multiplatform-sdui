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
    @Test fun twoIndependentHttpHostsShareRevisionJournalPresenceAndIdempotentUndo() {
        val url = requireNotNull(System.getenv("HUMMING_TEST_DB_URL"))
        val uri = URI(url.removePrefix("jdbc:"))
        require(uri.host in setOf("127.0.0.1", "localhost") && uri.path == "/humming_test")
        val catalog = initialNodeCatalog()
        fun repository() = PostgresDraftRepository(postgresDataSource(url,
            requireNotNull(System.getenv("HUMMING_TEST_DB_USER")), requireNotNull(System.getenv("HUMMING_TEST_DB_PASSWORD"))), catalog)
        val key = DocumentKey("replicas", "doc-${UUID.randomUUID()}")
        val grants = Permission.entries.map { DocumentGrant(key, it) }.toSet()
        val a = Actor("alice", grants); val b = Actor("bob", grants)
        val tokenA = UUID.randomUUID().toString(); val tokenB = UUID.randomUUID().toString()
        fun host(actor: Actor, token: String): DraftHttpHost {
            val repo = repository(); runBlocking { repo.migrate() }
            return DraftHttpHost(DraftApplication(repo, ExactGrantAuthorizer, catalog, TargetCapabilities(catalog.capabilities)),
                BearerAuthenticator(token, actor), editing = DraftEditingApplication(repo, ExactGrantAuthorizer),
                collaboration = CollaborationApplication(repo, ExactGrantAuthorizer))
        }
        val path = "/v1/tenants/${key.tenant}/documents/${key.document}"
        val source = AuthoringDocumentCodec(catalog).encode(LegacyAuthoringCodec(catalog).importDocument(
            """{"id":"label","component":"layout:text","properties":{"text":"initial"}}""", key.document).requireValue()).requireValue()
        fun command(id: String) = """{"commandId":"$id","expectedRevision":0,"operations":[{"operationType":"putProperty","nodeId":"label","name":"text","value":"$id"}]}"""
        fun request(host: DraftHttpHost, token: String, method: String, suffix: String = "", body: String = "") =
            HttpRequest.newBuilder(URI("http://127.0.0.1:${host.port}$path$suffix")).timeout(Duration.ofSeconds(15))
                .header("Authorization", "Bearer $token").header("Content-Type", "application/json")
                .method(method, HttpRequest.BodyPublishers.ofString(body)).build()
        host(a, tokenA).use { hostA -> host(b, tokenB).use { hostB ->
            HttpClient.newHttpClient().use { clientA -> HttpClient.newHttpClient().use { clientB ->
                fun send(client: HttpClient, host: DraftHttpHost, token: String, method: String = "GET", suffix: String = "", body: String = "") =
                    client.send(request(host, token, method, suffix, body), HttpResponse.BodyHandlers.ofString())
                assertEquals(201, send(clientA, hostA, tokenA, "POST", body = source).statusCode())
                assertEquals(200, send(clientB, hostB, tokenB).statusCode())
                val first = clientA.sendAsync(request(hostA, tokenA, "POST", "/commands", command("alice-command")), HttpResponse.BodyHandlers.ofString())
                val second = clientB.sendAsync(request(hostB, tokenB, "POST", "/commands", command("bob-command")), HttpResponse.BodyHandlers.ofString())
                val results = listOf(first.get(), second.get()); assertEquals(listOf(200, 409), results.map { it.statusCode() }.sorted())
                val aliceWon = results.first().statusCode() == 200
                val winnerHost = if (aliceWon) hostA else hostB; val winnerToken = if (aliceWon) tokenA else tokenB
                val winnerClient = if (aliceWon) clientA else clientB; val winnerId = if (aliceWon) "alice-command" else "bob-command"
                val feedA = send(clientA, hostA, tokenA, suffix = "/updates/0")
                val feedB = send(clientB, hostB, tokenB, suffix = "/updates/0")
                assertEquals(200, feedA.statusCode()); assertEquals(feedA.body(), feedB.body())
                val events = DraftUpdateFeedCodec().decode(feedA.body()).requireValue()
                assertEquals(1, events.currentRevision); assertEquals(winnerId, events.events.single().commandId)
                assertEquals(feedA.body(), send(clientA, hostA, tokenA, suffix = "/updates/0").body()) // Lost/duplicate poll cannot create another event.
                assertEquals(200, send(clientA, hostA, tokenA, "POST", "/presence").statusCode())
                val presence = send(clientB, hostB, tokenB, "POST", "/presence")
                assertEquals(setOf("alice", "bob"), Json.decodeFromString(DraftPresenceList.serializer(), presence.body()).members.map { it.actorId }.toSet())
                val undo = """{"commandId":"undo-winner","expectedRevision":1,"undoOfCommandId":"$winnerId"}"""
                val compensated = send(winnerClient, winnerHost, winnerToken, "POST", "/undo", undo)
                assertEquals(200, compensated.statusCode())
                val repeated = send(winnerClient, winnerHost, winnerToken, "POST", "/undo", undo)
                assertEquals(JsonPrimitive(true), Json.parseToJsonElement(repeated.body()).jsonObject["replayed"])
                val latest = send(clientB, hostB, tokenB)
                assertEquals(JsonPrimitive(2), Json.parseToJsonElement(latest.body()).jsonObject["revision"])
                assertTrue(latest.body().contains("initial"))
                val tail = DraftUpdateFeedCodec().decode(send(clientA, hostA, tokenA, suffix = "/updates/1").body()).requireValue()
                assertEquals(listOf(2L), tail.events.map { it.revision })
                assertTrue(DraftUpdateFeedCodec().decode(send(clientA, hostA, tokenA, suffix = "/updates/99").body()).requireValue().resyncRequired)
                assertEquals(200, send(clientB, hostB, tokenB, "POST", "/presence/leave").statusCode())
                assertEquals(setOf("alice"), Json.decodeFromString(DraftPresenceList.serializer(), send(clientA, hostA, tokenA, "POST", "/presence").body()).members.map { it.actorId }.toSet())
            } }
        } }
    }

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
                BearerAuthenticator(token, actor), editing = DraftEditingApplication(repo, ExactGrantAuthorizer),
                publications = publicationApplication(repo, repo, catalog))
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
                val published = request(it, "POST", "/publications", """{"expectedRevision":1,"profile":"local-tree"}""")
                assertEquals(201, published.statusCode())
                val snapshot = PublishedContractsCodec().decode(published.body()).requireValue()
                assertEquals(200, request(it, "POST", "/publications/${snapshot.version}/select").statusCode())
                assertEquals(published.body(), request(it, "GET", "/publications/${snapshot.version}/clients/local-tree").body())
                assertEquals(422, request(it, "GET", "/publications/active/clients/empty-client").statusCode())
                for (body in listOf("{}", """{"expectedRevision":[],"profile":"local-tree"}""", """{"expectedRevision":1,"profile":"local-tree","state":{"secret":true}}"""))
                    assertEquals(422, request(it, "POST", "/publications", body).statusCode())
            }
            host().use {
                val read = request(it, "GET")
                assertEquals(200, read.statusCode())
                assertEquals(JsonPrimitive(1), Json.parseToJsonElement(read.body()).jsonObject["revision"])
                assertTrue(read.body().contains("after restart"))
                val active = request(it, "GET", "/publications/active/clients/local-tree")
                assertEquals(200, active.statusCode())
                val pinned = PublishedContractsCodec().decode(active.body()).requireValue()
                assertEquals(1, pinned.contracts.revision)
                assertEquals(ApplicationError.REVISION_CONFLICT.name, Json.parseToJsonElement(request(it, "POST", "/publications", """{"expectedRevision":0,"profile":"local-tree"}""").body()).jsonObject["code"]?.jsonPrimitive?.content)
                val replay = Json.parseToJsonElement(request(it, "POST", "/commands", command).body()).jsonObject
                assertEquals(JsonPrimitive(true), replay["replayed"])
                assertEquals(JsonPrimitive(1), replay["appliedRevision"])
                val conflict = request(it, "POST", "/commands", command.replace("http-edit", "another"))
                assertEquals(409, conflict.statusCode())
            }
        }
    }
}
