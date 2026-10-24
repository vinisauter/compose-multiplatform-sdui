package me.developes.humming.sdui.studio

import me.developes.humming.sdui.common.NodeCatalog
import me.developes.humming.sdui.common.NodeDiagnostic
import me.developes.humming.sdui.common.NodeRole
import kotlin.math.abs

data class Point2(val x: Double, val y: Double) {
    init { require(x.isFinite() && y.isFinite()) }
}

data class Rect2(val x: Double, val y: Double, val width: Double, val height: Double) {
    init { require(listOf(x, y, width, height, x + width, y + height).all { it.isFinite() } && width >= 0 && height >= 0) }
    val right get() = x + width
    val bottom get() = y + height
    val hasArea get() = width > 0 && height > 0
    fun contains(p: Point2) = p.x >= x && p.x <= right && p.y >= y && p.y <= bottom
    fun corners() = listOf(Point2(x, y), Point2(right, y), Point2(right, bottom), Point2(x, bottom))
}

/** [a c tx; b d ty; 0 0 1], from node local coordinates to world. */
data class Affine2(val a: Double = 1.0, val b: Double = 0.0, val c: Double = 0.0,
                   val d: Double = 1.0, val tx: Double = 0.0, val ty: Double = 0.0) {
    init { require(listOf(a, b, c, d, tx, ty).all { it.isFinite() }) }
    fun apply(p: Point2) = Point2(a * p.x + c * p.y + tx, b * p.x + d * p.y + ty)
    fun inverse(): Affine2? {
        val determinant = a * d - b * c
        if (!determinant.isFinite() || abs(determinant) < 1e-12) return null
        val values = listOf(d / determinant, -b / determinant, -c / determinant, a / determinant,
            (c * ty - d * tx) / determinant, (b * tx - a * ty) / determinant)
        return if (values.all { it.isFinite() }) Affine2(values[0], values[1], values[2], values[3], values[4], values[5]) else null
    }
}

/** Pan is physical pixels; world is dp. Density is applied exactly once here. */
data class CanvasViewport(val zoom: Double = 1.0, val pan: Point2 = Point2(0.0, 0.0),
                          val density: Double = 1.0, val revision: Long = 0) {
    init { require(zoom.isFinite() && zoom > 0 && density.isFinite() && density > 0 && (zoom * density).isFinite() && zoom * density > 0) }
    fun toScreen(p: Point2) = Point2(p.x * zoom * density + pan.x, p.y * zoom * density + pan.y)
    fun toWorld(p: Point2) = Point2((p.x - pan.x) / (zoom * density), (p.y - pan.y) / (zoom * density))
    fun zoomAt(screen: Point2, nextZoom: Double): CanvasViewport {
        require(nextZoom.isFinite() && nextZoom > 0)
        val anchor = toWorld(screen)
        return copy(zoom = nextZoom, pan = Point2(screen.x - anchor.x * nextZoom * density,
            screen.y - anchor.y * nextZoom * density), revision = revision + 1)
    }
}

data class ClipRegion(val rect: Rect2, val transform: Affine2 = Affine2()) {
    fun contains(world: Point2): Boolean = transform.inverse()?.let { rect.hasArea && rect.contains(it.apply(world)) } == true
}
enum class FlowAxis { HORIZONTAL, VERTICAL, STACK }
data class ContainerBounds(val axis: FlowAxis, val content: Rect2, val slot: String = "children")
data class MeasuredNode(
    val id: String, val rect: Rect2, val transform: Affine2 = Affine2(), val paintOrder: Int = 0,
    val clips: List<ClipRegion> = emptyList(), val visible: Boolean = true, val inFlow: Boolean = true,
    val container: ContainerBounds? = null,
) {
    fun contains(world: Point2): Boolean = visible && rect.hasArea && clips.all { it.contains(world) } &&
        transform.inverse()?.let { rect.contains(it.apply(world)) } == true
}
data class LayoutSnapshot(val documentId: String, val documentRevision: Long, val revision: Long, val nodes: Map<String, MeasuredNode>)
data class DropPlan(
    val sourceId: String, val targetId: String, val slot: String, val targetIndex: Int,
    val previousId: String?, val nextId: String?, val guideStart: Point2, val guideEnd: Point2,
    val documentId: String, val documentRevision: Long, val layoutRevision: Long, val viewportRevision: Long,
)

