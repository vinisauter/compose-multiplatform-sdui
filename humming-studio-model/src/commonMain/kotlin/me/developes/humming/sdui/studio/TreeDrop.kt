package me.developes.humming.sdui.studio

import me.developes.humming.sdui.common.NodeCatalog
import me.developes.humming.sdui.common.NodeDiagnostic

enum class TreeDropPosition { BEFORE, INSIDE, AFTER }
data class TreeDropPlan(val documentId: String, val revision: Long, val sourceId: String, val targetId: String,
                        val position: TreeDropPosition, val operation: EditOperation.MoveNode)

/** Tree ordering is structural, independent of renderer bounds and viewport. */
object TreeDrop {
    fun plan(document: AuthoringDocument, sourceId: String, targetId: String, position: TreeDropPosition,
             catalog: NodeCatalog, preferredSlot: String? = null): AuthoringResult<TreeDropPlan> {
        fun fail(message: String) = AuthoringResult<TreeDropPlan>(null, listOf(NodeDiagnostic(
            "INVALID_TREE_DROP", message, "treeDrop", nodeId = sourceId, documentId = document.documentId)))
        val source = document.nodes[sourceId] ?: return fail("Origem inexistente")
        val target = document.nodes[targetId] ?: return fail("Destino inexistente")
        if (source.kind != AuthorNodeKind.VISUAL || source.placement !is Placement.Child)
            return fail("Arraste somente elementos visuais contidos em uma tela")
        if (sourceId == targetId) return fail("Origem e destino são o mesmo elemento")
        val destination = if (position == TreeDropPosition.INSIDE) {
            val slots = catalog[target.definition]?.let(DropGeometry::visualSlots).orEmpty()
            val slot = preferredSlot?.takeIf { it in slots } ?: slots.firstOrNull() ?: return fail("O destino não aceita filhos visuais")
            NodeContainer.Slot(targetId, slot)
        } else {
            val placement = target.placement as? Placement.Child ?: return fail("Use o centro da tela para inserir dentro dela")
            val parent = document.nodes[placement.parentId] ?: return fail("Container inexistente")
            if (placement.slot !in catalog[parent.definition]?.let(DropGeometry::visualSlots).orEmpty()) return fail("Slot não visual")
            NodeContainer.Slot(placement.parentId, placement.slot)
        }
        var parent: String? = destination.parentId
        val visited = mutableSetOf<String>()
        while (parent != null && visited.add(parent)) {
            if (parent == sourceId) return fail("Não é possível mover um elemento para si ou seus descendentes")
            parent = (document.nodes[parent]?.placement as? Placement.Child)?.parentId
        }
        val siblings = AuthoringIndices(document).childrenByParentAndSlot[ParentSlot(destination.parentId, destination.slot)].orEmpty().filter { it.id != sourceId }
        val index = if (position == TreeDropPosition.INSIDE) siblings.size else {
            val at = siblings.indexOfFirst { it.id == targetId }
            if (at < 0) return fail("Destino fora do slot")
            at + if (position == TreeDropPosition.AFTER) 1 else 0
        }
        return AuthoringResult(TreeDropPlan(document.documentId, document.revision, sourceId, targetId, position,
            EditOperation.MoveNode(sourceId, destination, siblings.getOrNull(index - 1)?.id, siblings.getOrNull(index)?.id)), emptyList())
    }

    fun commit(workspace: LocalWorkspace, plan: TreeDropPlan): EditOutcome {
        if (workspace.document.documentId != plan.documentId || workspace.document.revision != plan.revision)
            return EditOutcome(null, listOf(NodeDiagnostic("STALE_GESTURE", "Documento mudou durante o arraste", "treeDrop", nodeId = plan.sourceId, documentId = plan.documentId)))
        return workspace.apply(listOf(plan.operation))
    }
}
