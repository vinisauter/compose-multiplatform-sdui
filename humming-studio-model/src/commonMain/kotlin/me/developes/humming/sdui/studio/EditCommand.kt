package me.developes.humming.sdui.studio

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonElement
import me.developes.humming.sdui.common.NodeDiagnostic

@Serializable
sealed class NodeContainer {
    @Serializable @SerialName("root") data object Root : NodeContainer()
    @Serializable @SerialName("slot") data class Slot(val parentId: String, val slot: String) : NodeContainer()
}

@Serializable
sealed class EditOperation {
    /** The node placement supplies the destination container; its key is generated at commit. */
    @Serializable @SerialName("insertNode")
    data class InsertNode(val node: AuthorNode, val previousId: String? = null, val nextId: String? = null) : EditOperation()
    @Serializable @SerialName("removeNode") data class RemoveNode(val nodeId: String) : EditOperation()
    @Serializable @SerialName("moveNode") data class MoveNode(
        val nodeId: String, val destination: NodeContainer, val previousId: String? = null, val nextId: String? = null,
    ) : EditOperation()
    @Serializable @SerialName("properties") data class SetProperties(val nodeId: String, val properties: JsonObject) : EditOperation()
    @Serializable @SerialName("putProperty") data class PutProperty(val nodeId: String, val name: String, val value: JsonElement) : EditOperation()
    @Serializable @SerialName("removeProperty") data class RemoveProperty(val nodeId: String, val name: String) : EditOperation()
    @Serializable @SerialName("position") data class SetCanvasPosition(val nodeId: String, val position: CanvasPoint?) : EditOperation()
    @Serializable @SerialName("insertEdge") data class InsertEdge(
        val edge: AuthorEdge, val previousId: String? = null, val nextId: String? = null,
    ) : EditOperation()
    @Serializable @SerialName("removeEdge") data class RemoveEdge(val edgeId: String) : EditOperation()
    @Serializable @SerialName("entry") data class SetEntry(val nodeId: String) : EditOperation()
}

@Serializable
data class EditCommand(val commandId: String, val expectedRevision: Long, val operations: List<EditOperation>)

/** Null map values delete entries. Patches never reset the monotonic document revision. */
@Serializable
data class DocumentPatch(
    val nodes: Map<String, AuthorNode?> = emptyMap(),
    val edges: Map<String, AuthorEdge?> = emptyMap(),
    val entryNodeId: String? = null,
)

data class EditOutcome(
    val document: AuthoringDocument?,
    val diagnostics: List<NodeDiagnostic>,
    val forward: DocumentPatch? = null,
    val inverse: DocumentPatch? = null,
    val replayed: Boolean = false,
    val appliedRevision: Long? = document?.revision,
) {
    val isSuccess: Boolean get() = document != null
}

data class EditSessionLimits(
    val maxHistoryEntries: Int = 100,
    val maxHistoryChars: Int = 8_000_000,
    val maxReceipts: Int = 256,
    val maxReceiptChars: Int = 2_000_000,
    val maxOperations: Int = 1_000,
) {
    init { require(maxHistoryEntries > 0 && maxHistoryChars > 0 && maxReceipts > 0 && maxReceiptChars > 0 && maxOperations > 0) }
}