object DropGeometry {
    fun hit(document: AuthoringDocument, layout: LayoutSnapshot, viewport: CanvasViewport, screen: Point2): String? {
        if (!current(document, layout)) return null
        val world = viewport.toWorld(screen)
        return ordered(document, layout).firstOrNull { it.contains(world) }?.id
    }

    fun plan(document: AuthoringDocument, layout: LayoutSnapshot, viewport: CanvasViewport,
             sourceId: String, screen: Point2, catalog: NodeCatalog): AuthoringResult<DropPlan> {
        fun fail(code: String, message: String, nodeId: String = sourceId) = AuthoringResult<DropPlan>(null,
            listOf(NodeDiagnostic(code, message, "drop", nodeId = nodeId, documentId = document.documentId)))
        if (!current(document, layout)) return fail("STALE_LAYOUT", "Layout não corresponde ao documento")
        val source = document.nodes[sourceId]
        if (source?.kind != AuthorNodeKind.VISUAL || source.placement !is Placement.Child)
            return fail("INVALID_DRAG_SOURCE", "Somente nós visuais contidos podem ser movidos")
        val indices = AuthoringIndices(document)
        val excluded = mutableSetOf(sourceId)
        val childrenByParent = document.nodes.values.mapNotNull { node ->
            (node.placement as? Placement.Child)?.let { it.parentId to node.id }
        }.groupBy({ it.first }, { it.second })
        fun exclude(id: String) { childrenByParent[id].orEmpty().forEach { if (excluded.add(it)) exclude(it) } }
        exclude(sourceId)
        val world = viewport.toWorld(screen)
        val target = ordered(document, layout).firstOrNull { measured ->
            val container = measured.container
            val node = document.nodes[measured.id]
            val axisSupported = when (container?.axis) {
                FlowAxis.HORIZONTAL -> node?.definition == "layout:row"
                FlowAxis.VERTICAL -> node?.definition == "layout:column"
                FlowAxis.STACK -> node?.definition == "layout:box"
                null -> false
            }
            val definition = node?.let { catalog[it.definition] }
            measured.id !in excluded && container != null && axisSupported && container.content.hasArea &&
                container.slot == "children" && definition?.childrenRole == NodeRole.COMPONENT && measured.contains(world)
        } ?: return AuthoringResult(null, emptyList())
        val container = target.container!!
        val inverse = target.transform.inverse() ?: return fail("SINGULAR_TRANSFORM", "Transformação de destino singular", target.id)
        val localPointer = inverse.apply(world)
        val all = indices.childrenByParentAndSlot[ParentSlot(target.id, container.slot)].orEmpty().filter { it.id != sourceId }
        val flow = all.mapNotNull { node -> layout.nodes[node.id]?.takeIf { it.visible && it.inFlow && it.rect.hasArea } }
        val bounds = flow.map { measured ->
            val points = measured.rect.corners().map { inverse.apply(measured.transform.apply(it)) }
            val start = if (container.axis == FlowAxis.HORIZONTAL) points.minOf { it.x } else points.minOf { it.y }
            val end = if (container.axis == FlowAxis.HORIZONTAL) points.maxOf { it.x } else points.maxOf { it.y }
            start to end
        }
        val centers = bounds.map { (a, b) -> a / 2 + b / 2 }
        if (container.axis != FlowAxis.STACK && centers.zipWithNext().any { (a, b) -> a > b })
            return fail("UNSUPPORTED_FLOW", "Centros não monotônicos: wrap/reverse não são suportados", target.id)
        val pointerAxis = if (container.axis == FlowAxis.HORIZONTAL) localPointer.x else localPointer.y
        val index = if (container.axis == FlowAxis.STACK) flow.size else centers.indexOfFirst { pointerAxis < it }.let { if (it < 0) flow.size else it }
        val nextVisible = flow.getOrNull(index)?.id
        val completeIndex = if (nextVisible == null) all.size else all.indexOfFirst { it.id == nextVisible }
        val guide = when {
            container.axis == FlowAxis.STACK -> container.content.bottom
            bounds.isEmpty() -> if (container.axis == FlowAxis.HORIZONTAL) container.content.x else container.content.y
            index == 0 -> bounds.first().first
            index == bounds.size -> bounds.last().second
            else -> bounds[index - 1].second / 2 + bounds[index].first / 2
        }
        val start = if (container.axis == FlowAxis.HORIZONTAL) Point2(guide, container.content.y) else Point2(container.content.x, guide)
        val end = if (container.axis == FlowAxis.HORIZONTAL) Point2(guide, container.content.bottom) else Point2(container.content.right, guide)
        return AuthoringResult(DropPlan(sourceId, target.id, container.slot, index,
            all.getOrNull(completeIndex - 1)?.id, all.getOrNull(completeIndex)?.id,
            viewport.toScreen(target.transform.apply(start)), viewport.toScreen(target.transform.apply(end)),
            document.documentId, document.revision, layout.revision, viewport.revision), emptyList())
    }

