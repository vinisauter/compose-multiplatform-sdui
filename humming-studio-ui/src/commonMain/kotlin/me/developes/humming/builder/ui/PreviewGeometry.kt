package me.developes.humming.builder.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.positionInRoot
import me.developes.humming.sdui.common.ServerDrivenNode
import me.developes.humming.sdui.common.NodeCatalog
import me.developes.humming.sdui.common.initialNodeCatalog
import me.developes.humming.sdui.layout.components.NodeBoundary
import me.developes.humming.sdui.layout.components.NodeBoundsObserver
import me.developes.humming.sdui.layout.components.NodeContentFlow
import me.developes.humming.sdui.studio.*

/** Copies actual renderer measurements; LayoutCoordinates are never retained across frames. */
class PreviewGeometry(private val document: AuthoringDocument, private val sourceIds: Map<String, String>,
                      private val catalog: NodeCatalog = initialNodeCatalog(),
                      private val preferredSlots: Map<String, String> = emptyMap()) : NodeBoundsObserver {
    private data class Measurement(val full: Rect, val clipped: Rect)
    private val measurements = mutableMapOf<Pair<String, NodeBoundary>, Measurement>()
    private val flows = mutableMapOf<String, NodeContentFlow>()
    private val indices = AuthoringIndices(document)
    private val paint = LocalWorkspace(document).tree().mapIndexed { index, row -> row.node.id to index }.toMap()
    var revision by mutableLongStateOf(0)
        private set
    private var origin = Offset.Zero
    fun placeViewport(coordinates: LayoutCoordinates) {
        val next = coordinates.positionInRoot()
        if (next != origin) { origin = next; revision++ }
    }
    override fun placed(node: ServerDrivenNode, boundary: NodeBoundary, coordinates: LayoutCoordinates) {
        val id = sourceIds[node.id] ?: return
        if (!coordinates.isAttached) return
        val points = listOf(Offset.Zero, Offset(coordinates.size.width.toFloat(), 0f),
            Offset(0f, coordinates.size.height.toFloat()), Offset(coordinates.size.width.toFloat(), coordinates.size.height.toFloat()))
            .map { coordinates.localToRoot(it) }
        val next = Measurement(Rect(points.minOf { it.x }, points.minOf { it.y }, points.maxOf { it.x }, points.maxOf { it.y }), coordinates.boundsInRoot())
        val key = id to boundary
        if (measurements[key] != next) { measurements[key] = next; revision++ }
    }
    override fun removed(nodeId: String) {
        val id = sourceIds[nodeId] ?: return
        if (measurements.keys.removeAll { it.first == id }) revision++
        if (flows.remove(id) != null) revision++
    }
    override fun contentFlow(nodeId: String, flow: NodeContentFlow) {
        val id = sourceIds[nodeId] ?: return
        if (flows[id] != flow) { flows[id] = flow; revision++ }
    }
    fun snapshot(viewport: CanvasViewport): LayoutSnapshot {
        fun world(rect: Rect): Rect2 {
            val a = viewport.toWorld(Point2((rect.left - origin.x).toDouble(), (rect.top - origin.y).toDouble()))
            val b = viewport.toWorld(Point2((rect.right - origin.x).toDouble(), (rect.bottom - origin.y).toDouble()))
            return Rect2(a.x, a.y, maxOf(0.0, b.x - a.x), maxOf(0.0, b.y - a.y))
        }
        val nodes = document.nodes.values.mapNotNull { node ->
            val outer = measurements[node.id to NodeBoundary.OUTER] ?: return@mapNotNull null
            val inner = measurements[node.id to NodeBoundary.CONTENT] ?: return@mapNotNull null
            val axis = when (flows[node.id]) {
                NodeContentFlow.HORIZONTAL -> FlowAxis.HORIZONTAL
                NodeContentFlow.VERTICAL -> FlowAxis.VERTICAL
                null -> when (node.definition) {
                "layout:column" -> FlowAxis.VERTICAL
                "layout:row" -> FlowAxis.HORIZONTAL
                "layout:box" -> FlowAxis.STACK
                else -> FlowAxis.STACK
                }
            }
            val allowed = catalog[node.definition]?.let(DropGeometry::visualSlots).orEmpty()
            val preferred = preferredSlots[node.id]?.takeIf { it in allowed }
            val candidates = allowed.mapNotNull { slot ->
                val children = indices.childrenByParentAndSlot[ParentSlot(node.id, slot)].orEmpty()
                val bounds = children.mapNotNull { measurements[it.id to NodeBoundary.OUTER]?.clipped }.filter { it.width > 0 && it.height > 0 }
                val region = when {
                    slot == preferred || slot == "children" -> world(inner.full)
                    bounds.isNotEmpty() -> world(Rect(bounds.minOf { it.left }, bounds.minOf { it.top }, bounds.maxOf { it.right }, bounds.maxOf { it.bottom }))
                    allowed.size == 1 || preferred == null && slot == allowed.first() -> world(inner.full)
                    else -> null
                }
                val slotAxis = if (slot == "children") axis else FlowAxis.STACK
                region?.let { ContainerBounds(slotAxis, it, slot,
                    if (slotAxis == FlowAxis.STACK) DropGuideKind.CONTAINER else DropGuideKind.LINE) }
            }.sortedBy { if (it.slot == preferred) 0 else if (it.slot == "children") 2 else 1 }
            val clipped = world(outer.clipped)
            node.id to MeasuredNode(node.id, world(outer.full), paintOrder = paint[node.id] ?: 0,
                clips = listOf(ClipRegion(clipped)), visible = clipped.hasArea,
                container = candidates.firstOrNull(), slots = candidates.drop(1))
        }.toMap()
        return LayoutSnapshot(document.documentId, document.revision, revision, nodes)
    }
}
