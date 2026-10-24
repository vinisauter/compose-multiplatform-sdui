package me.developes.humming.sdui.studio

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import me.developes.humming.sdui.common.DiagnosticSeverity
import me.developes.humming.sdui.common.NodeCatalog
import me.developes.humming.sdui.common.NodeDiagnostic
import me.developes.humming.sdui.common.NodeRole
import me.developes.humming.sdui.common.PayloadException
import me.developes.humming.sdui.common.PayloadLimits
import me.developes.humming.sdui.common.PropertyKind
import me.developes.humming.sdui.common.ServerDrivenJson
import me.developes.humming.sdui.common.ServerDrivenNode
import me.developes.humming.sdui.common.parseBoundedJson
import me.developes.humming.sdui.common.parseNode
import me.developes.humming.sdui.common.validateBoundedJson

/** Loss-preserving local bridge, not the capability-checking compiler of the next milestone. */
class LegacyAuthoringCodec(
    private val catalog: NodeCatalog,
    private val limits: AuthoringLimits = AuthoringLimits(),
    private val payloadLimits: PayloadLimits = PayloadLimits(),
) {
    fun importDocument(source: String, documentId: String): AuthoringResult<AuthoringDocument> {
        val diagnostics = mutableListOf<NodeDiagnostic>()
        fun fail(code: String, message: String, id: String? = null): Nothing = throw PayloadException(
            NodeDiagnostic(code, message, "legacyImport", nodeId = id, documentId = documentId))
        return try {
            val root = parseNode(source, payloadLimits, documentId)
            diagnostics += catalog.validate(root, maxNodes = limits.maxNodes, maxDepth = limits.maxDepth)
            if (diagnostics.any { it.severity == DiagnosticSeverity.ERROR }) return AuthoringResult(null, diagnostics)
            val nodes = linkedMapOf<String, AuthorNode>()
            val edges = linkedMapOf<String, AuthorEdge>()
            var roots = 0
            fun rootPlacement() = Placement.Root(importOrderKey(roots++))
            fun visit(node: ServerDrivenNode, placement: Placement, expected: NodeRole, depth: Int) {
                if (depth > limits.maxDepth || nodes.size >= limits.maxNodes) fail("AUTHORING_BUDGET", "Import budget exceeded", node.id)
                if (node.id in nodes) fail("DUPLICATE_NODE_ID", "Legacy node IDs must be unique", node.id)
                val definition = catalog[node.component]
                val role = definition?.role ?: expected
                val kind = when (role) {
                    NodeRole.ACTION -> AuthorNodeKind.ACTION
                    NodeRole.NAVIGATION_METADATA -> AuthorNodeKind.NAVIGATION
                    NodeRole.COMPONENT -> when {
                        definition == null -> AuthorNodeKind.UNKNOWN
                        node.id == root.id -> AuthorNodeKind.SCREEN
                        else -> AuthorNodeKind.VISUAL
                    }
                }
                val original = (node as? ServerDrivenJson)?.json.orEmpty()
                val rawProperties = (original["properties"] as? JsonObject)?.toMap()
                    ?: node.properties.orEmpty().mapValues { JsonPrimitive(it.value) }
                val scalar = rawProperties.toMutableMap()
                val shapes = linkedMapOf<String, NodeListShape>()
                val lists = linkedMapOf<String, List<ServerDrivenNode>>()
                for (property in definition?.properties.orEmpty().filter { it.kind == PropertyKind.NODES }.sortedBy { it.name }) {
                    val raw = rawProperties[property.name] ?: continue
                    val embedded = if (raw is JsonPrimitive && raw.isString) parseBoundedJson(raw.content, payloadLimits, documentId) else raw
                    val array = embedded is JsonArray
                    shapes[property.name] = when {
                        raw is JsonPrimitive && array -> NodeListShape.STRING_ARRAY
                        raw is JsonPrimitive -> NodeListShape.STRING_OBJECT
                        array -> NodeListShape.ARRAY
                        else -> NodeListShape.OBJECT
                    }
                    lists[property.name] = node.propertyNodes(property.name)
                    scalar.remove(property.name)
                }
                nodes[node.id] = AuthorNode(node.id, kind, node.component, JsonObject(scalar), placement,
                    nodeListShapes = shapes, hasProperties = node.properties != null, hasChildren = node.children != null,
                    extensions = JsonObject(original.filterKeys { it !in setOf("component", "id", "properties", "children") }))
                node.children.orEmpty().forEachIndexed { index, child ->
                    visit(child, Placement.Child(node.id, "children", importOrderKey(index)), definition?.childrenRole ?: NodeRole.COMPONENT, depth + 1)
                }
                for ((port, children) in lists) {
                    val property = definition!!.properties.first { it.name == port }
                    children.forEachIndexed { index, child ->
                        if (property.nodeRole == NodeRole.ACTION) {
                            if (edges.size >= limits.maxEdges) fail("AUTHORING_BUDGET", "Edge budget exceeded", node.id)
                            val edgeId = "edge-${edges.size + 1}"
                            edges[edgeId] = AuthorEdge(edgeId,
                                if (kind == AuthorNodeKind.ACTION) EdgeRole.ACTION_FLOW else EdgeRole.EVENT,
                                node.id, port, child.id, "input", importOrderKey(index))
                            visit(child, rootPlacement(), NodeRole.ACTION, depth + 1)
                        } else visit(child, Placement.Child(node.id, port, importOrderKey(index)), property.nodeRole!!, depth + 1)
                    }
                }
            }
            visit(root, rootPlacement(), NodeRole.COMPONENT, 1)
            val document = AuthoringDocument(documentId, entryNodeId = root.id, nodes = nodes, edges = edges)
            diagnostics += validateAuthoringDocument(document, catalog, limits)
            AuthoringResult(document.takeIf { diagnostics.none { it.severity == DiagnosticSeverity.ERROR } }, diagnostics.distinct())
        } catch (error: PayloadException) { AuthoringResult(null, (diagnostics + error.diagnostic).distinct()) }
    }

    fun exportDocument(document: AuthoringDocument, rootId: String = document.entryNodeId): AuthoringResult<String> {
        val diagnostics = validateAuthoringDocument(document, catalog, limits).toMutableList()
        fun report(code: String, message: String, id: String? = rootId,
                   severity: DiagnosticSeverity = DiagnosticSeverity.ERROR) = NodeDiagnostic(
            code, message, "legacyExport", nodeId = id, documentId = document.documentId, severity = severity)
        if (document.edges.values.any { it.role == EdgeRole.NAVIGATION }) diagnostics += report(
            "NAVIGATION_COMPILATION_REQUIRED", "Explicit navigation edges require semantic compilation; authoring serialization preserves them")
        if (rootId !in document.nodes) diagnostics += report("MISSING_EXPORT_ROOT", "Selected export root does not exist")
        if (diagnostics.any { it.severity == DiagnosticSeverity.ERROR }) return AuthoringResult(null, diagnostics)
        val indices = AuthoringIndices(document)
        val reached = mutableSetOf<String>()
        var expanded = 0
        return try {
            fun materialize(id: String, depth: Int): JsonObject {
                if (++expanded > limits.maxNodes || depth > limits.maxDepth) throw PayloadException(report(
                    "AUTHORING_BUDGET", "Legacy expansion budget exceeded", id))
                if (id in reached) throw PayloadException(report("SHARED_NODE_COMPILATION_REQUIRED",
                    "Shared graph nodes require an explicit expansion/identity policy before tree export", id))
                val node = document.nodes.getValue(id)
                reached += id
                val output = node.extensions.toMutableMap()
                output["component"] = JsonPrimitive(node.definition)
                output["id"] = JsonPrimitive(node.id)
                val properties = node.properties.toMutableMap()
                val definition = catalog[node.definition]
                for (property in definition?.properties.orEmpty().filter { it.kind == PropertyKind.NODES }.sortedBy { it.name }) {
                    val related = if (property.nodeRole == NodeRole.ACTION)
                        indices.edgesBySourcePort[SourcePort(id, property.name)].orEmpty().map { it.targetNodeId }
                    else indices.childrenByParentAndSlot[ParentSlot(id, property.name)].orEmpty().map { it.id }
                    val shape = node.nodeListShapes[property.name]
                    if (related.isEmpty() && shape == null) continue
                    val nodes = related.map { materialize(it, depth + 1) }
                    val element: JsonElement = when (shape) {
                        NodeListShape.OBJECT, NodeListShape.STRING_OBJECT -> nodes.single()
                        else -> JsonArray(nodes)
                    }
                    properties[property.name] = if (shape in setOf(NodeListShape.STRING_ARRAY, NodeListShape.STRING_OBJECT))
                        JsonPrimitive(canonicalJson(element).toString()) else element
                }
                if (node.hasProperties || properties.isNotEmpty()) output["properties"] = JsonObject(properties)
                val children = indices.childrenByParentAndSlot[ParentSlot(id, "children")].orEmpty()
                if (node.hasChildren || children.isNotEmpty()) output["children"] = JsonArray(children.map { materialize(it.id, depth + 1) })
                return JsonObject(output)
            }
            val root = materialize(rootId, 1)
            validateBoundedJson(root, payloadLimits, document.documentId)
            val encoded = canonicalJson(root).toString()
            val parsed = parseNode(encoded, payloadLimits, document.documentId)
            diagnostics += catalog.validate(parsed, maxNodes = limits.maxNodes, maxDepth = limits.maxDepth)
            val omitted = document.nodes.keys - reached
            if (omitted.isNotEmpty()) diagnostics += report("UNREACHED_NODES",
                "Selected root excludes ${omitted.size} nodes; they remain in the authoring document", severity = DiagnosticSeverity.WARNING)
            AuthoringResult(encoded.takeIf { diagnostics.none { it.severity == DiagnosticSeverity.ERROR } }, diagnostics.distinct())
        } catch (error: PayloadException) { AuthoringResult(null, (diagnostics + error.diagnostic).distinct()) }
    }
}
