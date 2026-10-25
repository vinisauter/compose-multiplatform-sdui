package me.developes.humming.server

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import me.developes.humming.sdui.common.*
import me.developes.humming.sdui.studio.*

sealed interface DraftEditResult {
    data class Accepted(val draft: StoredDraft, val appliedRevision: Long, val replayed: Boolean,
                        val diagnostics: List<NodeDiagnostic> = emptyList()) : DraftEditResult
    data class Rejected(val code: ApplicationError, val diagnostics: List<NodeDiagnostic> = emptyList(),
                        val currentRevision: Long? = null) : DraftEditResult
}
interface RevisionedDraftRepository : DraftRepository {
    /** Atomically checks revision, applies and stores canonical command receipt with the new source. */
    suspend fun edit(key: DocumentKey, canonicalCommand: String): DraftEditResult
    suspend fun editAs(key: DocumentKey, canonicalCommand: String, actorId: String): DraftEditResult = edit(key, canonicalCommand)
}
class DraftEditingApplication(private val repository: RevisionedDraftRepository, private val authorizer: DocumentAuthorizer) {
    private val json = Json { encodeDefaults = true; classDiscriminator = "operationType" }
    suspend fun edit(actor: Actor, key: DocumentKey, source: String): DraftEditResult {
        currentCoroutineContext().ensureActive()
        if (!authorizer.allows(actor, key, Permission.EDIT) || !authorizer.allows(actor, key, Permission.READ))
            return DraftEditResult.Rejected(ApplicationError.FORBIDDEN)
        val canonical = try {
            val element = parseBoundedJson(source, documentId = key.document)
            val command = json.decodeFromJsonElement(EditCommand.serializer(), element)
            if (command.commandId.isBlank() || command.commandId.length > 256 || command.expectedRevision < 0 || command.operations.size !in 1..1000)
                return DraftEditResult.Rejected(ApplicationError.INVALID_COMMAND, listOf(NodeDiagnostic(
                    "COMMAND_INPUT", "Command identity, revision or operation budget is invalid", "editDraft", documentId = key.document)))
            boundedCanonicalJson(json.encodeToJsonElement(EditCommand.serializer(), command), 2_000_000, 64,
                operation = "editDraft", documentId = key.document)
        } catch (error: PayloadException) {
            return DraftEditResult.Rejected(ApplicationError.INVALID_COMMAND, listOf(error.diagnostic.copy(operation = "editDraft")))
        } catch (_: SerializationException) {
            return DraftEditResult.Rejected(ApplicationError.INVALID_COMMAND, listOf(NodeDiagnostic(
                "COMMAND_JSON", "Invalid command structure", "editDraft", documentId = key.document)))
        }
        currentCoroutineContext().ensureActive()
        return repository.editAs(key, canonical, actor.id)
    }
}
