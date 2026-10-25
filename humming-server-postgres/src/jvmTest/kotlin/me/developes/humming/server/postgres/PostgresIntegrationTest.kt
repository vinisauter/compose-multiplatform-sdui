package me.developes.humming.server.postgres

import kotlinx.coroutines.*
import kotlinx.serialization.json.JsonPrimitive
import me.developes.humming.server.*
import me.developes.humming.sdui.common.*
import me.developes.humming.sdui.studio.*
import java.net.URI
import java.sql.SQLException
import java.util.UUID
import javax.sql.DataSource
import kotlin.test.*

class PostgresIntegrationTest {
    private val catalog = initialNodeCatalog()
    private val codec = AuthoringDocumentCodec(catalog)
    private fun dataSource(): DataSource {
        val url = requireNotNull(System.getenv("HUMMING_TEST_DB_URL"))
        val uri = URI(url.removePrefix("jdbc:"))
        require(uri.host in setOf("127.0.0.1", "localhost") && uri.path == "/humming_test") { "Integration requires an explicitly named local humming_test database" }
        return postgresDataSource(url, requireNotNull(System.getenv("HUMMING_TEST_DB_USER")), requireNotNull(System.getenv("HUMMING_TEST_DB_PASSWORD")))
    }
    private fun key(tenant: String = "tenant-a") = DocumentKey(tenant, "doc-${UUID.randomUUID()}")
    private fun source(key: DocumentKey) = codec.encode(LegacyAuthoringCodec(catalog).importDocument(
        """{"id":"label","component":"layout:text","properties":{"text":"initial"}}""", key.document).requireValue()).requireValue()
    private fun command(id: String, revision: Long, text: String) =
        """{"commandId":"$id","expectedRevision":$revision,"operations":[{"operationType":"putProperty","nodeId":"label","name":"text","value":"$text"}]}"""
    private fun actor(key: DocumentKey) = Actor("author", Permission.entries.map { DocumentGrant(key, it) }.toSet())

