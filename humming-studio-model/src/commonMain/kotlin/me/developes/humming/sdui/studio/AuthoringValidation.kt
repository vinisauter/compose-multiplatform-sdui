package me.developes.humming.sdui.studio

import me.developes.humming.sdui.common.DiagnosticSeverity
import me.developes.humming.sdui.common.NodeCatalog
import me.developes.humming.sdui.common.NodeDiagnostic
import me.developes.humming.sdui.common.NodeRole
import me.developes.humming.sdui.common.PropertyKind
import me.developes.humming.sdui.common.PayloadException
import me.developes.humming.sdui.common.ServerDrivenNode
import me.developes.humming.sdui.common.qualifiedName

internal fun AuthorNodeKind.matches(role: NodeRole): Boolean = when (role) {
    NodeRole.COMPONENT -> this == AuthorNodeKind.VISUAL || this == AuthorNodeKind.SCREEN
    NodeRole.ACTION -> this == AuthorNodeKind.ACTION
    NodeRole.NAVIGATION_METADATA -> this == AuthorNodeKind.NAVIGATION
}

fun validateAuthoringDocument(
    document: AuthoringDocument,
    catalog: NodeCatalog,
    limits: AuthoringLimits = AuthoringLimits(),
): List<NodeDiagnostic> {
    val diagnostics = mutableListOf<NodeDiagnostic>()
    fun report(code: String, message: String, nodeId: String? = null, path: String = "$",
               severity: DiagnosticSeverity = DiagnosticSeverity.ERROR) {
        diagnostics += NodeDiagnostic(code, message, "authoring", path, nodeId, document.documentId, severity)
    }
    if (document.documentId.isBlank()) report("DOCUMENT_ID", "Document ID must be non-empty")
    if (document.formatVersion != 1) report("AUTHORING_VERSION", "Unsupported authoring format version")
    if (document.revision < 0) report("DOCUMENT_REVISION", "Revision must be non-negative")
    if (document.nodes.size > limits.maxNodes || document.edges.size > limits.maxEdges) {
        report("AUTHORING_BUDGET", "Document node/edge budget exceeded")
        return diagnostics
    }
    val entry = document.nodes[document.entryNodeId]
    if (entry == null) report("MISSING_ENTRY", "Entry node does not exist", document.entryNodeId)
    else if (entry.placement !is Placement.Root || entry.kind == AuthorNodeKind.ACTION || entry.kind == AuthorNodeKind.NAVIGATION) {
        report("INVALID_ENTRY", "Entry must be a root screen or visual node", entry.id)
    }
    val nodeIds = mutableSetOf<String>()
    val orderGroups = mutableMapOf<Pair<String?, String>, MutableSet<String>>()
    for ((key, node) in document.nodes.entries.sortedBy { it.key }) {
        val path = "$.nodes[$key]"
        if (node.id.isBlank() || key != node.id) report("NODE_ID", "Map key must equal a non-empty node ID", node.id, path)
        if (!nodeIds.add(node.id)) report("DUPLICATE_NODE_ID", "Node IDs must be unique", node.id, path)
        try { ServerDrivenNode(node.definition, node.id).qualifiedName() } catch (error: PayloadException) {
            diagnostics += error.diagnostic.copy(operation = "authoring", path = "$path.definition", documentId = document.documentId)
        }
        if (!validOrderKey(node.placement.orderKey, limits.maxOrderKeyLength)) report("ORDER_KEY", "Invalid order key", node.id, "$path.placement")
        if (node.canvasPosition?.let { !it.x.isFinite() || !it.y.isFinite() } == true) report("CANVAS_POSITION", "Coordinates must be finite", node.id, path)
        val definition = catalog[node.definition]
        if (definition == null) report("UNMODELED_DEFINITION", "Node is preserved without editing support", node.id, path, DiagnosticSeverity.WARNING)
        else if (!node.kind.matches(definition.role)) report("AUTHOR_NODE_ROLE", "Node kind does not match its definition", node.id, path)
        val placement = node.placement
        val group = when (placement) {
            is Placement.Root -> null to "root"
            is Placement.Child -> {
                val parent = document.nodes[placement.parentId]
                if (parent == null) report("MISSING_PARENT", "Parent does not exist", node.id, "$path.placement")
                if (node.kind == AuthorNodeKind.ACTION) report("ACTION_CONTAINMENT", "Actions use edges and root placement", node.id, path)
                val parentDefinition = parent?.let { catalog[it.definition] }
                if (parentDefinition != null) {
                    val expected = if (placement.slot == "children") parentDefinition.childrenRole else
                        parentDefinition.properties.firstOrNull { it.name == placement.slot && it.kind == PropertyKind.NODES }?.nodeRole
                    if (expected == null || expected == NodeRole.ACTION) report("INVALID_SLOT", "Slot is not structural", node.id, "$path.placement.slot")
                    else if (definition != null && !node.kind.matches(expected)) report("INVALID_SLOT_ROLE", "Child role is not allowed in this slot", node.id, path)
                } else if (parent != null && placement.slot != "children") {
                    report("UNMODELED_SLOT", "Unknown structural slots cannot be interpreted", node.id, path)
                }
                placement.parentId to placement.slot
            }
        }
        if (!orderGroups.getOrPut(group) { mutableSetOf() }.add(node.placement.orderKey)) report(
            "DUPLICATE_ORDER_KEY", "Sibling order keys must be unique", node.id, "$path.placement")
        if (node.extensions.keys.any { it in setOf("component", "id", "properties", "children") }) report(
            "RESERVED_EXTENSION", "Extension collides with a wire field", node.id, "$path.extensions")
        for (port in node.nodeListShapes.keys) {
            if (definition?.properties?.none { it.name == port && it.kind == PropertyKind.NODES } != false) report(
                "UNMODELED_PORT", "Node-list representation requires a defined port", node.id, "$path.nodeListShapes")
            if (port in node.properties) report("DUPLICATE_RELATION", "Raw property duplicates a structural/event relation", node.id, "$path.properties")
        }
        if (definition?.properties?.any { it.kind == PropertyKind.NODES && it.name in node.properties } == true) {
            report("DUPLICATE_RELATION", "Known node-list properties must use placements/edges", node.id, "$path.properties")
        }
    }
    // Memoized parent depth also detects cycles, independently of map iteration order.
    val depthByNode = mutableMapOf<String, Int>()
    val activeParents = mutableSetOf<String>()
    fun containmentDepth(id: String, traversalDepth: Int): Int {
        depthByNode[id]?.let { return it }
        if (!activeParents.add(id)) {
            report("CONTAINMENT_CYCLE", "Containment must be acyclic", id)
            return limits.maxDepth + 1
        }
        if (traversalDepth > limits.maxDepth) {
            report("CONTAINMENT_DEPTH", "Containment depth budget exceeded", id)
            activeParents.remove(id)
            return limits.maxDepth + 1
        }
        val parent = (document.nodes[id]?.placement as? Placement.Child)?.parentId
        val depth = if (parent != null && parent in document.nodes) 1 + containmentDepth(parent, traversalDepth + 1) else 1
        activeParents.remove(id)
        depthByNode[id] = depth
        if (depth > limits.maxDepth) report("CONTAINMENT_DEPTH", "Containment depth budget exceeded", id)
        return depth
    }
    document.nodes.keys.sorted().forEach { containmentDepth(it, 1) }

    val edgeIds = mutableSetOf<String>()
    val edgeOrder = mutableMapOf<SourcePort, MutableSet<String>>()
    for ((key, edge) in document.edges.entries.sortedBy { it.key }) {
        val path = "$.edges[$key]"
        if (edge.id.isBlank() || key != edge.id) report("EDGE_ID", "Map key must equal a non-empty edge ID", edge.sourceNodeId, path)
        if (!edgeIds.add(edge.id)) report("DUPLICATE_EDGE_ID", "Edge IDs must be unique", edge.sourceNodeId, path)
        val source = document.nodes[edge.sourceNodeId]
        val target = document.nodes[edge.targetNodeId]
        if (source == null || target == null) { report("MISSING_EDGE_NODE", "Edge endpoint does not exist", edge.sourceNodeId, path); continue }
        if (edge.role == EdgeRole.NAVIGATION) {
            if (edge.sourcePort != "navigate" || edge.targetPort != "entry" ||
                target.kind !in setOf(AuthorNodeKind.SCREEN, AuthorNodeKind.NAVIGATION)) report(
                "NAVIGATION_PORT", "Navigation uses navigate -> entry and a screen/route target", source.id, path)
            continue // Navigation cycles do not form containment or action cycles.
        }
        val property = catalog[source.definition]?.properties?.firstOrNull { it.name == edge.sourcePort }
        if (property?.kind != PropertyKind.NODES || property.nodeRole != NodeRole.ACTION || edge.targetPort != "input") report(
            "INVALID_EDGE_PORT", "Action edges require a declared action port and input target", source.id, path)
        if (target.kind != AuthorNodeKind.ACTION ||
            (edge.role == EdgeRole.ACTION_FLOW) != (source.kind == AuthorNodeKind.ACTION)) report(
            "INVALID_EDGE_ROLE", "Event/action-flow endpoints have incompatible roles", source.id, path)
        val order = edge.orderKey
        if (order == null || !validOrderKey(order, limits.maxOrderKeyLength)) report("ORDER_KEY", "Action edge needs a valid order key", source.id, path)
        else if (!edgeOrder.getOrPut(SourcePort(source.id, edge.sourcePort)) { mutableSetOf() }.add(order)) report(
            "DUPLICATE_EDGE_ORDER", "Edges on one port need unique order keys", source.id, path)
    }
    val indices = AuthoringIndices(document)
    for (node in document.nodes.values) for ((port, shape) in node.nodeListShapes) {
        val role = catalog[node.definition]?.properties?.firstOrNull { it.name == port }?.nodeRole
        val count = if (role == NodeRole.ACTION) indices.edgesBySourcePort[SourcePort(node.id, port)].orEmpty().size
            else indices.childrenByParentAndSlot[ParentSlot(node.id, port)].orEmpty().size
        if (shape in setOf(NodeListShape.OBJECT, NodeListShape.STRING_OBJECT) && count != 1) report(
            "PORT_CARDINALITY", "Object representation requires exactly one related node", node.id, "$.nodes[${node.id}].nodeListShapes")
    }
    val activeActions = mutableSetOf<String>()
    val actionDepths = mutableMapOf<String, Int>()
    val flow = document.edges.values.filter { it.role == EdgeRole.ACTION_FLOW }.groupBy { it.sourceNodeId }
    fun checkAction(id: String, depth: Int): Int {
        actionDepths[id]?.let { return it }
        if (!activeActions.add(id)) { report("ACTION_CYCLE", "Action-flow cycles are unsupported", id); return limits.maxDepth + 1 }
        if (depth > limits.maxDepth) {
            report("ACTION_DEPTH", "Action-flow depth budget exceeded", id)
            activeActions.remove(id)
            return limits.maxDepth + 1
        }
        val height = 1 + (flow[id].orEmpty().sortedBy { it.id }.maxOfOrNull { checkAction(it.targetNodeId, depth + 1) } ?: 0)
        activeActions.remove(id)
        actionDepths[id] = height
        if (height > limits.maxDepth) report("ACTION_DEPTH", "Action-flow depth budget exceeded", id)
        return height
    }
    document.nodes.keys.sorted().forEach { checkAction(it, 1) }
    return diagnostics
}
