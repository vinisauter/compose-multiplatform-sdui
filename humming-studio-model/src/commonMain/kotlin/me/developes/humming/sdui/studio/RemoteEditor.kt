package me.developes.humming.sdui.studio

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.*
import me.developes.humming.sdui.common.*

/** Connection is a host-owned alias, not a URL/token/credential. */
@Serializable
data class RemoteDocument(val connection: String, val tenant: String, val document: String)
@Serializable
enum class RemotePause { NONE, OFFLINE, UNAUTHORIZED, CONFLICT, REJECTED }
@Serializable
private data class RemoteCheckpoint(val formatVersion: Int, val identity: RemoteDocument, val confirmed: String,
    val pending: List<EditCommand>, val pause: RemotePause, val observedRemote: String?, val pendingUndo: RemoteUndoCommand? = null)

/** Pure outbox state machine. Host persists checkpoint BEFORE sending, and again after each acknowledgement. */
class RemoteEditor private constructor(val identity: RemoteDocument, private val catalog: NodeCatalog,
    private var confirmed: AuthoringDocument, private val pending: MutableList<EditCommand>,
    pause: RemotePause, observedRemote: String?, private var pendingUndo: RemoteUndoCommand? = null) {
    private val codec = AuthoringDocumentCodec(catalog)
    private val json = Json { encodeDefaults = true; classDiscriminator = "operationType" }
    var document: AuthoringDocument = rebuild(confirmed, pending, catalog)
        private set
    var pause: RemotePause = pause
        private set
    var observedRemote: String? = observedRemote
        private set
    val pendingCount: Int get() = pending.size + if (pendingUndo == null) 0 else 1
    val hasPendingUndo: Boolean get() = pendingUndo != null
    val confirmedRevision: Long get() = confirmed.revision
    private var persisted: String? = null
    private var active: SendTicket? = null
    private var activeUndo: UndoTicket? = null
    val sending: Boolean get() = active != null || activeUndo != null
    val needsPersistence: Boolean get() = persisted != checkpoint().requireValue()
    class SendTicket internal constructor(val command: EditCommand)
    class UndoTicket internal constructor(val command: RemoteUndoCommand)

    fun checkpoint(): AuthoringResult<String> = encodeCheckpoint(confirmed, pending, pause, observedRemote)
    private fun encodeCheckpoint(base: AuthoringDocument, commands: List<EditCommand>, reason: RemotePause, observed: String?, undo: RemoteUndoCommand? = pendingUndo): AuthoringResult<String> = try {
        val value = RemoteCheckpoint(2, identity, codec.encode(base).requireValue(), commands.toList(), reason, observed, undo)
        val element = json.encodeToJsonElement(RemoteCheckpoint.serializer(), value)
        validateBoundedJson(element, checkpointLimits, identity.document)
        AuthoringResult(boundedCanonicalJson(element, checkpointLimits.maxInputChars, operation = "remoteCheckpoint", documentId = identity.document), emptyList())
    } catch (failure: PayloadException) { AuthoringResult(null, listOf(failure.diagnostic)) }

    /** Return false if another local edit happened while the host was saving this checkpoint. */
    fun persisted(source: String): Boolean {
        if (source != checkpoint().requireValue()) return false
        persisted = source
        return true
    }
    fun enqueue(command: EditCommand): EditOutcome {
        if (pendingUndo != null) return rejected("REMOTE_UNDO_PENDING", "Resolve the pending undo before adding another edit")
        if (pending.size >= 128 || pending.any { it.commandId == command.commandId })
            return rejected("REMOTE_OUTBOX_LIMIT", "Outbox full or duplicate pending command ID")
        val result = EditSession(document, catalog).execute(command)
        val after = result.document ?: return result
        pending += json.decodeFromJsonElement(EditCommand.serializer(), json.encodeToJsonElement(EditCommand.serializer(), command))
        val checkpoint = checkpoint()
        if (!checkpoint.isSuccess) { pending.removeAt(pending.lastIndex); return EditOutcome(null, checkpoint.diagnostics) }
        document = after
        return result
    }
    fun beginSend(): SendTicket? {
        if (sending || pendingUndo != null || pending.isEmpty() || needsPersistence || pause !in setOf(RemotePause.NONE, RemotePause.OFFLINE)) return null
        return SendTicket(pending.first()).also { active = it }
    }
    /** Receipt may contain a newer document, but its appliedRevision must still identify this command. */
    fun acknowledge(ticket: SendTicket, source: String, appliedRevision: Long): AuthoringResult<Boolean> {
        if (active !== ticket) return AuthoringResult(false, emptyList())
        val decoded = codec.decode(source)
        val remote = decoded.value ?: return AuthoringResult(null, decoded.diagnostics)
        if (remote.documentId != identity.document || appliedRevision != ticket.command.expectedRevision + 1 || remote.revision < appliedRevision)
            return AuthoringResult(null, listOf(diagnostic("REMOTE_ACK", "Receipt identity/revision does not match the pending command")))
        val predicted = EditSession(confirmed, catalog).execute(ticket.command).document
            ?: error("Pending command no longer applies to its confirmed base")
        val predictedSource = codec.encode(predicted).requireValue()
        val remoteSource = codec.encode(remote).requireValue()
        val conflict = remote.revision != appliedRevision || predictedSource != remoteSource
        val nextObserved = remoteSource.takeIf { conflict }
        val nextPause = if (conflict) RemotePause.CONFLICT else RemotePause.NONE
        val checkpoint = encodeCheckpoint(predicted, pending.drop(1), nextPause, nextObserved)
        if (!checkpoint.isSuccess) return AuthoringResult(null, checkpoint.diagnostics)
        pending.removeAt(0); confirmed = predicted; active = null
        observedRemote = nextObserved; pause = nextPause
        // The visible local document already includes every later queued edit. An old ack never replaces it.
        return AuthoringResult(true, emptyList())
    }
    fun failed(ticket: SendTicket, reason: RemotePause): Boolean {
        require(reason != RemotePause.NONE)
        if (active !== ticket) return false
        active = null; pause = reason
        return true
    }
    /** Host may retry after reconnect/reauthentication. Conflict/rejection require an explicit resolution. */
    fun retry(): Boolean {
        if (sending || pause in setOf(RemotePause.CONFLICT, RemotePause.REJECTED)) return false
        pause = RemotePause.NONE
        return true
    }
    fun observeConflict(source: String): AuthoringResult<Boolean> {
        if (sending) return AuthoringResult(null, listOf(diagnostic("REMOTE_BUSY", "Finish the current request before reading a conflict")))
        val decoded = codec.decode(source)
        val remote = decoded.value ?: return AuthoringResult(null, decoded.diagnostics)
        if (remote.documentId != identity.document || remote.revision < confirmed.revision)
            return AuthoringResult(null, listOf(diagnostic("REMOTE_IDENTITY", "Remote document is stale or belongs to another identity")))
        val canonical = codec.encode(remote).requireValue()
        val checkpoint = encodeCheckpoint(confirmed, pending, RemotePause.CONFLICT, canonical)
        if (!checkpoint.isSuccess) return AuthoringResult(null, checkpoint.diagnostics)
        observedRemote = canonical; pause = RemotePause.CONFLICT
        return AuthoringResult(true, emptyList())
    }
    /** Remote broadcast/poll is definition-only and must never consume or overwrite a pending local command. */
    fun receiveUpdates(feed: DraftUpdateFeed): AuthoringResult<Boolean> {
        if (pendingCount > 0 || sending) return AuthoringResult(false, emptyList())
        if (feed.formatVersion != 1 || feed.documentId != identity.document || feed.afterRevision < 0 || feed.currentRevision < 0 || feed.events.size > 32)
            return AuthoringResult(null, listOf(diagnostic("REMOTE_FEED", "Invalid update feed identity or budget")))
        if (feed.currentRevision <= confirmed.revision) return AuthoringResult(false, emptyList())
        if (feed.afterRevision > feed.currentRevision || feed.afterRevision > confirmed.revision)
            return AuthoringResult(null, listOf(diagnostic("REMOTE_RESYNC", "Update cursor is ahead of this editor")))
        if (feed.resyncRequired) return AuthoringResult(null, listOf(diagnostic("REMOTE_RESYNC", "A fresh snapshot is required")))
        var candidate = confirmed
        for (event in feed.events) {
            if (event.revision <= candidate.revision) continue // Duplicate/older broadcast is harmless.
            if (candidate.revision == Long.MAX_VALUE || event.revision != candidate.revision + 1 || event.revision > feed.currentRevision ||
                event.commandId.isBlank() || event.commandId.length > 256 || event.actorId.isBlank() || event.actorId.length > 128)
                return AuthoringResult(null, listOf(diagnostic("REMOTE_RESYNC", "Update sequence has a gap or arrived out of order")))
            val next = applyPatch(candidate, event.forward).copy(revision = event.revision)
            val encoded = codec.encode(next)
            if (!encoded.isSuccess) return AuthoringResult(null, encoded.diagnostics)
            candidate = codec.decode(encoded.requireValue()).requireValue()
        }
        if (candidate.revision == confirmed.revision || (!feed.hasMore && candidate.revision != feed.currentRevision))
            return AuthoringResult(null, listOf(diagnostic("REMOTE_RESYNC", "Update feed does not reach its declared cursor")))
        return adoptClean(codec.encode(candidate).requireValue())
    }
    fun adoptClean(source: String): AuthoringResult<Boolean> {
        if (pendingCount > 0 || sending) return AuthoringResult(false, emptyList())
        val decoded = codec.decode(source)
        val remote = decoded.value ?: return AuthoringResult(null, decoded.diagnostics)
        if (remote.documentId != identity.document || remote.revision < confirmed.revision)
            return AuthoringResult(null, listOf(diagnostic("REMOTE_IDENTITY", "Cannot adopt a stale or different remote definition")))
        if (remote.revision == confirmed.revision && codec.encode(remote).requireValue() != codec.encode(confirmed).requireValue())
            return AuthoringResult(null, listOf(diagnostic("REMOTE_DIVERGENCE", "Same revision contains different remote content")))
        val encoded = encodeCheckpoint(remote, emptyList(), RemotePause.NONE, null)
        if (!encoded.isSuccess) return AuthoringResult(null, encoded.diagnostics)
        confirmed = remote; document = remote; pause = RemotePause.NONE; observedRemote = null
        return AuthoringResult(true, emptyList())
    }
    /** Explicitly destructive resolution: caller must offer/export the pending checkpoint before invoking. */
    fun discardPendingAndOpen(source: String): AuthoringResult<Boolean> {
        if (sending) return AuthoringResult(null, listOf(diagnostic("REMOTE_BUSY", "Cannot replace an in-flight outbox")))
        val decoded = codec.decode(source)
        val remote = decoded.value ?: return AuthoringResult(null, decoded.diagnostics)
        if (remote.documentId != identity.document || remote.revision < confirmed.revision)
            return AuthoringResult(null, listOf(diagnostic("REMOTE_IDENTITY", "A stale or different document cannot replace this outbox")))
        pending.clear(); pendingUndo = null; confirmed = remote; document = remote; observedRemote = null; pause = RemotePause.NONE
        return AuthoringResult(true, emptyList())
    }
    fun enqueueUndo(command: RemoteUndoCommand): AuthoringResult<Boolean> {
        if (pendingCount > 0 || sending || command.expectedRevision != confirmed.revision ||
            !validUndo(command) || pause in setOf(RemotePause.CONFLICT, RemotePause.REJECTED))
            return AuthoringResult(null, listOf(diagnostic("REMOTE_UNDO_PENDING", "Undo requires a clean confirmed document and a valid command")))
        val candidate = encodeCheckpoint(confirmed, emptyList(), pause, observedRemote, command)
        if (!candidate.isSuccess) return AuthoringResult(null, candidate.diagnostics)
        pendingUndo = command
        return AuthoringResult(true, emptyList())
    }
    fun beginUndo(): UndoTicket? {
        if (sending || pendingUndo == null || needsPersistence || pause !in setOf(RemotePause.NONE, RemotePause.OFFLINE)) return null
        return UndoTicket(pendingUndo!!).also { activeUndo = it }
    }
    fun acknowledgeUndo(ticket: UndoTicket, source: String, appliedRevision: Long): AuthoringResult<Boolean> {
        if (activeUndo !== ticket) return AuthoringResult(false, emptyList())
        val decoded = codec.decode(source)
        val remote = decoded.value ?: return AuthoringResult(null, decoded.diagnostics)
        if (remote.documentId != identity.document || appliedRevision != ticket.command.expectedRevision + 1 || remote.revision < appliedRevision)
            return AuthoringResult(null, listOf(diagnostic("REMOTE_ACK", "Undo receipt identity/revision does not match")))
        val checkpoint = encodeCheckpoint(remote, emptyList(), RemotePause.NONE, null, null)
        if (!checkpoint.isSuccess) return AuthoringResult(null, checkpoint.diagnostics)
        pendingUndo = null; activeUndo = null; confirmed = remote; document = remote; pause = RemotePause.NONE; observedRemote = null
        return AuthoringResult(true, emptyList())
    }
    fun failedUndo(ticket: UndoTicket, reason: RemotePause): Boolean {
        require(reason != RemotePause.NONE)
        if (activeUndo !== ticket) return false
        activeUndo = null; pause = reason
        return true
    }
    private fun rejected(code: String, message: String) = EditOutcome(null, listOf(diagnostic(code, message)))
    private fun diagnostic(code: String, message: String) = NodeDiagnostic(code, message, "remoteEditor", documentId = identity.document)

    companion object {
        private val checkpointLimits = PayloadLimits(maxInputChars = 8_000_000, maxPropertyChars = 4_000_000, maxJsonValues = 150_000)
        private fun valid(identity: RemoteDocument) = Regex("[A-Za-z0-9][A-Za-z0-9_.-]{0,127}").matches(identity.connection) &&
            listOf(identity.tenant, identity.document).all { Regex("[A-Za-z0-9][A-Za-z0-9_-]{0,127}").matches(it) }
        private fun validUndo(command: RemoteUndoCommand) = command.expectedRevision in 0 until Long.MAX_VALUE &&
            command.commandId != command.undoOfCommandId && listOf(command.commandId, command.undoOfCommandId).all { it.isNotBlank() && it.length <= 256 }
        private fun rebuild(base: AuthoringDocument, commands: List<EditCommand>, catalog: NodeCatalog): AuthoringDocument {
            val session = EditSession(base, catalog)
            commands.forEach { check(session.execute(it).isSuccess) { "Invalid pending command sequence" } }
            return session.snapshot()
        }
        fun open(identity: RemoteDocument, source: String, catalog: NodeCatalog): AuthoringResult<RemoteEditor> {
            val decoded = AuthoringDocumentCodec(catalog).decode(source)
            val document = decoded.value ?: return AuthoringResult(null, decoded.diagnostics)
            if (!valid(identity) || document.documentId != identity.document) return failure("REMOTE_IDENTITY", identity)
            return AuthoringResult(RemoteEditor(identity, catalog, document, mutableListOf(), RemotePause.NONE, null), emptyList())
        }
        fun restore(identity: RemoteDocument, source: String, catalog: NodeCatalog): AuthoringResult<RemoteEditor> = try {
            val json = Json { encodeDefaults = true; classDiscriminator = "operationType" }
            val saved = json.decodeFromJsonElement(RemoteCheckpoint.serializer(), parseBoundedJson(source, checkpointLimits))
            if (!valid(identity) || saved.formatVersion !in 1..2 || saved.identity != identity || saved.pending.size > 128 ||
                saved.pending.map { it.commandId }.toSet().size != saved.pending.size ||
                (saved.observedRemote != null && saved.pause != RemotePause.CONFLICT) ||
                (saved.pendingUndo != null && (saved.formatVersion != 2 || saved.pending.isNotEmpty() || !validUndo(saved.pendingUndo)))) failure("REMOTE_CHECKPOINT", identity)
            else {
                val codec = AuthoringDocumentCodec(catalog)
                val base = codec.decode(saved.confirmed).requireValue()
                if (base.documentId != identity.document) failure("REMOTE_IDENTITY", identity)
                else {
                    val session = EditSession(base, catalog)
                    val errors = saved.pending.flatMap { command -> session.execute(command).let { if (it.isSuccess) emptyList() else it.diagnostics } }
                    val observed = saved.observedRemote?.let { codec.decode(it).requireValue() }
                    if ((observed != null && (observed.documentId != identity.document || observed.revision < base.revision)) ||
                        (saved.pendingUndo != null && saved.pendingUndo.expectedRevision != base.revision)) failure("REMOTE_CHECKPOINT", identity)
                    else if (errors.isNotEmpty()) AuthoringResult(null, errors)
                    else AuthoringResult(RemoteEditor(identity, catalog, base, saved.pending.toMutableList(), saved.pause, saved.observedRemote, saved.pendingUndo).also {
                        it.persisted = it.checkpoint().requireValue()
                    }, emptyList())
                }
            }
        } catch (failure: PayloadException) { AuthoringResult(null, listOf(failure.diagnostic))
        } catch (_: SerializationException) { failure("REMOTE_CHECKPOINT", identity) }
        private fun failure(code: String, identity: RemoteDocument): AuthoringResult<RemoteEditor> = AuthoringResult(null,
            listOf(NodeDiagnostic(code, "Invalid remote identity or recovery checkpoint", "remoteEditor", documentId = identity.document)))
    }
}