    @Test fun twoAdaptersReplayJournalAndGuardUndoWithoutErasingAPeersChanges() = runBlocking {
        val ds = dataSource(); val first = PostgresDraftRepository(ds, catalog); first.migrate()
        val second = PostgresDraftRepository(dataSource(), catalog); second.migrate()
        val key = key()
        val base = LegacyAuthoringCodec(catalog).importDocument(
            """{"id":"root","component":"layout:column","children":[{"id":"left","component":"layout:text","properties":{"text":"left initial"}},{"id":"right","component":"layout:text","properties":{"text":"right initial"}}]}""", key.document).requireValue()
        val source = codec.encode(base).requireValue(); assertTrue(first.create(StoredDraft(key, 0, source)))
        val a = Actor("alice", actor(key).grants); val b = Actor("bob", actor(key).grants)
        val editA = DraftEditingApplication(first, ExactGrantAuthorizer); val editB = DraftEditingApplication(second, ExactGrantAuthorizer)
        val collabA = CollaborationApplication(first, ExactGrantAuthorizer); val collabB = CollaborationApplication(second, ExactGrantAuthorizer)
        fun edit(id: String, rev: Long, node: String, text: String) = """{"commandId":"$id","expectedRevision":$rev,"operations":[{"operationType":"putProperty","nodeId":"$node","name":"text","value":"$text"}]}"""
        fun undo(id: String, rev: Long, original: String) = """{"commandId":"$id","expectedRevision":$rev,"undoOfCommandId":"$original"}"""
        assertIs<DraftEditResult.Accepted>(editA.edit(a, key, edit("alice-left", 0, "left", "alice changed")))
        assertIs<DraftEditResult.Accepted>(editB.edit(b, key, edit("bob-right", 1, "right", "bob changed")))
        val feed = assertIs<ApplicationResult.Success<DraftUpdateFeed>>(collabB.updates(b, key, 0)).value
        assertEquals(listOf(1L, 2L), feed.events.map { it.revision }); assertEquals(listOf("alice", "bob"), feed.events.map { it.actorId })
        val clientA = RemoteEditor.open(RemoteDocument("server", key.tenant, key.document), source, catalog).requireValue()
        val clientB = RemoteEditor.open(RemoteDocument("server", key.tenant, key.document), source, catalog).requireValue()
        assertFalse(clientB.receiveUpdates(feed.copy(events = feed.events.reversed())).isSuccess)
        assertEquals(0, clientB.document.revision)
        assertFalse(clientB.receiveUpdates(feed.copy(events = feed.events.drop(1))).isSuccess)
        assertTrue(clientA.receiveUpdates(feed).requireValue()); assertTrue(clientB.receiveUpdates(feed).requireValue())
        assertEquals(clientA.document, clientB.document); assertFalse(clientB.receiveUpdates(feed).requireValue())
        assertEquals(codec.decode(first.find(key)!!.source).requireValue(), clientA.document)
        assertEquals(ApplicationError.FORBIDDEN, assertIs<DraftEditResult.Rejected>(collabB.undo(b, key, undo("forbidden", 2, "alice-left"))).code)
        val undone = assertIs<DraftEditResult.Accepted>(collabA.undo(a, key, undo("undo-alice", 2, "alice-left")))
        assertEquals(3, undone.appliedRevision)
        val after = codec.decode(undone.draft.source).requireValue()
        assertEquals(JsonPrimitive("left initial"), after.nodes.getValue("left").properties["text"])
        assertEquals(JsonPrimitive("bob changed"), after.nodes.getValue("right").properties["text"])
        val replay = assertIs<DraftEditResult.Accepted>(collabB.undo(a, key, undo("undo-alice", 2, "alice-left")))
        assertTrue(replay.replayed); assertEquals(3, replay.appliedRevision)
        assertEquals(ApplicationError.UNDO_CONFLICT, assertIs<DraftEditResult.Rejected>(collabA.undo(a, key, undo("again", 3, "alice-left"))).code)
        val next = assertIs<ApplicationResult.Success<DraftUpdateFeed>>(collabA.updates(a, key, 2)).value
        clientA.receiveUpdates(next).requireValue(); clientB.receiveUpdates(next).requireValue(); assertEquals(after, clientB.document)
        assertIs<DraftEditResult.Accepted>(editA.edit(a, key, edit("alice-again", 3, "left", "alice newer")))
        assertIs<DraftEditResult.Accepted>(editB.edit(b, key, edit("bob-same-node", 4, "left", "bob newest")))
        assertEquals(ApplicationError.UNDO_CONFLICT, assertIs<DraftEditResult.Rejected>(collabA.undo(a, key, undo("must-not-erase", 5, "alice-again"))).code)
        assertEquals(JsonPrimitive("bob newest"), codec.decode(first.find(key)!!.source).requireValue().nodes.getValue("left").properties["text"])
        assertEquals(5, first.find(key)?.revision)
        assertEquals(ApplicationError.FORBIDDEN, assertIs<ApplicationResult.Failure>(collabA.updates(a, key.copy(tenant = "other"), 0)).code)
        assertFailsWith<SQLException> { ds.connection.use { connection -> connection.prepareStatement("UPDATE public.humming_edit_events SET actor_id=actor_id WHERE tenant_id=? AND document_id=?").use {
            it.setString(1, key.tenant); it.setString(2, key.document); it.executeUpdate()
        } } }
        Unit
    }

    @Test fun journalPagesAndPresenceAreSharedAndLegacyGapsRequireResynchronization() = runBlocking {
        val ds = dataSource(); val first = PostgresDraftRepository(ds, catalog); first.migrate()
        val second = PostgresDraftRepository(dataSource(), catalog)
        val key = key(); assertTrue(first.create(StoredDraft(key, 0, source(key))))
        val a = Actor("alice", actor(key).grants); val b = Actor("bob", actor(key).grants)
        val editor = DraftEditingApplication(first, ExactGrantAuthorizer)
        for (revision in 0L..34L) assertIs<DraftEditResult.Accepted>(editor.edit(a, key, command("page-$revision", revision, "revision-$revision")))
        val page = second.updates(key, 0)!!
        assertEquals(32, page.events.size); assertTrue(page.hasMore); assertFalse(page.resyncRequired)
        val tail = first.updates(key, 32)!!; assertEquals(listOf(33L, 34L, 35L), tail.events.map { it.revision }); assertFalse(tail.hasMore)
        assertEquals(second.updates(key, 0), first.updates(key, 0))
        val collabA = CollaborationApplication(first, ExactGrantAuthorizer); val collabB = CollaborationApplication(second, ExactGrantAuthorizer)
        assertEquals(setOf("alice"), assertIs<ApplicationResult.Success<DraftPresenceList>>(collabA.heartbeat(a, key)).value.members.map { it.actorId }.toSet())
        assertEquals(setOf("alice", "bob"), assertIs<ApplicationResult.Success<DraftPresenceList>>(collabB.heartbeat(b, key)).value.members.map { it.actorId }.toSet())
        ds.connection.use { connection -> connection.prepareStatement("UPDATE public.humming_presence SET expires_at=clock_timestamp()-interval '1 second' WHERE tenant_id=? AND document_id=? AND actor_id='alice'").use {
            it.setString(1, key.tenant); it.setString(2, key.document); it.executeUpdate()
        } }
        assertEquals(setOf("bob"), assertIs<ApplicationResult.Success<DraftPresenceList>>(collabB.heartbeat(b, key)).value.members.map { it.actorId }.toSet())
        collabB.leave(b, key)
        assertEquals(setOf("alice"), assertIs<ApplicationResult.Success<DraftPresenceList>>(collabA.heartbeat(a, key)).value.members.map { it.actorId }.toSet())
        val legacy = key()
        val old = codec.decode(source(legacy)).requireValue().copy(revision = 5)
        assertTrue(first.create(StoredDraft(legacy, 5, codec.encode(old).requireValue())))
        assertTrue(second.updates(legacy, 0)!!.resyncRequired)
        assertFalse(second.updates(legacy, 5)!!.resyncRequired)
        assertTrue(second.updates(legacy, 6)!!.resyncRequired)
    }

