package me.developes.humming.sdui.studio

import me.developes.humming.sdui.common.NodeDiagnostic

/** One possible navigation relation reachable from an event, not a prediction of its condition. */
data class NavigationLink(val eventEdgeId: String, val navigationEdgeId: String,
                          val sourceNodeId: String, val targetScreenId: String, val trigger: String)

object InteractionProjection {
    fun navigationLinks(document: AuthoringDocument, maxLinks: Int = 4096, maxVisits: Int = 100_000): AuthoringResult<List<NavigationLink>> {
        require(maxLinks > 0 && maxVisits > 0)
        val edges = document.edges.values.sortedBy { it.id }
        val flow = edges.filter { it.role == EdgeRole.ACTION_FLOW }.groupBy { it.sourceNodeId }
        val navigation = edges.filter { it.role == EdgeRole.NAVIGATION }.groupBy { it.sourceNodeId }
        val links = mutableListOf<NavigationLink>()
        var visits = 0
        for (event in edges.filter { it.role == EdgeRole.EVENT }) {
            val pending = ArrayDeque<String>().apply { add(event.targetNodeId) }
            val seen = mutableSetOf<String>()
            while (pending.isNotEmpty()) {
                val id = pending.removeFirst()
                if (!seen.add(id)) continue
                if (++visits > maxVisits) return exceeded(document, event.sourceNodeId)
                navigation[id].orEmpty().forEach { edge ->
                    if (links.size == maxLinks) return exceeded(document, event.sourceNodeId)
                    links += NavigationLink(event.id, edge.id, event.sourceNodeId, edge.targetNodeId, event.sourcePort)
                }
                flow[id].orEmpty().forEach { pending.add(it.targetNodeId) }
            }
        }
        return AuthoringResult(links.toList(), emptyList())
    }

    private fun exceeded(document: AuthoringDocument, id: String) = AuthoringResult<List<NavigationLink>>(null,
        listOf(NodeDiagnostic("INTERACTION_LIMIT", "Orçamento de projeção das interações excedido", "interactionProjection",
            nodeId = id, documentId = document.documentId)))
}
