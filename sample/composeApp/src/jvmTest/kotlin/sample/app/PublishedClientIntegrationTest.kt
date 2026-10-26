package sample.app

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.key
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.test.*
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import kotlinx.coroutines.runBlocking
import me.developes.humming.server.*
import me.developes.humming.server.host.*
import me.developes.humming.server.postgres.*
import me.developes.humming.sdui.HummingSDUI
import me.developes.humming.sdui.action.SDActions
import me.developes.humming.sdui.common.*
import me.developes.humming.sdui.layout.SDLayoutMaterial3
import me.developes.humming.sdui.layout.components.Material3Theme
import me.developes.humming.sdui.studio.*
import org.junit.After
import org.junit.Rule
import org.junit.Test
import java.net.URI
import java.net.http.*
import java.time.Duration
import java.util.UUID
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlinx.serialization.json.*
import kotlin.test.*

/** Opt-in real database test. No fixture implementation stands in for PostgreSQL, HTTP or the renderer. */
class PublishedClientIntegrationTest {
    @get:Rule val compose = createComposeRule()

    @Test fun officialCatalogRunsHttpPublicationAndLiveBffDataWithoutChangingTheSnapshot() {
        val url = requireNotNull(System.getenv("HUMMING_TEST_DB_URL"))
        val catalog = initialNodeCatalog()
        val repo = PostgresDraftRepository(postgresDataSource(url, requireNotNull(System.getenv("HUMMING_TEST_DB_USER")),
            requireNotNull(System.getenv("HUMMING_TEST_DB_PASSWORD"))), catalog)
        val key = DocumentKey("catalog", "doc-${UUID.randomUUID()}")
        val actor = Actor("catalog-test", Permission.entries.map { DocumentGrant(key, it) }.toSet())
        val token = UUID.randomUUID().toString()
        val publisher = publicationApplication(repo, repo, catalog)
        runBlocking { repo.migrate() }
        DraftHttpHost(DraftApplication(repo, ExactGrantAuthorizer, catalog, TargetCapabilities(catalog.capabilities)),
            BearerAuthenticator(token, actor), publications = publisher, data = catalogDataApplication(publisher),
            dataIdentity = { DataIdentity(it, "catalog-test-session") }).use { host ->
            val client = io.ktor.client.HttpClient(io.ktor.client.engine.okhttp.OkHttp) { followRedirects = false; expectSuccess = false }
            try {
                val services = CatalogServices(client, key.document,
                    "http://127.0.0.1:${host.port}/v1/tenants/${key.tenant}/documents/${key.document}", token)
                compose.setContent { if (mounted.value) MaterialTheme { Surface {
                    androidx.compose.foundation.layout.Box { CatalogServicesPanel(services) }
                } } }
                compose.onNodeWithText("Consultar rascunho por HTTP SDUI").performClick()
                compose.waitUntil(15_000) { compose.onAllNodesWithText("NETWORK_HTTP_STATUS", substring = true).fetchSemanticsNodes().isNotEmpty() }
                compose.onNodeWithText("Preparar e publicar").performClick()
                compose.waitUntil(15_000) { compose.onAllNodesWithText("Runtime do servidor: aguardando dados").fetchSemanticsNodes().size == 1 }
                val original = runBlocking { repo.find(key)!! }
                compose.onNodeWithText("Carregar dados BFF").performClick()
                compose.waitUntil(15_000) { compose.onAllNodesWithText("Runtime do servidor: ${System.getProperty("java.version")}").fetchSemanticsNodes().size == 1 }
                compose.onNodeWithTag("material-name").performTextReplacement("Local da sessão")
                compose.onNodeWithText("Salvar").performScrollTo().performClick()
                compose.onNodeWithText("Salvo").assertExists()
                assertEquals(original, runBlocking { repo.find(key) })
                val output = java.io.File("build/reports/official-catalog/catalog-services.png").also { it.parentFile.mkdirs() }
                org.jetbrains.skia.Image.makeFromBitmap(compose.onRoot().captureToImage().asSkiaBitmap()).use { image ->
                    requireNotNull(image.encodeToData(org.jetbrains.skia.EncodedImageFormat.PNG)).use { output.writeBytes(it.bytes) }
                }
            } finally { compose.runOnIdle { mounted.value = false }; compose.waitForIdle(); client.close() }
        }
    }
    private val mounted = mutableStateOf(true)
    private val owner = object : ViewModelStoreOwner, LifecycleOwner {
        override val viewModelStore = ViewModelStore()
        override val lifecycle = LifecycleRegistry(this)
    }
    @After fun cleanup() {
        compose.runOnIdle { mounted.value = false }
        compose.waitForIdle()
        compose.runOnIdle {
            if (owner.lifecycle.currentState == Lifecycle.State.INITIALIZED) owner.lifecycle.currentState = Lifecycle.State.CREATED
            owner.lifecycle.currentState = Lifecycle.State.DESTROYED; owner.viewModelStore.clear()
        }
    }