    @Test fun publicationIsContentAddressedImmutableAndRollbackOnlySelectsExistingContent() = runBlocking {
        val ds = dataSource(); val repo = PostgresDraftRepository(ds, catalog); repo.migrate(); repo.migrate()
        val key = key(); val author = actor(key)
        assertTrue(repo.create(StoredDraft(key, 0, source(key))))
        val digest = PublicationDigest { text -> java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(text.toByteArray(Charsets.UTF_8))) }
        fun application(repository: PostgresDraftRepository) = PublicationApplication(repository, repository, ExactGrantAuthorizer, catalog,
            listOf(PublicationProfile("full", TargetCapabilities(catalog.capabilities)), PublicationProfile("old", TargetCapabilities(emptyMap()))), digest)
        val app = application(repo)
        val first = assertIs<ApplicationResult.Success<StoredPublication>>(app.publish(author, key, 0, "full")).value
        assertEquals(first, assertIs<ApplicationResult.Success<StoredPublication>>(app.publish(author, key, 0, "full")).value)
        assertNull(repo.activePublication(key)) // Publishing and rollout are separate explicit operations.
        assertIs<ApplicationResult.Success<String>>(app.select(author, key, first.version))
        assertEquals(first, assertIs<ApplicationResult.Success<StoredPublication>>(app.read(author, key, null, "full")).value)
        val incompatible = assertIs<ApplicationResult.Failure>(app.read(author, key, first.version, "old"))
        assertEquals(ApplicationError.INCOMPATIBLE_CLIENT, incompatible.code)
        assertTrue(incompatible.diagnostics.single().path.contains("layout:text"))
        assertEquals(ApplicationError.FORBIDDEN, assertIs<ApplicationResult.Failure>(app.publish(Actor("denied", emptySet()), key, 0, "full")).code)
        assertEquals(ApplicationError.FORBIDDEN, assertIs<ApplicationResult.Failure>(app.read(author, key.copy(tenant = "other"), first.version, "full")).code)
        assertIs<DraftEditResult.Accepted>(DraftEditingApplication(repo, ExactGrantAuthorizer).edit(author, key, command("second", 0, "second snapshot")))
        assertEquals(ApplicationError.REVISION_CONFLICT, assertIs<ApplicationResult.Failure>(app.publish(author, key, 0, "full")).code)
        // Even a candidate compiled before a concurrent edit is rejected inside the storage transaction.
        assertEquals(ApplicationError.REVISION_CONFLICT, assertIs<ApplicationResult.Failure>(repo.publish(first)).code)
        val second = assertIs<ApplicationResult.Success<StoredPublication>>(app.publish(author, key, 1, "full")).value
        assertNotEquals(first.version, second.version)
        assertEquals(first, repo.publication(key, first.version))
        assertIs<ApplicationResult.Success<String>>(app.select(author, key, second.version))
        assertEquals(second, repo.activePublication(key))
        assertEquals(ApplicationError.NOT_FOUND, assertIs<ApplicationResult.Failure>(app.select(author, key, "f".repeat(64))).code)
        assertEquals(second, repo.activePublication(key))
        val reopened = PostgresDraftRepository(dataSource(), catalog)
        assertIs<ApplicationResult.Success<String>>(application(reopened).select(author, key, first.version))
        assertEquals(first, reopened.activePublication(key))
        assertEquals(1, repo.find(key)?.revision) // Rollback never rewinds the draft.
        for (sql in listOf("UPDATE public.humming_publications SET source=source WHERE tenant_id=? AND document_id=?", "DELETE FROM public.humming_publications WHERE tenant_id=? AND document_id=?")) {
            assertFailsWith<SQLException> { ds.connection.use { connection -> connection.prepareStatement(sql).use {
                it.setString(1, key.tenant); it.setString(2, key.document); it.executeUpdate()
            } } }
        }
        assertEquals(first, reopened.publication(key, first.version))
    }

    @Test fun migrationsAreIdempotentAndNewAdaptersReopenThePersistedRevisionAndReceipt() = runBlocking {
        val repo = PostgresDraftRepository(dataSource(), catalog)
        repo.migrate(); repo.migrate()
        val key = key()
        val app = DraftApplication(repo, ExactGrantAuthorizer, catalog, TargetCapabilities(catalog.capabilities))
        assertIs<ApplicationResult.Success<StoredDraft>>(app.create(actor(key), key, source(key)))
        val edit = command("edit", 0, "persisted")
        val result = assertIs<DraftEditResult.Accepted>(DraftEditingApplication(repo, ExactGrantAuthorizer).edit(actor(key), key, edit))
        assertEquals(1, result.draft.revision)
        val reopened = PostgresDraftRepository(dataSource(), catalog)
        assertEquals(result.draft, reopened.find(key))
        val replay = assertIs<DraftEditResult.Accepted>(DraftEditingApplication(reopened, ExactGrantAuthorizer).edit(actor(key), key, edit))
        assertTrue(replay.replayed)
        assertEquals(1, replay.appliedRevision)
        assertEquals(1, replay.draft.revision)
    }

    @Test fun racingExpectedRevisionsHaveOneWinnerAndDuplicateCommandIsNotAppliedTwice() = runBlocking {
        val repo = PostgresDraftRepository(dataSource(), catalog); repo.migrate()
        val key = key(); assertTrue(repo.create(StoredDraft(key, 0, source(key))))
        val editor = DraftEditingApplication(repo, ExactGrantAuthorizer)
        val results = coroutineScope { (1..8).map { n -> async(Dispatchers.Default) { editor.edit(actor(key), key, command("command-$n", 0, "value-$n")) } }.awaitAll() }
        assertEquals(1, results.count { it is DraftEditResult.Accepted })
        assertEquals(7, results.count { it is DraftEditResult.Rejected && it.code == ApplicationError.REVISION_CONFLICT })
        assertEquals(1, repo.find(key)?.revision)
        val next = command("same-command", 1, "once")
        val duplicates = coroutineScope { (1..4).map { async(Dispatchers.Default) { editor.edit(actor(key), key, next) } }.awaitAll() }
        assertTrue(duplicates.all { it is DraftEditResult.Accepted && it.appliedRevision == 2L })
        assertEquals(1, duplicates.count { it is DraftEditResult.Accepted && !it.replayed })
        assertEquals(2, repo.find(key)?.revision)
        val conflict = editor.edit(actor(key), key, command("same-command", 2, "different"))
        assertEquals(ApplicationError.COMMAND_REPLAY_CONFLICT, assertIs<DraftEditResult.Rejected>(conflict).code)
    }

    @Test fun tenantsAndDocumentsHaveSeparateReceiptsAndDeniedCallsDoNotChangeOtherTenant() = runBlocking {
        val repo = PostgresDraftRepository(dataSource(), catalog); repo.migrate()
        val a = key(); val b = a.copy(tenant = "tenant-b")
        for (key in listOf(a, b)) assertTrue(repo.create(StoredDraft(key, 0, source(key))))
        val editor = DraftEditingApplication(repo, ExactGrantAuthorizer)
        assertEquals(ApplicationError.FORBIDDEN, assertIs<DraftEditResult.Rejected>(editor.edit(actor(a), b, "{")).code)
        assertIs<DraftEditResult.Accepted>(editor.edit(actor(a), a, command("shared-id", 0, "tenant-a")))
        assertIs<DraftEditResult.Accepted>(editor.edit(actor(b), b, command("shared-id", 0, "tenant-b")))
        assertEquals(JsonPrimitive("tenant-a"), codec.decode(repo.find(a)!!.source).requireValue().nodes.getValue("label").properties["text"])
        assertEquals(JsonPrimitive("tenant-b"), codec.decode(repo.find(b)!!.source).requireValue().nodes.getValue("label").properties["text"])
    }

    @Test fun invalidBatchAndReceiptFailureRollBackWithoutConsumingCommandIdentity() = runBlocking {
        val dataSource = dataSource()
        val repo = PostgresDraftRepository(dataSource, catalog); repo.migrate()
        val key = key(); val initial = StoredDraft(key, 0, source(key)); assertTrue(repo.create(initial))
        val editor = DraftEditingApplication(repo, ExactGrantAuthorizer)
        val invalid = """{"commandId":"invalid","expectedRevision":0,"operations":[{"operationType":"putProperty","nodeId":"label","name":"text","value":"never"},{"operationType":"removeNode","nodeId":"missing"}]}"""
        assertIs<DraftEditResult.Rejected>(editor.edit(actor(key), key, invalid))
        assertEquals(initial, repo.find(key))
        // Test-owned trigger in a dedicated local test database forces failure after UPDATE and before receipt commit.
        dataSource.connection.use { connection -> connection.createStatement().use { statement ->
            statement.execute("CREATE FUNCTION public.humming_fail_receipt_probe() RETURNS trigger LANGUAGE plpgsql AS 'BEGIN IF NEW.command_id = ''rollback_probe'' THEN RAISE EXCEPTION ''intentional test failure''; END IF; RETURN NEW; END;'")
            statement.execute("CREATE TRIGGER humming_fail_receipt_probe BEFORE INSERT ON public.humming_edit_receipts FOR EACH ROW EXECUTE FUNCTION public.humming_fail_receipt_probe()")
        } }
        try {
            assertFailsWith<SQLException> { editor.edit(actor(key), key, command("rollback_probe", 0, "not committed")) }
            assertEquals(initial, repo.find(key))
        } finally {
            dataSource.connection.use { connection -> connection.createStatement().use {
                it.execute("DROP TRIGGER humming_fail_receipt_probe ON public.humming_edit_receipts")
                it.execute("DROP FUNCTION public.humming_fail_receipt_probe()")
            } }
        }
        assertIs<DraftEditResult.Accepted>(editor.edit(actor(key), key, command("rollback_probe", 0, "committed")))
        assertEquals(1, repo.find(key)?.revision)
    }

    @Test fun cancellationWhileWaitingForRealRowLockRollsBackAndDoesNotWriteReceipt() = runBlocking {
        val dataSource = dataSource()
        val repo = PostgresDraftRepository(dataSource, catalog); repo.migrate()
        val key = key(); val initial = StoredDraft(key, 0, source(key)); assertTrue(repo.create(initial))
        val editor = DraftEditingApplication(repo, ExactGrantAuthorizer)
        dataSource.connection.use { blocker ->
            blocker.autoCommit = false
            blocker.prepareStatement("SELECT revision FROM public.humming_drafts WHERE tenant_id=? AND document_id=? FOR UPDATE").use {
                it.setString(1, key.tenant); it.setString(2, key.document)
                it.executeQuery().use { rows -> check(rows.next()) }
            }
            val pending = launch(Dispatchers.Default) { editor.edit(actor(key), key, command("cancelled", 0, "must roll back")) }
            try {
                withTimeout(5000) {
                    while (true) {
                        val waiting = dataSource.connection.use { connection -> connection.createStatement().use { statement ->
                            statement.executeQuery("SELECT EXISTS (SELECT 1 FROM pg_stat_activity WHERE application_name='humming-drafts' AND wait_event_type='Lock' AND query LIKE 'SELECT revision,source FROM public.humming_drafts%')").use {
                                rows -> check(rows.next()); rows.getBoolean(1)
                            }
                        } }
                        if (waiting) break
                        delay(20)
                    }
                }
                pending.cancel()
                blocker.rollback()
                pending.join()
                assertTrue(pending.isCancelled)
            } finally { pending.cancel(); blocker.rollback(); pending.join() }
        }
        assertEquals(initial, repo.find(key))
        dataSource.connection.use { connection -> connection.prepareStatement(
            "SELECT count(*) FROM public.humming_edit_receipts WHERE tenant_id=? AND document_id=? AND command_id='cancelled'").use {
            it.setString(1, key.tenant); it.setString(2, key.document)
            it.executeQuery().use { rows -> check(rows.next()); assertEquals(0, rows.getInt(1)) }
        } }
    }
}
