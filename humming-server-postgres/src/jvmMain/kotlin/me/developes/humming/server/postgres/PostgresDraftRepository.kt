package me.developes.humming.server.postgres

import kotlinx.coroutines.*
import kotlinx.serialization.json.*
import me.developes.humming.server.*
import me.developes.humming.sdui.common.*
import me.developes.humming.sdui.studio.*
import java.sql.Connection
import java.security.MessageDigest
import java.util.HexFormat
import javax.sql.DataSource

/** No HTTP/auth/UI dependency. The composition root owns the datasource and its credentials. */
class PostgresDraftRepository(private val dataSource: DataSource, catalog: NodeCatalog) : RevisionedDraftRepository, PublicationRepository {
    private val catalog = catalog
    private val codec = AuthoringDocumentCodec(catalog)
    private val json = Json { encodeDefaults = true; classDiscriminator = "operationType" }

    suspend fun migrate() = transaction { connection ->
        connection.createStatement().use { statement ->
            statement.execute("SELECT pg_advisory_xact_lock(861739024)")
            statement.execute("CREATE TABLE IF NOT EXISTS public.humming_schema_migrations (version integer PRIMARY KEY, checksum char(64) NOT NULL)")
        }
        for ((version, resource) in listOf(1 to "/migrations/V1__drafts.sql", 2 to "/migrations/V2__publications.sql")) {
        val sql = checkNotNull(javaClass.getResource(resource)).readText().replace("\r\n", "\n")
        val checksum = hash(sql)
        val prior = connection.createStatement().use { statement ->
            statement.executeQuery("SELECT checksum FROM public.humming_schema_migrations WHERE version=$version").use { rows -> if (rows.next()) rows.getString(1) else null }
        }
        if (prior != null) check(prior == checksum) { "Migration checksum differs from the installed schema" }
        else {
            connection.createStatement().use { it.execute(sql) }
            connection.prepareStatement("INSERT INTO public.humming_schema_migrations(version,checksum) VALUES(?,?)").use {
                it.setInt(1, version); it.setString(2, checksum); it.executeUpdate()
            }
        }
        }
    }

    override suspend fun publish(snapshot: StoredPublication): ApplicationResult<StoredPublication> = transaction { connection ->
        val draft = find(connection, snapshot.key, lock = true)
            ?: return@transaction ApplicationResult.Failure(ApplicationError.NOT_FOUND)
        if (draft.revision != snapshot.revision) return@transaction ApplicationResult.Failure(ApplicationError.REVISION_CONFLICT)
        connection.prepareStatement("INSERT INTO public.humming_publications(tenant_id,document_id,version,revision,source) VALUES(?,?,?,?,?) ON CONFLICT DO NOTHING").use {
            it.setString(1, snapshot.key.tenant); it.setString(2, snapshot.key.document); it.setString(3, snapshot.version)
            it.setLong(4, snapshot.revision); it.setString(5, snapshot.source); it.executeUpdate()
        }
        val stored = checkNotNull(publication(connection, snapshot.key, snapshot.version))
        check(stored == snapshot) { "Immutable publication identity collision" }
        ApplicationResult.Success(stored)
    }
    override suspend fun publication(key: DocumentKey, version: String): StoredPublication? = transaction { publication(it, key, version) }
    private fun publication(connection: Connection, key: DocumentKey, version: String): StoredPublication? =
        connection.prepareStatement("SELECT revision,source FROM public.humming_publications WHERE tenant_id=? AND document_id=? AND version=?").use {
            it.setString(1, key.tenant); it.setString(2, key.document); it.setString(3, version)
            it.executeQuery().use { rows -> if (rows.next()) StoredPublication(key, version, rows.getLong(1), rows.getString(2)) else null }
        }
    override suspend fun activePublication(key: DocumentKey): StoredPublication? = transaction { connection ->
        connection.prepareStatement("SELECT p.version,p.revision,p.source FROM public.humming_publication_heads h JOIN public.humming_publications p USING(tenant_id,document_id,version) WHERE h.tenant_id=? AND h.document_id=?").use {
            it.setString(1, key.tenant); it.setString(2, key.document)
            it.executeQuery().use { rows -> if (rows.next()) StoredPublication(key, rows.getString(1), rows.getLong(2), rows.getString(3)) else null }
        }
    }
    override suspend fun selectPublication(key: DocumentKey, version: String): Boolean = transaction { connection ->
        // Serialize pointer changes with publications/edits for this document only.
        if (find(connection, key, lock = true) == null || publication(connection, key, version) == null) return@transaction false
        connection.prepareStatement("INSERT INTO public.humming_publication_heads(tenant_id,document_id,version) VALUES(?,?,?) ON CONFLICT(tenant_id,document_id) DO UPDATE SET version=EXCLUDED.version").use {
            it.setString(1, key.tenant); it.setString(2, key.document); it.setString(3, version); check(it.executeUpdate() == 1)
        }
        true
    }

    override suspend fun create(draft: StoredDraft): Boolean = transaction { connection ->
        connection.prepareStatement("INSERT INTO public.humming_drafts(tenant_id,document_id,revision,source) VALUES(?,?,?,?) ON CONFLICT DO NOTHING").use {
            it.setString(1, draft.key.tenant); it.setString(2, draft.key.document); it.setLong(3, draft.revision); it.setString(4, draft.source)
            it.executeUpdate() == 1
        }
    }
    override suspend fun find(key: DocumentKey): StoredDraft? = transaction { find(it, key, lock = false) }

