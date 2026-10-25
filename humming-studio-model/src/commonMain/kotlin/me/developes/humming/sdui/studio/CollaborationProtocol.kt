package me.developes.humming.sdui.studio

import kotlinx.serialization.Serializable
import kotlinx.serialization.Required
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.*
import me.developes.humming.sdui.common.*

@Serializable
data class DraftUpdate(val revision: Long, val commandId: String, val actorId: String, val forward: DocumentPatch)
@Serializable
data class DraftUpdateFeed(val documentId: String, val afterRevision: Long, val currentRevision: Long,
    val events: List<DraftUpdate>, val hasMore: Boolean, val resyncRequired: Boolean, @Required val formatVersion: Int = 1)
@Serializable
data class DraftPresence(val actorId: String, val expiresAtMillis: Long)
@Serializable
data class DraftPresenceList(val members: List<DraftPresence>, val truncated: Boolean = false)
@Serializable
data class RemoteUndoCommand(val commandId: String, val expectedRevision: Long, val undoOfCommandId: String)

/** Same discriminator as command/patch serialization. Never use platform Json defaults for this wire. */
class DraftUpdateFeedCodec {
    private val json = Json { encodeDefaults = true; classDiscriminator = "operationType" }
    private val limits = PayloadLimits(maxInputChars = 4_000_000, maxPropertyChars = 4_000_000, maxJsonValues = 150_000)
    private fun valid(value: DraftUpdateFeed) = value.formatVersion == 1 && value.documentId.isNotBlank() &&
        value.documentId.length <= 128 && value.afterRevision >= 0 && value.currentRevision >= 0 && value.events.size <= 32 &&
        (!value.resyncRequired || value.events.isEmpty()) && value.events.all { it.revision > 0 &&
            it.commandId.isNotBlank() && it.commandId.length <= 256 && it.actorId.isNotBlank() && it.actorId.length <= 128 }
    private fun failure() = listOf(NodeDiagnostic("REMOTE_FEED", "Invalid collaboration feed", "updateFeed"))
    fun encode(value: DraftUpdateFeed): AuthoringResult<String> {
        if (!valid(value)) return AuthoringResult(null, failure())
        return try {
            val element = json.encodeToJsonElement(DraftUpdateFeed.serializer(), value)
            validateBoundedJson(element, limits, value.documentId)
            AuthoringResult(boundedCanonicalJson(element, limits.maxInputChars, operation = "updateFeed", documentId = value.documentId), emptyList())
        } catch (error: PayloadException) { AuthoringResult(null, listOf(error.diagnostic)) }
    }
    fun decode(source: String): AuthoringResult<DraftUpdateFeed> = try {
        val value = json.decodeFromJsonElement(DraftUpdateFeed.serializer(), parseBoundedJson(source, limits))
        if (valid(value)) AuthoringResult(value, emptyList()) else AuthoringResult(null, failure())
    } catch (error: PayloadException) { AuthoringResult(null, listOf(error.diagnostic))
    } catch (_: SerializationException) { AuthoringResult(null, failure()) }
}

/** Conservative compensation: any change to an affected node/edge blocks undo instead of erasing a peer's work. */
fun guardedUndo(document: AuthoringDocument, forward: DocumentPatch, inverse: DocumentPatch, catalog: NodeCatalog): AuthoringResult<AuthoringDocument> {
    fun failure() = AuthoringResult<AuthoringDocument>(null, listOf(NodeDiagnostic("UNDO_CONFLICT",
        "Affected content changed after the original command", "undoDraft", documentId = document.documentId)))
    if (document.revision == Long.MAX_VALUE || forward.nodes.keys != inverse.nodes.keys || forward.edges.keys != inverse.edges.keys ||
        (forward.entryNodeId == null) != (inverse.entryNodeId == null) ||
        forward.nodes.any { (id, expected) -> document.nodes[id] != expected } ||
        forward.edges.any { (id, expected) -> document.edges[id] != expected } ||
        (forward.entryNodeId != null && forward.entryNodeId != document.entryNodeId)) return failure()
    val candidate = applyPatch(document, inverse).copy(revision = document.revision + 1)
    val codec = AuthoringDocumentCodec(catalog)
    val encoded = codec.encode(candidate)
    return if (encoded.isSuccess) codec.decode(encoded.requireValue()) else failure()
}
