package me.developes.humming.sdui.studio

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import me.developes.humming.sdui.common.*
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class EditorTreeRow(val node: AuthorNode, val depth: Int, val relation: String, val edgeId: String? = null)

/** Local editor orchestration. The EditSession snapshot remains the only document authority. */
class LocalWorkspace(
    initialDocument: AuthoringDocument = blankDocument(),
    val catalog: NodeCatalog = initialNodeCatalog(),
) {
    var session: EditSession = EditSession(initialDocument, catalog)
        private set
    val document: AuthoringDocument get() = session.snapshot()
    private val codec = AuthoringDocumentCodec(catalog)

    fun openAuthoring(source: String): AuthoringResult<AuthoringDocument> = open(codec.decode(source))
    fun importContract(source: String): AuthoringResult<AuthoringDocument> =
        open(LegacyAuthoringCodec(catalog).importDocument(source, identity()))
    private fun open(result: AuthoringResult<AuthoringDocument>): AuthoringResult<AuthoringDocument> {
        result.value?.let { session = EditSession(it, catalog) }
        return result
    }
    fun newDocument() { session = EditSession(blankDocument(), catalog) }
    fun save(): AuthoringResult<String> = codec.encode(document)
    fun compile(): AuthoringResult<CompiledContracts> = ContractCompiler(catalog).compileTree(document,
        TargetCapabilities(catalog.capabilities.filterKeys { it.startsWith("layout:") || it.startsWith("action:") }))
    fun compilePreview(): AuthoringResult<CompiledContracts> = if (document.nodes.values.count { it.kind == AuthorNodeKind.SCREEN } > 1 ||
        document.nodes.values.any { it.definition.startsWith("navigation:") } || document.edges.values.any { it.role == EdgeRole.NAVIGATION })
        compileJourney("compiled-preview") else compile()
    fun apply(operations: List<EditOperation>): EditOutcome = session.execute(EditCommand(identity(), document.revision, operations))
    fun undo(): EditOutcome = session.undo(identity(), document.revision)
    fun redo(): EditOutcome = session.redo(identity(), document.revision)

    /** Explicit journey compilation: the caller must install this provider and retain every artifact. */
    fun compileJourney(providerType: String): AuthoringResult<CompiledContracts> =
        ContractCompiler(catalog).compileJourney(document,
            TargetCapabilities(catalog.capabilities.filterKeys {
                it.startsWith("layout:") || it.startsWith("action:") || it.startsWith("navigation:")
            }, providers = setOf(providerType)), providerType)

    fun addScreen(position: CanvasPoint): EditOutcome = apply(listOf(EditOperation.InsertNode(
        AuthorNode(identity(), AuthorNodeKind.SCREEN, "layout:column",
            JsonObject(mapOf("paddingAll" to JsonPrimitive("24"))), Placement.Root("V"), canvasPosition = position),
        previousId = AuthoringIndices(document).roots.lastOrNull()?.id)))

    /** Event and branch ports come from the shared schema, never a second port registry. */
    fun connectAction(sourceId: String, port: String, targetId: String): EditOutcome {
        val source = document.nodes[sourceId] ?: return rejected("NODE_NOT_FOUND", "Origem inexistente", sourceId)
        if (document.nodes[targetId]?.kind != AuthorNodeKind.ACTION)
            return rejected("INVALID_EDGE_ROLE", "Destino precisa ser uma ação", targetId)
        if (document.edges.values.any { it.sourceNodeId == sourceId && it.sourcePort == port && it.targetNodeId == targetId })
            return rejected("EDITOR_DUPLICATE_EDGE", "Relação já existente", sourceId)
        // Iterative and bounded by the document, including imported drafts with existing cycles.
        val outgoing = document.edges.values.filter { it.role == EdgeRole.ACTION_FLOW }.groupBy { it.sourceNodeId }
        val navigation = document.edges.values.filter { it.role == EdgeRole.NAVIGATION }.groupBy { it.sourceNodeId }
        val sources = eventSources(sourceId)
        val pending = ArrayDeque<String>().apply { add(targetId) }
        val visited = mutableSetOf<String>()
        while (pending.isNotEmpty()) {
            val id = pending.removeFirst()
            if (id == sourceId) return rejected("ACTION_CYCLE", "Conexão criaria ciclo de ações", sourceId)
            if (navigation[id].orEmpty().any { it.targetNodeId in sources })
                return rejected("EDITOR_SELF_CONNECTION", "Autoconexão visual exige roteamento de loop ainda indisponível", sourceId)
            if (visited.add(id)) outgoing[id].orEmpty().forEach { pending.add(it.targetNodeId) }
        }
        val previous = AuthoringIndices(document).edgesBySourcePort[SourcePort(sourceId, port)]?.lastOrNull()?.id
        return apply(listOf(EditOperation.InsertEdge(AuthorEdge(identity(),
            if (source.kind == AuthorNodeKind.ACTION) EdgeRole.ACTION_FLOW else EdgeRole.EVENT,
            sourceId, port, targetId, "input", "V"), previousId = previous)))
    }

    /** Retargeting and legacy destiny removal are a single undoable transaction. */
    fun setNavigation(actionId: String, screenId: String): EditOutcome {
        val action = document.nodes[actionId]
        val screen = document.nodes[screenId]
        if (action?.definition != "navigation:goTo" || action.kind != AuthorNodeKind.ACTION)
            return rejected("EDITOR_NAVIGATION", "Origem precisa ser navigation:goTo", actionId)
        if (screen?.kind != AuthorNodeKind.SCREEN || screen.placement !is Placement.Root)
            return rejected("EDITOR_NAVIGATION", "Destino precisa ser uma tela raiz", screenId)
        if (screenId in eventSources(actionId))
            return rejected("EDITOR_SELF_CONNECTION", "Autoconexão visual exige roteamento de loop ainda indisponível", actionId)
        val operations = document.edges.values.filter { it.role == EdgeRole.NAVIGATION && it.sourceNodeId == actionId }
            .mapTo(mutableListOf<EditOperation>()) { EditOperation.RemoveEdge(it.id) }
        if ("destiny" in action.properties) operations += EditOperation.RemoveProperty(actionId, "destiny")
        operations += EditOperation.InsertEdge(AuthorEdge(identity(), EdgeRole.NAVIGATION, actionId, "navigate", screenId, "entry"))
        return apply(operations)
    }

    fun disconnect(edgeId: String): EditOutcome = apply(listOf(EditOperation.RemoveEdge(edgeId)))

    fun addAction(sourceId: String, port: String, definition: String, targetScreenId: String? = null): EditOutcome {
        if (definition !in actionDefinitions) return rejected("EDITOR_DEFINITION", "Ação indisponível no editor", sourceId)
        val source = document.nodes[sourceId] ?: return rejected("NODE_NOT_FOUND", "Origem inexistente", sourceId)
        if (definition == "navigation:goTo") {
            val target = document.nodes[targetScreenId]
            if (target?.kind != AuthorNodeKind.SCREEN || target.placement !is Placement.Root)
                return rejected("EDITOR_NAVIGATION", "Selecione uma tela raiz de destino", sourceId)
            if (targetScreenId in eventSources(sourceId))
                return rejected("EDITOR_SELF_CONNECTION", "Autoconexão visual exige roteamento de loop ainda indisponível", sourceId)
        }
        val indices = AuthoringIndices(document)
        val id = identity()
        val properties = when (definition) {
            "action:update" -> mapOf("state" to JsonPrimitive("status"), "value" to JsonPrimitive("confirmado"))
            "action:ifState" -> mapOf("state" to JsonPrimitive("ready"))
            else -> emptyMap()
        }
        val operations = mutableListOf<EditOperation>(
            EditOperation.InsertNode(AuthorNode(id, AuthorNodeKind.ACTION, definition, JsonObject(properties), Placement.Root("V")),
                previousId = indices.roots.lastOrNull()?.id),
            EditOperation.InsertEdge(AuthorEdge(identity(), if (source.kind == AuthorNodeKind.ACTION) EdgeRole.ACTION_FLOW else EdgeRole.EVENT,
                sourceId, port, id, "input", "V"), previousId = indices.edgesBySourcePort[SourcePort(sourceId, port)]?.lastOrNull()?.id))
        if (definition == "navigation:goTo") operations += EditOperation.InsertEdge(
            AuthorEdge(identity(), EdgeRole.NAVIGATION, id, "navigate", targetScreenId!!, "entry"))
        return apply(operations)
    }

    fun insertVisual(parentId: String, definition: String, slot: String = "children"): EditOutcome {
        if (definition !in visualDefinitions) return rejected("EDITOR_DEFINITION", "Componente indisponível no editor", parentId)
        val indices = AuthoringIndices(document)
        val id = identity()
        val properties = when (definition) {
            "layout:text" -> mapOf("text" to JsonPrimitive("Texto"))
            "layout:textField" -> mapOf("text" to JsonPrimitive("Visitante"), "onChangeUpdateState" to JsonPrimitive("nome"))
            else -> emptyMap()
        }
        val node = AuthorNode(id, AuthorNodeKind.VISUAL, definition, JsonObject(properties), Placement.Child(parentId, slot, "V"))
        val operations = mutableListOf<EditOperation>(EditOperation.InsertNode(node,
            previousId = indices.childrenByParentAndSlot[ParentSlot(parentId, slot)]?.lastOrNull()?.id))
        if (definition == "layout:button") operations += EditOperation.InsertNode(AuthorNode(identity(), AuthorNodeKind.VISUAL,
            "layout:text", JsonObject(mapOf("text" to JsonPrimitive("Confirmar"))), Placement.Child(id, "children", "V")))
        return apply(operations)
    }

    fun addUpdate(sourceId: String, port: String): EditOutcome {
        return addAction(sourceId, port, "action:update")
    }

    /** Only known scalar fields are editable here. Relationships use commands, never raw JSON fields. */
    fun putProperty(nodeId: String, name: String, value: JsonElement?): EditOutcome {
        val node = document.nodes[nodeId] ?: return rejected("NODE_NOT_FOUND", "Nó inexistente", nodeId)
        if (name == "destiny" && value != null && document.edges.values.any {
            it.role == EdgeRole.NAVIGATION && it.sourceNodeId == nodeId
        }) return rejected("EDITOR_NAVIGATION", "Destino controlado pela relação de navegação", nodeId)
        val definition = catalog[node.definition] ?: return rejected("EDITOR_READ_ONLY", "Nó sem schema: somente leitura", nodeId)
        val property = definition.properties.find { it.name == name }
        if (property == null || !property.editable || property.kind in setOf(PropertyKind.NODES, PropertyKind.OPAQUE))
            return rejected("EDITOR_READ_ONLY", "Propriedade sem suporte de edição", nodeId)
        val properties = node.properties.toMutableMap().apply { if (value == null) remove(name) else put(name, value) }
        val wire = ServerDrivenNode(node.definition, nodeId, properties.mapValues { (_, item) ->
            if (item is JsonPrimitive) item.content else item.toString()
        }.toMutableMap())
        val errors = definition.parse(wire, ValidationMode.AUTHORING).diagnostics.filter {
            it.severity == DiagnosticSeverity.ERROR && (it.path.contains("[${JsonPrimitive(name)}]") || it.code == "PROPERTY_ORDER")
        }
        if (errors.isNotEmpty()) return EditOutcome(null, errors.map {
            it.copy(operation = "editorProperty", documentId = document.documentId)
        })
        return apply(listOf(if (value == null) EditOperation.RemoveProperty(nodeId, name) else EditOperation.PutProperty(nodeId, name, value)))
    }

    fun remove(nodeId: String): EditOutcome = apply(listOf(EditOperation.RemoveNode(nodeId)))

    fun reorder(nodeId: String, direction: Int, edgeId: String? = null): EditOutcome {
        require(direction == -1 || direction == 1)
        val indices = AuthoringIndices(document)
        if (edgeId != null) {
            val edge = document.edges[edgeId] ?: return rejected("EDGE_NOT_FOUND", "Aresta inexistente", nodeId)
            val siblings = indices.edgesBySourcePort[SourcePort(edge.sourceNodeId, edge.sourcePort)].orEmpty()
            val at = siblings.indexOfFirst { it.id == edgeId }
            val target = at + direction
            if (target !in siblings.indices) return rejected("EDITOR_ORDER", "Limite da sequência", nodeId)
            val remaining = siblings.filter { it.id != edgeId }
            return apply(listOf(EditOperation.RemoveEdge(edgeId), EditOperation.InsertEdge(edge,
                remaining.getOrNull(target - 1)?.id, remaining.getOrNull(target)?.id)))
        }
        val node = document.nodes[nodeId] ?: return rejected("NODE_NOT_FOUND", "Nó inexistente", nodeId)
        val container = when (val placement = node.placement) {
            is Placement.Root -> NodeContainer.Root
            is Placement.Child -> NodeContainer.Slot(placement.parentId, placement.slot)
        }
        val siblings = when (container) {
            NodeContainer.Root -> indices.roots
            is NodeContainer.Slot -> indices.childrenByParentAndSlot[ParentSlot(container.parentId, container.slot)].orEmpty()
        }
        val target = siblings.indexOfFirst { it.id == nodeId } + direction
        if (target !in siblings.indices) return rejected("EDITOR_ORDER", "Limite da sequência", nodeId)
        val remaining = siblings.filter { it.id != nodeId }
        return apply(listOf(EditOperation.MoveNode(nodeId, container, remaining.getOrNull(target - 1)?.id, remaining.getOrNull(target)?.id)))
    }

    fun move(nodeId: String, parentId: String, slot: String): EditOutcome {
        val last = AuthoringIndices(document).childrenByParentAndSlot[ParentSlot(parentId, slot)]
            .orEmpty().lastOrNull { it.id != nodeId }
        return apply(listOf(EditOperation.MoveNode(nodeId, NodeContainer.Slot(parentId, slot), previousId = last?.id)))
    }

    fun tree(): List<EditorTreeRow> {
        val indices = AuthoringIndices(document)
        val rows = mutableListOf<EditorTreeRow>()
        val visited = mutableSetOf<String>()
        val childSlots = indices.childrenByParentAndSlot.entries.groupBy { it.key.parentId }
        val actionPorts = indices.edgesBySourcePort.entries.groupBy { it.key.sourceNodeId }
        fun visit(node: AuthorNode, depth: Int, relation: String, edgeId: String? = null) {
            rows += EditorTreeRow(node, depth, relation, edgeId)
            if (!visited.add(node.id)) return
            childSlots[node.id].orEmpty().sortedBy { it.key.slot }.forEach { (key, children) ->
                children.forEach { visit(it, depth + 1, key.slot) }
            }
            actionPorts[node.id].orEmpty().sortedBy { it.key.port }.forEach { (key, edges) ->
                edges.filter { it.role != EdgeRole.NAVIGATION }.forEach {
                    visit(document.nodes.getValue(it.targetNodeId), depth + 1, key.port, it.id)
                }
            }
        }
        indices.roots.filter { indices.incomingEdges[it.id].isNullOrEmpty() }.forEach { visit(it, 0, "raiz") }
        indices.roots.forEach { if (it.id !in visited) visit(it, 0, "raiz") }
        return rows
    }

    private fun eventSources(id: String): Set<String> {
        val incoming = document.edges.values.filter { it.role != EdgeRole.NAVIGATION }.groupBy { it.targetNodeId }
        val pending = ArrayDeque<String>().apply { add(id) }
        val seen = mutableSetOf<String>()
        val sources = mutableSetOf<String>()
        while (pending.isNotEmpty()) {
            val next = pending.removeFirst()
            if (!seen.add(next)) continue
            if (document.nodes[next]?.kind != AuthorNodeKind.ACTION) sources.add(next)
            else incoming[next].orEmpty().forEach { pending.add(it.sourceNodeId) }
        }
        return sources
    }

    private fun rejected(code: String, message: String, id: String) = EditOutcome(null,
        listOf(NodeDiagnostic(code, message, "editor", nodeId = id, documentId = document.documentId)))

    companion object {
        val actionDefinitions = listOf("action:update", "action:ifState", "action:then", "navigation:goTo", "navigation:goBack")
        val visualDefinitions = listOf("layout:column", "layout:row", "layout:box", "layout:text", "layout:textField", "layout:button")
        @OptIn(ExperimentalUuidApi::class)
        private fun identity() = Uuid.random().toString()
        fun blankDocument(): AuthoringDocument {
            val root = AuthorNode(identity(), AuthorNodeKind.SCREEN, "layout:column",
                JsonObject(mapOf("paddingAll" to JsonPrimitive("24"))), Placement.Root("V"))
            return AuthoringDocument(identity(), entryNodeId = root.id, nodes = mapOf(root.id to root))
        }
    }
}