    private class AccountService : AutoCloseable {
        val failing = AtomicBoolean(true)
        private val executor = Executors.newFixedThreadPool(2)
        private val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 8).apply {
            this.executor = this@AccountService.executor
            createContext("/account") { exchange ->
                try {
                    val bytes = """{"name":"Real local service","private":"not selected"}""".toByteArray()
                    exchange.responseHeaders.set("Content-Type", "application/json")
                    exchange.sendResponseHeaders(if (failing.get()) 503 else 200, bytes.size.toLong())
                    exchange.responseBody.write(bytes)
                } finally { exchange.close() }
            }
            start()
        }
        val endpoint get() = URI("http://127.0.0.1:${server.address.port}/account")
        override fun close() { server.stop(0); executor.shutdownNow(); assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS)) }
    }

    @Test fun collaborativeControllersConvergeAndDurableUndoNeverErasesAPeer() {
        val url = requireNotNull(System.getenv("HUMMING_TEST_DB_URL"))
        val uri = URI(url.removePrefix("jdbc:"))
        require(uri.host in setOf("127.0.0.1", "localhost") && uri.path == "/humming_test")
        val catalog = initialNodeCatalog(); val codec = AuthoringDocumentCodec(catalog)
        fun repository() = PostgresDraftRepository(postgresDataSource(url, requireNotNull(System.getenv("HUMMING_TEST_DB_USER")),
            requireNotNull(System.getenv("HUMMING_TEST_DB_PASSWORD"))), catalog)
        val repoA = repository(); val repoB = repository()
        val document = DocumentKey("collaborative", "doc-${UUID.randomUUID()}")
        val grants = Permission.entries.map { DocumentGrant(document, it) }.toSet()
        val alice = Actor("alice", grants); val bob = Actor("bob", grants)
        val tokenA = UUID.randomUUID().toString(); val tokenB = UUID.randomUUID().toString()
        val source = codec.encode(LegacyAuthoringCodec(catalog).importDocument(
            """{"id":"root","component":"layout:column","children":[{"id":"left","component":"layout:text","properties":{"text":"left initial"}},{"id":"right","component":"layout:text","properties":{"text":"right initial"}}]}""", document.document).requireValue()).requireValue()
        val publisher = publicationApplication(repoA, repoA, catalog)
        val publication = runBlocking {
            repoA.migrate(); repoB.migrate(); assertTrue(repoA.create(StoredDraft(document, 0, source)))
            assertIs<ApplicationResult.Success<StoredPublication>>(publisher.publish(alice, document, 0, "local-tree")).value
        }
        fun server(repo: PostgresDraftRepository, actor: Actor, token: String) = DraftHttpHost(
            DraftApplication(repo, ExactGrantAuthorizer, catalog, TargetCapabilities(catalog.capabilities)), BearerAuthenticator(token, actor),
            editing = DraftEditingApplication(repo, ExactGrantAuthorizer), collaboration = CollaborationApplication(repo, ExactGrantAuthorizer))
        val identity = RemoteDocument("collaborative-server", document.tenant, document.document)
        val directory = Path.of("build", "reports", "collaborative-studio", document.document)
        val storeA = JvmRemoteStudioRecovery(directory.resolve("alice.json")); val storeB = JvmRemoteStudioRecovery(directory.resolve("bob.json"))
        fun awaitStatus(text: String) = compose.waitUntil(15_000) {
            compose.onAllNodes(hasTestTag("studio-remote-state") and hasText(text, substring = true)).fetchSemanticsNodes().size == 1
        }
        fun apply(text: String) {
            compose.onNodeWithTag("studio-node-left").performScrollTo().performClick()
            compose.onNodeWithTag("studio-property-value").performScrollTo().performTextReplacement(text)
            compose.onNodeWithText("Aplicar propriedade").performScrollTo().performClick()
            compose.onNodeWithText("Sincronizar pendências").performClick()
        }
        fun text(host: RemoteStudioHost, node: String) = host.workspace.document.nodes.getValue(node).properties["text"]
        server(repoA, alice, tokenA).use { serverA -> server(repoB, bob, tokenB).use { serverB ->
            JvmRemoteStudioApi("http://127.0.0.1:${serverA.port}", identity, tokenA).use { apiA ->
                JvmRemoteStudioApi("http://127.0.0.1:${serverB.port}", identity, tokenB).use { apiB ->
                    val a = runBlocking { RemoteStudioHost.open(identity, catalog, apiA, storeA).requireValue() }
                    val b = runBlocking { RemoteStudioHost.open(identity, catalog, apiB, storeB).requireValue() }
                    val current = mutableStateOf(a)
                    compose.setContent { if (mounted.value) key(current.value) { LocalStudio(workspace = current.value.workspace, remoteHost = current.value) } }
                    try {
                        apply("Alice left"); awaitStatus("Confirmada 1 • pendentes 0")
                        compose.runOnIdle { runBlocking { b.pollUpdates() }; assertEquals(JsonPrimitive("Alice left"), text(b, "left")) }
                        compose.onNodeWithTag("studio-follow-editors").performClick()
                        compose.waitUntil(10_000) { compose.onAllNodesWithText("colaboradores: alice, bob", substring = true).fetchSemanticsNodes().size == 1 }
                        compose.runOnIdle { runBlocking { b.workspace.putProperty("right", "text", JsonPrimitive("Bob right")); b.synchronize() } }
                        awaitStatus("Confirmada 2 • pendentes 0")
                        compose.onNodeWithText("Desfazer último envio").performClick(); awaitStatus("Confirmada 3 • pendentes 0")
                        compose.runOnIdle {
                            assertEquals(JsonPrimitive("left initial"), text(a, "left")); assertEquals(JsonPrimitive("Bob right"), text(a, "right"))
                            runBlocking { b.pollUpdates() }; assertEquals(a.workspace.document, b.workspace.document)
                        }
                        apply("Alice again"); awaitStatus("Confirmada 4 • pendentes 0")
                        compose.runOnIdle { runBlocking { b.pollUpdates(); b.workspace.putProperty("left", "text", JsonPrimitive("Bob newest")); b.synchronize() } }
                        awaitStatus("Confirmada 5 • pendentes 0")
                        compose.onNodeWithText("Desfazer último envio").performClick(); awaitStatus("CONFLICT")
                        compose.runOnIdle { assertEquals(JsonPrimitive("Bob newest"), text(a, "left")); assertTrue(a.editor.hasPendingUndo) }
                        compose.onNodeWithText("Arquivar e usar remoto").performClick(); compose.onNodeWithText("Arquivar e substituir").performClick()
                        awaitStatus("Confirmada 5 • pendentes 0")
                        apply("Alice safe"); awaitStatus("Confirmada 6 • pendentes 0")
                        // Persist the undo, commit it, then lose its response before changing the client object.
                        compose.runOnIdle { runBlocking {
                            a.editor.enqueueUndo(RemoteUndoCommand("lost-undo", 6, a.lastOwnCommandId!!)).requireValue(); assertTrue(a.saveRecovery())
                            val ticket = assertNotNull(a.editor.beginUndo())
                            assertIs<RemoteStudioSubmit.Acknowledged>(apiA.undo(ticket.command))
                            b.pollUpdates(); b.workspace.putProperty("right", "text", JsonPrimitive("Bob after undo")); b.synchronize()
                        } }
                        val recovered = runBlocking { RemoteStudioHost.open(identity, catalog, apiA, storeA).requireValue() }
                        assertTrue(recovered.editor.hasPendingUndo)
                        compose.runOnIdle { current.value = recovered }
                        compose.onNodeWithText("Sincronizar pendências").performClick(); awaitStatus("Confirmada 8 • pendentes 0")
                        compose.runOnIdle {
                            assertEquals(JsonPrimitive("Bob newest"), text(recovered, "left")); assertEquals(JsonPrimitive("Bob after undo"), text(recovered, "right"))
                            runBlocking { b.pollUpdates(); recovered.leavePresence(); b.leavePresence() }
                            assertEquals(recovered.workspace.document, b.workspace.document)
                        }
                        runBlocking { assertEquals(8, repoA.find(document)?.revision); assertEquals(publication, repoB.publication(document, publication.version)) }
                    } finally { compose.runOnIdle { mounted.value = false }; compose.waitForIdle() }
                }
            }
        } }
    }

    @Test fun studioInspectorSavesReopensOfflineWorkAndArchivesAConflictThroughTheRealAdapters() {
        val url = requireNotNull(System.getenv("HUMMING_TEST_DB_URL"))
        val uri = URI(url.removePrefix("jdbc:"))
        require(uri.host in setOf("127.0.0.1", "localhost") && uri.path == "/humming_test")
        val catalog = initialNodeCatalog()
        val repo = PostgresDraftRepository(postgresDataSource(url, requireNotNull(System.getenv("HUMMING_TEST_DB_USER")),
            requireNotNull(System.getenv("HUMMING_TEST_DB_PASSWORD"))), catalog)
        val document = DocumentKey("studio", "doc-${UUID.randomUUID()}")
        val actor = Actor("studio-author", Permission.entries.map { DocumentGrant(document, it) }.toSet())
        val token = UUID.randomUUID().toString()
        val codec = AuthoringDocumentCodec(catalog)
        val source = codec.encode(LegacyAuthoringCodec(catalog).importDocument(
            """{"id":"label","component":"layout:text","properties":{"text":"initial"}}""", document.document).requireValue()).requireValue()
        val publisher = publicationApplication(repo, repo, catalog)
        val snapshot = runBlocking {
            repo.migrate(); assertTrue(repo.create(StoredDraft(document, 0, source)))
            assertIs<ApplicationResult.Success<StoredPublication>>(publisher.publish(actor, document, 0, "local-tree")).value
        }
        fun server() = DraftHttpHost(DraftApplication(repo, ExactGrantAuthorizer, catalog, TargetCapabilities(catalog.capabilities)),
            BearerAuthenticator(token, actor), editing = DraftEditingApplication(repo, ExactGrantAuthorizer))
        val identity = RemoteDocument("studio-integration", document.tenant, document.document)
        val path = Path.of("build", "reports", "studio-remote", "${document.document}.json")
        val recovery = JvmRemoteStudioRecovery(path)
        fun origin(host: DraftHttpHost) = "http://127.0.0.1:${host.port}"
        fun apply(text: String) {
            compose.onNodeWithTag("studio-property-value").performScrollTo().performTextReplacement(text)
            compose.onNodeWithText("Aplicar propriedade").performScrollTo().performClick()
        }
        fun awaitStatus(text: String) = compose.waitUntil(15_000) {
            compose.onAllNodes(hasTestTag("studio-remote-state") and hasText(text, substring = true)).fetchSemanticsNodes().size == 1
        }
        val firstServer = server()
        try { JvmRemoteStudioApi(origin(firstServer), identity, token).use { api ->
            val first = runBlocking { RemoteStudioHost.open(identity, catalog, api, recovery).requireValue() }
            val current = mutableStateOf(first)
            compose.setContent { if (mounted.value) key(current.value) { LocalStudio(workspace = current.value.workspace, remoteHost = current.value) } }
            apply("UI saved")
            compose.onNodeWithText("Sincronizar pendências").performClick()
            awaitStatus("Confirmada 1 • pendentes 0")
            runBlocking { assertTrue(repo.find(document)!!.source.contains("UI saved")) }
            firstServer.close()
            apply("UI offline")
            compose.onNodeWithText("Sincronizar pendências").performClick()
            awaitStatus("OFFLINE")
            assertFalse(Files.readString(path).contains(token))
            server().use { reopenedServer -> JvmRemoteStudioApi(origin(reopenedServer), identity, token).use { reopenedApi ->
                val reopened = runBlocking { RemoteStudioHost.open(identity, catalog, reopenedApi, recovery).requireValue() }
                assertEquals(1, reopened.editor.pendingCount)
                compose.runOnIdle { current.value = reopened }
                compose.onNodeWithText("Sincronizar pendências").performClick()
                awaitStatus("Confirmada 2 • pendentes 0")
                apply("UI conflict")
                runBlocking {
                    val edit = """{"commandId":"other-editor","expectedRevision":2,"operations":[{"operationType":"putProperty","nodeId":"label","name":"text","value":"other editor"}]}"""
                    assertIs<DraftEditResult.Accepted>(DraftEditingApplication(repo, ExactGrantAuthorizer).edit(actor, document, edit))
                }
                compose.onNodeWithText("Sincronizar pendências").performClick(); awaitStatus("CONFLICT")
                compose.runOnIdle { assertEquals(JsonPrimitive("UI conflict"), reopened.workspace.document.nodes.getValue("label").properties["text"]) }
                compose.onNodeWithText("Arquivar e usar remoto").performClick()
                compose.onNodeWithText("Arquivar e substituir").performClick()
                awaitStatus("Confirmada 3 • pendentes 0")
                compose.runOnIdle { assertEquals(JsonPrimitive("other editor"), reopened.workspace.document.nodes.getValue("label").properties["text"]) }
                Files.list(path.parent).use { files ->
                    val archive = files.filter { it.fileName.toString().startsWith(path.fileName.toString() + ".conflict-") }.findFirst().orElseThrow()
                    assertTrue(Files.readString(archive).contains("UI conflict")); assertFalse(Files.readString(archive).contains(token))
                }
                JvmRemoteStudioApi(origin(reopenedServer), identity, UUID.randomUUID().toString()).use { denied -> runBlocking {
                    val result = assertIs<RemoteStudioSubmit.Failed>(denied.submit(EditCommand("denied", 3,
                        listOf(EditOperation.PutProperty("label", "text", JsonPrimitive("never"))))))
                    assertEquals(RemotePause.UNAUTHORIZED, result.reason)
                    assertEquals(3, repo.find(document)?.revision); assertEquals(snapshot, repo.publication(document, snapshot.version))
                } }
            } }
        } } finally { firstServer.close() }
    }

    @Test fun durableOutboxReplaysALostHttpReceiptAndPreservesLocalWorkOnARevisionConflict() {
        val url = requireNotNull(System.getenv("HUMMING_TEST_DB_URL"))
        val uri = URI(url.removePrefix("jdbc:"))
        require(uri.host in setOf("127.0.0.1", "localhost") && uri.path == "/humming_test")
        val catalog = initialNodeCatalog()
        val repo = PostgresDraftRepository(postgresDataSource(url, requireNotNull(System.getenv("HUMMING_TEST_DB_USER")),
            requireNotNull(System.getenv("HUMMING_TEST_DB_PASSWORD"))), catalog)
        val document = DocumentKey("outbox", "doc-${UUID.randomUUID()}")
        val actor = Actor("outbox-author", Permission.entries.map { DocumentGrant(document, it) }.toSet())
        val token = UUID.randomUUID().toString()
        val codec = AuthoringDocumentCodec(catalog)
        val source = codec.encode(LegacyAuthoringCodec(catalog).importDocument(
            """{"id":"label","component":"layout:text","properties":{"text":"initial"}}""", document.document).requireValue()).requireValue()
        val publisher = publicationApplication(repo, repo, catalog)
        val snapshot = runBlocking {
            repo.migrate(); assertTrue(repo.create(StoredDraft(document, 0, source)))
            assertIs<ApplicationResult.Success<StoredPublication>>(publisher.publish(actor, document, 0, "local-tree")).value
        }
        fun host() = DraftHttpHost(DraftApplication(repo, ExactGrantAuthorizer, catalog, TargetCapabilities(catalog.capabilities)),
            BearerAuthenticator(token, actor), editing = DraftEditingApplication(repo, ExactGrantAuthorizer))
        val identity = RemoteDocument("integration-server", document.tenant, document.document)
        val json = Json { encodeDefaults = true; classDiscriminator = "operationType" }
        fun edit(id: String, revision: Long, text: String) = EditCommand(id, revision, listOf(EditOperation.PutProperty("label", "text", JsonPrimitive(text))))
        val recovery = Path.of("build", "reports", "remote-outbox", "${document.document}.json")
        Files.createDirectories(recovery.parent)
        fun persist(editor: RemoteEditor) {
            val checkpoint = editor.checkpoint().requireValue()
            val temporary = Files.createTempFile(recovery.parent, "checkpoint-", ".tmp")
            Files.writeString(temporary, checkpoint)
            Files.move(temporary, recovery, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
            assertTrue(editor.persisted(Files.readString(recovery)))
        }
        HttpClient.newHttpClient().use { client ->
            fun request(host: DraftHttpHost, command: EditCommand? = null): HttpResponse<String> = client.send(HttpRequest.newBuilder(
                URI("http://127.0.0.1:${host.port}/v1/tenants/${document.tenant}/documents/${document.document}" + if (command == null) "" else "/commands"))
                .timeout(Duration.ofSeconds(10)).header("Authorization", "Bearer $token").header("Content-Type", "application/json")
                .method(if (command == null) "GET" else "POST", if (command == null) HttpRequest.BodyPublishers.noBody()
                    else HttpRequest.BodyPublishers.ofString(json.encodeToString(EditCommand.serializer(), command))).build(), HttpResponse.BodyHandlers.ofString())
            fun acknowledge(editor: RemoteEditor, ticket: RemoteEditor.SendTicket, response: HttpResponse<String>) {
                assertEquals(200, response.statusCode())
                val result = Json.parseToJsonElement(response.body()).jsonObject
                assertTrue(editor.acknowledge(ticket, result.getValue("document").toString(), result.getValue("appliedRevision").jsonPrimitive.long).requireValue())
            }
            host().use { server ->
                val loaded = Json.parseToJsonElement(request(server).body()).jsonObject.getValue("document").toString()
                val editor = RemoteEditor.open(identity, loaded, catalog).requireValue()
                editor.enqueue(edit("one", 0, "first")); editor.enqueue(edit("two", 1, "second")); persist(editor)
                val ticket = assertNotNull(editor.beginSend())
                assertEquals(200, request(server, ticket.command).statusCode())
                // Lose the response deliberately: disk still contains both commands, while PostgreSQL committed one.
            }
            val recovered = RemoteEditor.restore(identity, Files.readString(recovery), catalog).requireValue()
            host().use { server ->
                val retry = assertNotNull(recovered.beginSend())
                val replay = request(server, retry.command)
                assertEquals(JsonPrimitive(true), Json.parseToJsonElement(replay.body()).jsonObject["replayed"])
                acknowledge(recovered, retry, replay); persist(recovered)
                val second = assertNotNull(recovered.beginSend())
                recovered.enqueue(edit("three", 2, "local-third")); persist(recovered)
                acknowledge(recovered, second, request(server, second.command))
                assertEquals(JsonPrimitive("local-third"), recovered.document.nodes.getValue("label").properties["text"])
                assertEquals(1, recovered.pendingCount); persist(recovered)
                assertEquals(200, request(server, edit("another-editor", 2, "other-third")).statusCode())
                val conflict = assertNotNull(recovered.beginSend())
                assertEquals(409, request(server, conflict.command).statusCode())
                assertTrue(recovered.failed(conflict, RemotePause.CONFLICT))
                val remote = Json.parseToJsonElement(request(server).body()).jsonObject.getValue("document").toString()
                recovered.observeConflict(remote).requireValue(); persist(recovered)
                val reopened = RemoteEditor.restore(identity, Files.readString(recovery), catalog).requireValue()
                assertEquals(RemotePause.CONFLICT, reopened.pause); assertNull(reopened.beginSend())
                assertEquals(1, reopened.pendingCount)
                assertEquals(JsonPrimitive("local-third"), reopened.document.nodes.getValue("label").properties["text"])
                assertEquals(JsonPrimitive("other-third"), codec.decode(reopened.observedRemote!!).requireValue().nodes.getValue("label").properties["text"])
                runBlocking {
                    assertEquals(3, repo.find(document)?.revision)
                    assertEquals(snapshot, repo.publication(document, snapshot.version))
                }
            }
        }
    }

    @Test fun fixedPublicationUsesRealServiceDataAndRecoversFromPartialFailureInANewEntry() {
        val url = requireNotNull(System.getenv("HUMMING_TEST_DB_URL"))
        val uri = URI(url.removePrefix("jdbc:"))
        require(uri.host in setOf("127.0.0.1", "localhost") && uri.path == "/humming_test")
        val catalog = initialNodeCatalog()
        val repo = PostgresDraftRepository(postgresDataSource(url, requireNotNull(System.getenv("HUMMING_TEST_DB_USER")),
            requireNotNull(System.getenv("HUMMING_TEST_DB_PASSWORD"))), catalog)
        val document = DocumentKey("data", "doc-${UUID.randomUUID()}")
        val actor = Actor("data-author", Permission.entries.map { DocumentGrant(document, it) }.toSet())
        val token = UUID.randomUUID().toString()
        val publisher = publicationApplication(repo, repo, catalog)
        val source = AuthoringDocumentCodec(catalog).encode(LegacyAuthoringCodec(catalog).importDocument(
            """{"id":"root","component":"layout:reactiveText","properties":{"value":{"version":1,"expression":{"language":"humming.expression","version":1,"source":"server.account.name ?? \"Data unavailable\""},"fallback":"Data unavailable"}}}""", document.document).requireValue()).requireValue()
        val snapshot = runBlocking {
            repo.migrate(); assertTrue(repo.create(StoredDraft(document, 0, source)))
            assertIs<ApplicationResult.Success<StoredPublication>>(publisher.publish(actor, document, 0, "local-tree")).value
        }
        AccountService().use { service -> HttpClient.newHttpClient().use { http ->
            val resolver = PublicationDataApplication(publisher, mapOf("account" to RegisteredHttpDataSource(http, service.endpoint)),
                listOf(DataPlan("screen", listOf(DataMapping("account", "account", mapOf("name" to DataField(listOf("name"), DataKind.TEXT)))))),
                DataSourceAuthorizer { context, _ -> context.document == document }, DataClock { System.nanoTime() / 1_000_000 })
            DraftHttpHost(DraftApplication(repo, ExactGrantAuthorizer, catalog, TargetCapabilities(catalog.capabilities)),
                BearerAuthenticator(token, actor), publications = publisher, data = resolver,
                dataIdentity = { DataIdentity(it, "authenticated-session") }).use { host ->
                fun data(): JsonObject = http.send(HttpRequest.newBuilder(URI("http://127.0.0.1:${host.port}/v1/tenants/${document.tenant}/documents/${document.document}/publications/${snapshot.version}/data/local-tree/screen"))
                    .timeout(Duration.ofSeconds(10)).header("Authorization", "Bearer $token").GET().build(), HttpResponse.BodyHandlers.ofString()).let {
                        assertEquals(200, it.statusCode()); assertEquals("no-store", it.headers().firstValue("Cache-Control").orElse("")); Json.parseToJsonElement(it.body()).jsonObject
                    }
                val failed = data()
                assertEquals("HTTP_STATUS", failed.getValue("issues").jsonArray.single().jsonObject.getValue("code").jsonPrimitive.content)
                val pinned = PublishedDefinition.decode(snapshot.source, snapshot.version, document.document, catalog, TargetCapabilities(catalog.capabilities)).requireValue()
                fun entry(id: String, values: JsonElement): SDUIFormViewModel = SDUIFormViewModel(SavedStateHandle(), listOf(SDLayoutMaterial3()),
                    screenInputs = ScreenInputs(server = parseSduiValue(values.toString()) as SduiValue.ObjectValue), screenEntryId = id).also { owner.viewModelStore.put(id, it) }
                compose.runOnIdle { owner.lifecycle.currentState = Lifecycle.State.RESUMED }
                val current = mutableStateOf(entry("failed", failed.getValue("values")))
                compose.setContent { if (mounted.value) MaterialTheme { Surface { key(current.value) { current.value.loadComponent(pinned.node(pinned.entryDestination))?.Content() } } } }
                compose.onNodeWithText("Data unavailable").assertExists()
                service.failing.set(false)
                val recovered = data()
                assertTrue(recovered.getValue("issues").jsonArray.isEmpty())
                assertFalse(recovered.toString().contains("not selected"))
                compose.runOnIdle { current.value = entry("recovered", recovered.getValue("values")) }
                compose.waitUntil(10_000) { compose.onAllNodesWithText("Real local service").fetchSemanticsNodes().size == 1 }
                assertEquals(snapshot.version, recovered.getValue("version").jsonPrimitive.content)
                runBlocking {
                    assertEquals(snapshot, repo.publication(document, snapshot.version))
                    assertFalse(repo.find(document)!!.source.contains("Real local service"))
                    assertFalse(snapshot.source.contains("Real local service"))
                }
            }
        } }
    }

    @Test fun httpSnapshotRendersAndLocalFormStateNeverChangesThePinnedVersionOrTheDatabase() {
        val url = requireNotNull(System.getenv("HUMMING_TEST_DB_URL"))
        val uri = URI(url.removePrefix("jdbc:"))
        require(uri.host in setOf("127.0.0.1", "localhost") && uri.path == "/humming_test")
        val catalog = initialNodeCatalog()
        val repo = PostgresDraftRepository(postgresDataSource(url, requireNotNull(System.getenv("HUMMING_TEST_DB_USER")),
            requireNotNull(System.getenv("HUMMING_TEST_DB_PASSWORD"))), catalog)
        val key = DocumentKey("client", "doc-${UUID.randomUUID()}")
        val actor = Actor("client-test-author", Permission.entries.map { DocumentGrant(key, it) }.toSet())
        val token = UUID.randomUUID().toString()
        val source = AuthoringDocumentCodec(catalog).encode(LegacyAuthoringCodec(catalog).importDocument(
            """{"id":"root","component":"layout:column","children":[{"id":"title","component":"layout:text","properties":{"text":"Publication one"}},{"id":"input","component":"layout:textField","properties":{"text":"initial","onChangeUpdateState":"privateField"}},{"id":"echo","component":"layout:text","properties":{"text":"Local: #{privateField}"}}]}""", key.document).requireValue()).requireValue()
        val publisher = publicationApplication(repo, repo, catalog)
        runBlocking { repo.migrate(); assertTrue(repo.create(StoredDraft(key, 0, source))) }
        DraftHttpHost(DraftApplication(repo, ExactGrantAuthorizer, catalog, TargetCapabilities(catalog.capabilities)),
            BearerAuthenticator(token, actor), publications = publisher).use { host ->
            HttpClient.newHttpClient().use { client ->
                val base = "http://127.0.0.1:${host.port}/v1/tenants/${key.tenant}/documents/${key.document}/publications"
                fun request(method: String, suffix: String = "", body: String = ""): HttpResponse<String> = client.send(
                    HttpRequest.newBuilder(URI(base + suffix)).timeout(Duration.ofSeconds(15))
                        .header("Authorization", "Bearer $token").header("Content-Type", "application/json")
                        .method(method, HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString())
                val published = request("POST", body = """{"expectedRevision":0,"profile":"local-tree"}""")
                assertEquals(201, published.statusCode())
                val snapshot = PublishedContractsCodec().decode(published.body()).requireValue()
                assertEquals(200, request("POST", "/${snapshot.version}/select").statusCode())
                val loaded = request("GET", "/active/clients/local-tree")
                assertEquals(200, loaded.statusCode())
                val pinned = PublishedDefinition.decode(loaded.body(), snapshot.version, key.document, catalog,
                    TargetCapabilities(catalog.capabilities)).requireValue()
                lateinit var vm: SDUIFormViewModel
                val config = HummingSDUI.Config(type = "published", destiny = pinned.entryDestination, theme = Material3Theme()) {
                    vm = this
                    addLibrary(SDActions()); addLibrary(SDLayoutMaterial3())
                    addNodeProvider("published", pinned::node)
                }
                compose.runOnIdle { owner.lifecycle.currentState = Lifecycle.State.RESUMED }
                compose.setContent {
                    if (mounted.value) CompositionLocalProvider(LocalViewModelStoreOwner provides owner, LocalLifecycleOwner provides owner) {
                        HummingSDUI().Content(config, savedStateHandleFactory = { SavedStateHandle() })
                    }
                }
                compose.waitUntil(10_000) { compose.onAllNodesWithText("Publication one").fetchSemanticsNodes().size == 1 }
                compose.onNode(hasSetTextAction()).performTextReplacement("private-session-value")
                compose.waitUntil(10_000) { compose.onAllNodesWithText("Local: private-session-value").fetchSemanticsNodes().size == 1 }
                compose.runOnIdle { assertEquals("private-session-value", vm.formState["privateField"]); assertNull(vm.errorState.value) }
                runBlocking {
                    val edit = """{"commandId":"new-title","expectedRevision":0,"operations":[{"operationType":"putProperty","nodeId":"title","name":"text","value":"Publication two"}]}"""
                    assertIs<DraftEditResult.Accepted>(DraftEditingApplication(repo, ExactGrantAuthorizer).edit(actor, key, edit))
                    val second = assertIs<ApplicationResult.Success<StoredPublication>>(publisher.publish(actor, key, 1, "local-tree")).value
                    assertNotEquals(snapshot.version, second.version)
                    assertIs<ApplicationResult.Success<String>>(publisher.select(actor, key, second.version))
                    assertEquals(snapshot.version, pinned.version)
                    assertEquals(loaded.body(), repo.publication(key, pinned.version)?.source)
                    assertFalse(repo.find(key)!!.source.contains("private-session-value"))
                    assertFalse(second.source.contains("private-session-value"))
                    assertIs<ApplicationResult.Success<String>>(publisher.select(actor, key, pinned.version))
                }
                compose.onNodeWithText("Publication one").assertExists()
                assertEquals(loaded.body(), request("GET", "/active/clients/local-tree").body())
            }
        }
    }
}
