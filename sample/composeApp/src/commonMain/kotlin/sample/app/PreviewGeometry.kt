package sample.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.positionInRoot
import me.developes.humming.sdui.common.ServerDrivenNode
import me.developes.humming.sdui.layout.components.NodeBoundary
import me.developes.humming.sdui.layout.components.NodeBoundsObserver
import me.developes.humming.sdui.studio.*

/** Copies actual renderer measurements; LayoutCoordinates are never retained across frames. */
class PreviewGeometry(private val document: AuthoringDocument, private val sourceIds: Map<String, String>) : NodeBoundsObserver {
    private data class Measurement(val full: Rect, val clipped: Rect)
    private val measurements = mutableMapOf<Pair<String, NodeBoundary>, Measurement>()
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
            val axis = when (node.definition) {
                "layout:column" -> FlowAxis.VERTICAL
                "layout:row" -> FlowAxis.HORIZONTAL
                "layout:box" -> FlowAxis.STACK
                else -> null
            }
            val clipped = world(outer.clipped)
            node.id to MeasuredNode(node.id, world(outer.full), paintOrder = paint[node.id] ?: 0,
                clips = listOf(ClipRegion(clipped)), visible = clipped.hasArea,
                container = axis?.let { ContainerBounds(it, world(inner.full)) })
        }.toMap()
        return LayoutSnapshot(document.documentId, document.revision, revision, nodes)
    }
}
