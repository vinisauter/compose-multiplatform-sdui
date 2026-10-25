package me.developes.humming.sdui.studio

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import me.developes.humming.sdui.common.NodeCatalog
import me.developes.humming.sdui.common.NodeDiagnostic
import me.developes.humming.sdui.common.NodeRole
import me.developes.humming.sdui.common.PayloadException
import me.developes.humming.sdui.common.PayloadLimits
import me.developes.humming.sdui.common.PropertyKind
import me.developes.humming.sdui.common.validateBoundedJson
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

private data class HistoryEntry(val forward: DocumentPatch, val inverse: DocumentPatch, val chars: Int)
private data class Receipt(val signature: String, val revision: Long, val diagnostics: List<NodeDiagnostic>)
private class SessionState(
    val document: AuthoringDocument,
    val undo: List<HistoryEntry> = emptyList(),
    val redo: List<HistoryEntry> = emptyList(),
    val receipts: Map<String, Receipt> = emptyMap(),
)
private enum class Direction { EDIT, UNDO, REDO }

/** Owns a deep snapshot. One CAS publishes document, history and receipts together. */
@OptIn(ExperimentalAtomicApi::class)
class EditSession(
    initialDocument: AuthoringDocument,
    private val catalog: NodeCatalog,
    private val authoringLimits: AuthoringLimits = AuthoringLimits(),
    private val sessionLimits: EditSessionLimits = EditSessionLimits(),
    private val payloadLimits: PayloadLimits = PayloadLimits(),
) {
    private val json = Json { encodeDefaults = true; classDiscriminator = "operationType" }
    private val codec = AuthoringDocumentCodec(catalog, authoringLimits, payloadLimits)
    private val state = AtomicReference(SessionState(freeze(initialDocument)))
    fun snapshot(): AuthoringDocument = state.load().document
    val canUndo: Boolean get() = state.load().undo.isNotEmpty()
    val canRedo: Boolean get() = state.load().redo.isNotEmpty()

    fun execute(command: EditCommand): EditOutcome {
        return try {
            if (command.operations.isEmpty() || command.operations.size > sessionLimits.maxOperations) return rejected(
                "COMMAND_OPERATIONS", "Command operation count is outside its budget")
            val element = json.encodeToJsonElement(EditCommand.serializer(), command)
            validateBoundedJson(element, payloadLimits, snapshot().documentId)
            val signature = "edit:" + boundedCanonicalJson(element, maxOf(0, payloadLimits.maxInputChars - 5),
                payloadLimits.maxDepth, "edit", snapshot().documentId)
            if (signature.length > payloadLimits.maxInputChars) return rejected("COMMAND_BUDGET", "Command character budget exceeded")
            // Snapshot the command too: callers cannot mutate an input map after commit.
            val owned = json.decodeFromString(EditCommand.serializer(), signature.removePrefix("edit:"))
            transition(owned.commandId, owned.expectedRevision, signature, Direction.EDIT, owned.operations)
        } catch (error: PayloadException) { EditOutcome(null, listOf(error.diagnostic)) }
        catch (_: SerializationException) { rejected("COMMAND_JSON", "Command contains an unsupported serialized value") }
    }

    fun undo(commandId: String, expectedRevision: Long): EditOutcome =
        transition(commandId, expectedRevision, "undo:$expectedRevision", Direction.UNDO)

    fun redo(commandId: String, expectedRevision: Long): EditOutcome =
        transition(commandId, expectedRevision, "redo:$expectedRevision", Direction.REDO)

    private fun transition(id: String, revision: Long, signature: String, direction: Direction,
                           operations: List<EditOperation> = emptyList()): EditOutcome {
        if (id.isBlank() || id.length > 256) return rejected("COMMAND_ID", "Command ID must have 1..256 characters")
        if (signature.length.toLong() + id.length > sessionLimits.maxReceiptChars) return rejected(
            "RECEIPT_BUDGET", "Command cannot fit the local deduplication budget")
        while (true) {
            val before = state.load()
            before.receipts[id]?.let { receipt ->
                return if (receipt.signature == signature) EditOutcome(before.document, receipt.diagnostics,
                    replayed = true, appliedRevision = receipt.revision)
                else rejected("COMMAND_REPLAY_CONFLICT", "Command ID was already used for a different request")
            }
            if (revision != before.document.revision) return rejected("REVISION_CONFLICT", "Expected revision does not match current document")
            if (revision == Long.MAX_VALUE) return rejected("REVISION_OVERFLOW", "Document revision cannot advance")
            try {
                val history = when (direction) {
                    Direction.EDIT -> null
                    Direction.UNDO -> before.undo.lastOrNull() ?: return rejected("NOTHING_TO_UNDO", "Undo history is empty")
                    Direction.REDO -> before.redo.lastOrNull() ?: return rejected("NOTHING_TO_REDO", "Redo history is empty")
                }
                val candidate = when (direction) {
                    Direction.EDIT -> applyOperations(before.document, operations)
                    Direction.UNDO -> applyPatch(before.document, history!!.inverse)
                    Direction.REDO -> applyPatch(before.document, history!!.forward)
                }.copy(revision = revision + 1)
                val validation = codec.encode(candidate)
                if (!validation.isSuccess) return EditOutcome(null, validation.diagnostics)
                val after = codec.decode(validation.requireValue()).requireValue()
                val forward = difference(before.document, after)
                val inverse = difference(after, before.document)
                val weight = json.encodeToString(DocumentPatch.serializer(), forward).length.toLong() +
                    json.encodeToString(DocumentPatch.serializer(), inverse).length
                if (weight > sessionLimits.maxHistoryChars) return rejected("HISTORY_BUDGET", "Command cannot fit the undo budget")
                val entry = HistoryEntry(forward, inverse, weight.toInt())
                val undo = when (direction) {
                    Direction.EDIT -> trimHistory(before.undo + entry)
                    Direction.UNDO -> before.undo.dropLast(1)
                    Direction.REDO -> before.undo + history!!
                }
                val redo = when (direction) {
                    Direction.EDIT -> emptyList()
                    Direction.UNDO -> before.redo + history!!
                    Direction.REDO -> before.redo.dropLast(1)
                }
                val receipts = (before.receipts + (id to Receipt(signature, after.revision, validation.diagnostics))).toMutableMap()
                var chars = receipts.entries.sumOf { it.key.length.toLong() + it.value.signature.length }
                while (receipts.size > sessionLimits.maxReceipts || chars > sessionLimits.maxReceiptChars) {
                    val oldest = receipts.entries.first()
                    chars -= oldest.key.length.toLong() + oldest.value.signature.length
                    receipts.remove(oldest.key)
                }
                if (state.compareAndSet(before, SessionState(after, undo, redo, receipts))) return EditOutcome(
                    after, validation.diagnostics, forward, inverse)
                // A competing commit won. Re-read receipts/revision before retrying.
            } catch (error: PayloadException) { return EditOutcome(null, listOf(error.diagnostic)) }
        }
    }

    private fun trimHistory(entries: List<HistoryEntry>): List<HistoryEntry> {
        val result = entries.toMutableList()
        var chars = result.sumOf { it.chars.toLong() }
        while (result.size > sessionLimits.maxHistoryEntries || chars > sessionLimits.maxHistoryChars) chars -= result.removeAt(0).chars
        return result
    }

    private fun freeze(document: AuthoringDocument): AuthoringDocument = codec.decode(codec.encode(document).requireValue()).requireValue()

    private fun rejected(code: String, message: String): EditOutcome = EditOutcome(null,
        listOf(NodeDiagnostic(code, message, "edit", documentId = snapshot().documentId)))

    private fun applyOperations(document: AuthoringDocument, operations: List<EditOperation>): AuthoringDocument {
        val nodes = document.nodes.toMutableMap()
        val edges = document.edges.toMutableMap()
        var entry = document.entryNodeId
        fun fail(code: String, message: String, id: String?, index: Int): Nothing = throw PayloadException(
            NodeDiagnostic(code, message, "edit", "$.operations[$index]", id, document.documentId))
        fun requireNode(id: String, index: Int): AuthorNode = nodes[id] ?: fail("NODE_NOT_FOUND", "Node does not exist", id, index)
        fun container(placement: Placement): NodeContainer = when (placement) {
            is Placement.Root -> NodeContainer.Root
            is Placement.Child -> NodeContainer.Slot(placement.parentId, placement.slot)
        }
        fun keyFor(items: List<Pair<String, String>>, previous: String?, next: String?, index: Int): String {
            val previousIndex = if (previous == null) -1 else items.indexOfFirst { it.first == previous }
            val nextIndex = if (next == null) items.size else items.indexOfFirst { it.first == next }
            if (previous != null && previousIndex < 0 || next != null && nextIndex < 0) fail(
                "STALE_NEIGHBOR", "Neighbor is missing from the destination", previous ?: next, index)
            if (previousIndex + 1 != nextIndex) fail("NEIGHBOR_GAP", "Neighbors must describe one adjacent gap", null, index)
            return try { generateKeyBetween(items.getOrNull(previousIndex)?.second, items.getOrNull(nextIndex)?.second,
                authoringLimits.maxOrderKeyLength) } catch (error: OrderKeyException) {
                fail(error.code, error.message ?: "Order key failed", null, index)
            }
        }
        fun placementFor(target: NodeContainer, previous: String?, next: String?, excluded: String?, index: Int): Placement {
            if (target is NodeContainer.Slot) requireNode(target.parentId, index)
            val siblings = nodes.values.filter { it.id != excluded && container(it.placement) == target }
                .sortedWith(compareBy({ it.placement.orderKey }, { it.id })).map { it.id to it.placement.orderKey }
            val key = keyFor(siblings, previous, next, index)
            return when (target) {
                NodeContainer.Root -> Placement.Root(key)
                is NodeContainer.Slot -> Placement.Child(target.parentId, target.slot, key)
            }
        }
        operations.forEachIndexed { index, operation ->
            when (operation) {
                is EditOperation.InsertNode -> {
                    if (operation.node.id in nodes) fail("DUPLICATE_NODE_ID", "Node ID already exists", operation.node.id, index)
                    nodes[operation.node.id] = operation.node.copy(placement = placementFor(container(operation.node.placement),
                        operation.previousId, operation.nextId, null, index))
                }
                is EditOperation.MoveNode -> {
                    val node = requireNode(operation.nodeId, index)
                    nodes[node.id] = node.copy(placement = placementFor(operation.destination,
                        operation.previousId, operation.nextId, node.id, index))
                }
                is EditOperation.RemoveNode -> {
                    requireNode(operation.nodeId, index)
                    val byParent = nodes.values.filter { it.placement is Placement.Child }.groupBy { (it.placement as Placement.Child).parentId }
                    val removed = mutableSetOf<String>()
                    val queue = mutableListOf(operation.nodeId)
                    var cursor = 0
                    while (cursor < queue.size) {
                        val id = queue[cursor++]
                        if (removed.add(id)) queue.addAll(byParent[id].orEmpty().map { it.id })
                    }
                    removed.forEach { nodes.remove(it) }
                    edges.values.filter { it.sourceNodeId in removed || it.targetNodeId in removed }.map { it.id }.forEach { edges.remove(it) }
                }
                is EditOperation.SetProperties -> {
                    val node = requireNode(operation.nodeId, index)
                    nodes[node.id] = node.copy(properties = operation.properties)
                }
                is EditOperation.PutProperty -> {
                    val node = requireNode(operation.nodeId, index)
                    nodes[node.id] = node.copy(properties = JsonObject(node.properties + (operation.name to operation.value)))
                }
                is EditOperation.RemoveProperty -> {
                    val node = requireNode(operation.nodeId, index)
                    nodes[node.id] = node.copy(properties = JsonObject(node.properties - operation.name))
                }
                is EditOperation.SetCanvasPosition -> {
                    val node = requireNode(operation.nodeId, index)
                    nodes[node.id] = node.copy(canvasPosition = operation.position)
                }
                is EditOperation.InsertEdge -> {
                    val edge = operation.edge
                    if (edge.id in edges) fail("DUPLICATE_EDGE_ID", "Edge ID already exists", edge.sourceNodeId, index)
                    requireNode(edge.sourceNodeId, index)
                    requireNode(edge.targetNodeId, index)
                    val key = if (edge.role == EdgeRole.NAVIGATION) {
                        if (operation.previousId != null || operation.nextId != null) fail("NAVIGATION_ORDER", "Navigation has no action sequence order", edge.sourceNodeId, index)
                        null
                    } else {
                        val siblings = edges.values.filter { it.role != EdgeRole.NAVIGATION && it.sourceNodeId == edge.sourceNodeId && it.sourcePort == edge.sourcePort }
                            .sortedWith(compareBy({ it.orderKey ?: "" }, { it.id })).map { it.id to it.orderKey!! }
                        keyFor(siblings, operation.previousId, operation.nextId, index)
                    }
                    edges[edge.id] = edge.copy(orderKey = key)
                }
                is EditOperation.RemoveEdge -> if (edges.remove(operation.edgeId) == null) fail(
                    "EDGE_NOT_FOUND", "Edge does not exist", null, index)
                is EditOperation.SetEntry -> entry = operation.nodeId
            }
        }
        // Keep loss-preserving wire hints consistent with edited relations.
        val indices = AuthoringIndices(document.copy(nodes = nodes, edges = edges, entryNodeId = entry))
        for (node in nodes.values.toList()) {
            val shapes = node.nodeListShapes.toMutableMap()
            for (property in catalog[node.definition]?.properties.orEmpty().filter { it.kind == PropertyKind.NODES }) {
                val count = if (property.nodeRole == NodeRole.ACTION) indices.edgesBySourcePort[SourcePort(node.id, property.name)].orEmpty().size
                    else indices.childrenByParentAndSlot[ParentSlot(node.id, property.name)].orEmpty().size
                when (shapes[property.name]) {
                    NodeListShape.OBJECT -> if (count == 0) shapes.remove(property.name) else if (count > 1) shapes[property.name] = NodeListShape.ARRAY
                    NodeListShape.STRING_OBJECT -> if (count == 0) shapes.remove(property.name) else if (count > 1) shapes[property.name] = NodeListShape.STRING_ARRAY
                    null -> if (count > 0) shapes[property.name] = NodeListShape.ARRAY
                    else -> Unit
                }
            }
            nodes[node.id] = node.copy(nodeListShapes = shapes)
        }
        return document.copy(nodes = nodes, edges = edges, entryNodeId = entry)
    }
}

private fun difference(before: AuthoringDocument, after: AuthoringDocument): DocumentPatch = DocumentPatch(
    (before.nodes.keys + after.nodes.keys).sorted().filter { before.nodes[it] != after.nodes[it] }.associateWith { after.nodes[it] },
    (before.edges.keys + after.edges.keys).sorted().filter { before.edges[it] != after.edges[it] }.associateWith { after.edges[it] },
    after.entryNodeId.takeIf { it != before.entryNodeId },
)

internal fun applyPatch(document: AuthoringDocument, patch: DocumentPatch): AuthoringDocument {
    val nodes = document.nodes.toMutableMap()
    val edges = document.edges.toMutableMap()
    patch.nodes.forEach { (id, node) -> if (node == null) nodes.remove(id) else nodes[id] = node }
    patch.edges.forEach { (id, edge) -> if (edge == null) edges.remove(id) else edges[id] = edge }
    return document.copy(nodes = nodes, edges = edges, entryNodeId = patch.entryNodeId ?: document.entryNodeId)
}