    override suspend fun edit(key: DocumentKey, canonicalCommand: String): DraftEditResult {
        val requestContext = currentCoroutineContext()
        requestContext.ensureActive()
        // Parse/fingerprint before acquiring a document row lock; no external IO while holding it.
        val command = json.decodeFromJsonElement(EditCommand.serializer(), parseBoundedJson(canonicalCommand, documentId = key.document))
        val fingerprint = hash(canonicalCommand)
        return transaction { connection ->
            val stored = find(connection, key, lock = true)
                ?: return@transaction DraftEditResult.Rejected(ApplicationError.NOT_FOUND)
            requestContext.ensureActive()
            connection.prepareStatement("SELECT fingerprint,applied_revision,diagnostics FROM public.humming_edit_receipts WHERE tenant_id=? AND document_id=? AND command_id=?").use {
                it.setString(1, key.tenant); it.setString(2, key.document); it.setString(3, command.commandId)
                it.executeQuery().use { rows ->
                    if (rows.next()) return@transaction if (rows.getString(1) == fingerprint)
                        DraftEditResult.Accepted(stored, rows.getLong(2), replayed = true, decodeDiagnostics(rows.getString(3)))
                    else DraftEditResult.Rejected(ApplicationError.COMMAND_REPLAY_CONFLICT, currentRevision = stored.revision)
                }
            }
            if (stored.revision != command.expectedRevision)
                return@transaction DraftEditResult.Rejected(ApplicationError.REVISION_CONFLICT, currentRevision = stored.revision)
            val document = codec.decode(stored.source).requireValue()
            check(document.documentId == key.document && document.revision == stored.revision) { "Stored draft metadata differs from its source" }
            val outcome = EditSession(document, catalog).execute(command)
            if (!outcome.isSuccess) return@transaction DraftEditResult.Rejected(ApplicationError.INVALID_COMMAND, outcome.diagnostics, stored.revision)
            val after = checkNotNull(outcome.document)
            val updated = StoredDraft(key, after.revision, codec.encode(after).requireValue())
            requestContext.ensureActive()
            connection.prepareStatement("UPDATE public.humming_drafts SET revision=?,source=? WHERE tenant_id=? AND document_id=? AND revision=?").use {
                it.setLong(1, updated.revision); it.setString(2, updated.source); it.setString(3, key.tenant); it.setString(4, key.document); it.setLong(5, stored.revision)
                check(it.executeUpdate() == 1) { "Locked draft changed unexpectedly" }
            }
            val diagnostics = JsonArray(outcome.diagnostics.map(::encodeDiagnostic)).toString()
            requestContext.ensureActive()
            connection.prepareStatement("INSERT INTO public.humming_edit_receipts(tenant_id,document_id,command_id,fingerprint,applied_revision,diagnostics) VALUES(?,?,?,?,?,?)").use {
                it.setString(1, key.tenant); it.setString(2, key.document); it.setString(3, command.commandId)
                it.setString(4, fingerprint); it.setLong(5, updated.revision); it.setString(6, diagnostics); it.executeUpdate()
            }
            DraftEditResult.Accepted(updated, updated.revision, replayed = false, outcome.diagnostics)
        }
    }

    private fun find(connection: Connection, key: DocumentKey, lock: Boolean): StoredDraft? =
        connection.prepareStatement("SELECT revision,source FROM public.humming_drafts WHERE tenant_id=? AND document_id=?" + if (lock) " FOR UPDATE" else "").use {
            it.setString(1, key.tenant); it.setString(2, key.document)
            it.executeQuery().use { rows -> if (rows.next()) StoredDraft(key, rows.getLong(1), rows.getString(2)) else null }
        }
    private suspend fun <T> transaction(block: (Connection) -> T): T = withContext(Dispatchers.IO) {
        currentCoroutineContext().ensureActive()
        try { dataSource.connection.use { connection ->
            currentCoroutineContext().ensureActive()
            connection.transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
            currentCoroutineContext().ensureActive()
            connection.autoCommit = false
            try {
                connection.createStatement().use {
                    it.execute("SET LOCAL statement_timeout='10s'")
                    it.execute("SET LOCAL lock_timeout='5s'")
                }
                currentCoroutineContext().ensureActive()
                val value = block(connection)
                currentCoroutineContext().ensureActive()
                connection.commit()
                value
            } catch (failure: Throwable) {
                try { connection.rollback() } catch (cleanup: Throwable) { failure.addSuppressed(cleanup) }
                throw failure
            }
        } } catch (failure: Throwable) {
            try { currentCoroutineContext().ensureActive() } catch (cancellation: CancellationException) {
                if (cancellation !== failure) cancellation.addSuppressed(failure)
                throw cancellation
            }
            throw failure
        }
    }
    private fun hash(source: String) = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(source.toByteArray(Charsets.UTF_8)))
    private fun encodeDiagnostic(value: NodeDiagnostic) = buildJsonObject {
        put("code", value.code); put("message", value.message); put("operation", value.operation)
        put("path", value.path); put("severity", value.severity.name)
        value.nodeId?.let { put("nodeId", it) }; value.documentId?.let { put("documentId", it) }
    }
    private fun decodeDiagnostics(source: String): List<NodeDiagnostic> = (parseBoundedJson(source) as JsonArray).map { value ->
        val item = value.jsonObject
        NodeDiagnostic(item.getValue("code").jsonPrimitive.content, item.getValue("message").jsonPrimitive.content,
            item.getValue("operation").jsonPrimitive.content, item.getValue("path").jsonPrimitive.content,
            item["nodeId"]?.jsonPrimitive?.content, item["documentId"]?.jsonPrimitive?.content,
            DiagnosticSeverity.valueOf(item.getValue("severity").jsonPrimitive.content))
    }
}