    private fun current(document: AuthoringDocument, layout: LayoutSnapshot) =
        document.documentId == layout.documentId && document.revision == layout.documentRevision &&
            layout.nodes.all { (id, node) -> id == node.id && id in document.nodes }
    private fun ordered(document: AuthoringDocument, layout: LayoutSnapshot): List<MeasuredNode> {
        val depths = mutableMapOf<String, Int>()
        fun depth(id: String): Int = depths.getOrPut(id) {
            (document.nodes[id]?.placement as? Placement.Child)?.let { depth(it.parentId) + 1 } ?: 0
        }
        return layout.nodes.values.sortedWith(compareByDescending<MeasuredNode> { it.paintOrder }
            .thenByDescending { depth(it.id) }.thenBy { it.id })
    }
}

/** One captured pointer, no document edits until a revision-checked release. */
class DropGesture {
    private data class Capture(val pointer: Long, val source: String, val documentId: String, val revision: Long)
    private var capture: Capture? = null
    var preview: DropPlan? = null
        private set
    val isActive get() = capture != null
    fun begin(pointer: Long, source: String, document: AuthoringDocument): Boolean {
        if (capture != null || document.nodes[source]?.kind != AuthorNodeKind.VISUAL || document.nodes[source]?.placement !is Placement.Child) return false
        capture = Capture(pointer, source, document.documentId, document.revision)
        preview = null
        return true
    }
    fun move(pointer: Long, document: AuthoringDocument, layout: LayoutSnapshot, viewport: CanvasViewport,
             screen: Point2, catalog: NodeCatalog): AuthoringResult<DropPlan> {
        val active = capture ?: return AuthoringResult(null, emptyList())
        if (active.pointer != pointer) return AuthoringResult(null, emptyList())
        if (active.documentId != document.documentId || active.revision != document.revision) {
            cancel(); return AuthoringResult(null, listOf(NodeDiagnostic("STALE_GESTURE", "Documento mudou durante o gesto", "drop")))
        }
        val result = DropGeometry.plan(document, layout, viewport, active.source, screen, catalog)
        preview = result.value
        return result
    }
    fun cancel() { capture = null; preview = null }
    fun finish(pointer: Long, session: EditSession, layoutRevision: Long, viewportRevision: Long, commandId: String): EditOutcome? {
        val active = capture ?: return null
        if (active.pointer != pointer) return null
        val plan = preview
        cancel()
        if (plan == null) return null
        val doc = session.snapshot()
        if (doc.documentId != plan.documentId || doc.revision != plan.documentRevision || layoutRevision != plan.layoutRevision || viewportRevision != plan.viewportRevision)
            return EditOutcome(null, listOf(NodeDiagnostic("STALE_DROP", "Documento, layout ou viewport mudou; inicie outro gesto", "drop", nodeId = active.source)))
        return session.execute(EditCommand(commandId, plan.documentRevision, listOf(EditOperation.MoveNode(active.source,
            NodeContainer.Slot(plan.targetId, plan.slot), plan.previousId, plan.nextId))))
    }
}
