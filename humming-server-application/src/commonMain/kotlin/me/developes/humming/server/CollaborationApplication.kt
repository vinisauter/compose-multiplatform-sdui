package me.developes.humming.server

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import me.developes.humming.sdui.common.*
import me.developes.humming.sdui.studio.*

interface CollaborationRepository {
    suspend fun updates(key: DocumentKey, afterRevision: Long): DraftUpdateFeed?
    suspend fun heartbeat(key: DocumentKey, actorId: String): DraftPresenceList?
    suspend fun leave(key: DocumentKey, actorId: String)
    suspend fun undo(key: DocumentKey, actorId: String, canonicalCommand: String): DraftEditResult
}
class CollaborationApplication(private val repository: CollaborationRepository, private val authorizer: DocumentAuthorizer) {
    private val json = Json { encodeDefaults = true }
    private fun allows(actor: Actor, key: DocumentKey, edit: Boolean = false) = authorizer.allows(actor, key, Permission.READ) &&
        (!edit || authorizer.allows(actor, key, Permission.EDIT))
    suspend fun updates(actor: Actor, key: DocumentKey, afterRevision: Long): ApplicationResult<DraftUpdateFeed> {
        currentCoroutineContext().ensureActive()
        if (!allows(actor, key)) return ApplicationResult.Failure(ApplicationError.FORBIDDEN)
        if (afterRevision < 0) return ApplicationResult.Failure(ApplicationError.INVALID_COMMAND)
        val feed = repository.updates(key, afterRevision) ?: return ApplicationResult.Failure(ApplicationError.NOT_FOUND)
        currentCoroutineContext().ensureActive()
        check(feed.documentId == key.document && feed.afterRevision == afterRevision) { "Repository returned a different feed" }
        return ApplicationResult.Success(feed)
    }
    suspend fun heartbeat(actor: Actor, key: DocumentKey): ApplicationResult<DraftPresenceList> {
        currentCoroutineContext().ensureActive()
        if (!allows(actor, key, true)) return ApplicationResult.Failure(ApplicationError.FORBIDDEN)
        val result = repository.heartbeat(key, actor.id)
        currentCoroutineContext().ensureActive()
        return result?.let { ApplicationResult.Success(it) } ?: ApplicationResult.Failure(ApplicationError.NOT_FOUND)
    }
    suspend fun leave(actor: Actor, key: DocumentKey): ApplicationResult<Boolean> {
        currentCoroutineContext().ensureActive()
        if (!allows(actor, key, true)) return ApplicationResult.Failure(ApplicationError.FORBIDDEN)
        repository.leave(key, actor.id); currentCoroutineContext().ensureActive(); return ApplicationResult.Success(true)
    }
    suspend fun undo(actor: Actor, key: DocumentKey, source: String): DraftEditResult {
        currentCoroutineContext().ensureActive()
        if (!allows(actor, key, true)) return DraftEditResult.Rejected(ApplicationError.FORBIDDEN)
        val canonical = try {
            val element = parseBoundedJson(source, documentId = key.document)
            val command = json.decodeFromJsonElement(RemoteUndoCommand.serializer(), element)
            if (command.expectedRevision < 0 || listOf(command.commandId, command.undoOfCommandId).any { it.isBlank() || it.length > 256 })
                return DraftEditResult.Rejected(ApplicationError.INVALID_COMMAND)
            boundedCanonicalJson(json.encodeToJsonElement(RemoteUndoCommand.serializer(), command), 4096, operation = "undoDraft", documentId = key.document)
        } catch (failure: PayloadException) { return DraftEditResult.Rejected(ApplicationError.INVALID_COMMAND, listOf(failure.diagnostic))
        } catch (_: SerializationException) { return DraftEditResult.Rejected(ApplicationError.INVALID_COMMAND) }
        currentCoroutineContext().ensureActive()
        val result = repository.undo(key, actor.id, canonical)
        currentCoroutineContext().ensureActive()
        return result
    }
}
