package me.developes.humming.sdui.studio

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import me.developes.humming.sdui.common.DiagnosticSeverity
import me.developes.humming.sdui.common.NodeDiagnostic
import me.developes.humming.sdui.common.PayloadException

enum class AuthorNodeKind { VISUAL, SCREEN, ACTION, NAVIGATION, UNKNOWN }
enum class EdgeRole { EVENT, ACTION_FLOW, NAVIGATION }
enum class NodeListShape { OBJECT, ARRAY, STRING_OBJECT, STRING_ARRAY }

@Serializable
data class CanvasPoint(val x: Double, val y: Double)

@Serializable
sealed class Placement {
    abstract val orderKey: String
    @Serializable @SerialName("root")
    data class Root(override val orderKey: String) : Placement()
    @Serializable @SerialName("child")
    data class Child(val parentId: String, val slot: String, override val orderKey: String) : Placement()
}

@Serializable
data class AuthorNode(
    val id: String,
    val kind: AuthorNodeKind,
    val definition: String,
    val properties: JsonObject,
    val placement: Placement,
    val canvasPosition: CanvasPoint? = null,
    val nodeListShapes: Map<String, NodeListShape> = emptyMap(),
    val hasProperties: Boolean = true,
    val hasChildren: Boolean = false,
    val extensions: JsonObject = JsonObject(emptyMap()),
)

@Serializable
data class AuthorEdge(
    val id: String,
    val role: EdgeRole,
    val sourceNodeId: String,
    val sourcePort: String,
    val targetNodeId: String,
    val targetPort: String,
    val orderKey: String? = null,
    val metadata: JsonObject = JsonObject(emptyMap()),
)

/** Maps are the only persisted authority; all tree/graph indices are derived. */
data class AuthoringDocument(
    val documentId: String,
    val formatVersion: Int = 1,
    val revision: Long = 0,
    val entryNodeId: String,
    val nodes: Map<String, AuthorNode>,
    val edges: Map<String, AuthorEdge> = emptyMap(),
)

data class AuthoringLimits(
    val maxNodes: Int = 10_000,
    val maxEdges: Int = 20_000,
    val maxDepth: Int = 64,
    val maxOrderKeyLength: Int = 256,
) {
    init { require(maxNodes > 0 && maxEdges > 0 && maxDepth in 1..256 && maxOrderKeyLength > 0) }
}

data class AuthoringResult<T>(val value: T?, val diagnostics: List<NodeDiagnostic>) {
    val isSuccess: Boolean get() = value != null && diagnostics.none { it.severity == DiagnosticSeverity.ERROR }
    fun requireValue(): T {
        diagnostics.firstOrNull { it.severity == DiagnosticSeverity.ERROR }?.let { throw PayloadException(it) }
        return checkNotNull(value) { "A successful authoring result must contain a value" }
    }
}

data class ParentSlot(val parentId: String, val slot: String)
data class SourcePort(val sourceNodeId: String, val port: String)

class AuthoringIndices(document: AuthoringDocument) {
    val roots: List<AuthorNode> = document.nodes.values.filter { it.placement is Placement.Root }
        .sortedWith(compareBy({ it.placement.orderKey }, { it.id }))
    val childrenByParentAndSlot: Map<ParentSlot, List<AuthorNode>> = document.nodes.values
        .filter { it.placement is Placement.Child }.groupBy {
            val placement = it.placement as Placement.Child
            ParentSlot(placement.parentId, placement.slot)
        }.mapValues { (_, nodes) -> nodes.sortedWith(compareBy({ it.placement.orderKey }, { it.id })) }
    val edgesBySourcePort: Map<SourcePort, List<AuthorEdge>> = document.edges.values
        .groupBy { SourcePort(it.sourceNodeId, it.sourcePort) }
        .mapValues { (_, edges) -> edges.sortedWith(compareBy({ it.orderKey ?: "" }, { it.id })) }
    val incomingEdges: Map<String, List<AuthorEdge>> = document.edges.values.groupBy { it.targetNodeId }
        .mapValues { (_, edges) -> edges.sortedBy { it.id } }
}

internal const val ORDER_DIGITS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
internal fun validOrderKey(key: String, limit: Int): Boolean = key.isNotEmpty() && key.length <= limit &&
    key.last() != '0' && key.all { it in ORDER_DIGITS }

/** Import ranks only. Midpoint generation and edit commands belong to the next development step. */
internal fun importOrderKey(index: Int): String {
    require(index >= 0)
    var number = index.toLong() + 1
    var encoded = ""
    while (number > 0) { encoded = ORDER_DIGITS[(number % 62).toInt()] + encoded; number /= 62 }
    return encoded.padStart(8, '0') + "V"
}
