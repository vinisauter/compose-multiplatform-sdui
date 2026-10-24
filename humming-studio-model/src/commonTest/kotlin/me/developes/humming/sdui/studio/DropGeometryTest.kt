package me.developes.humming.sdui.studio

import me.developes.humming.sdui.common.initialNodeCatalog
import kotlin.test.*

class DropGeometryTest {
    private val catalog = initialNodeCatalog()
    private val codec = LegacyAuthoringCodec(catalog)
    private val viewport = CanvasViewport()
    private fun document() = codec.importDocument("""{"component":"layout:column","id":"root","children":[{"component":"layout:text","id":"a","properties":{"text":"A"}},{"component":"layout:text","id":"hidden","properties":{"text":"H"}},{"component":"layout:text","id":"b","properties":{"text":"B"}},{"component":"layout:text","id":"drag","properties":{"text":"X"}}]}""", "doc").requireValue()
    private fun layout(doc: AuthoringDocument, axis: FlowAxis = FlowAxis.VERTICAL): LayoutSnapshot {
        val nodes = linkedMapOf("root" to MeasuredNode("root", Rect2(0.0, 0.0, 200.0, 200.0),
            container = ContainerBounds(axis, Rect2(10.0, 10.0, 180.0, 180.0))))
        listOf("a", "hidden", "b", "drag").forEachIndexed { i, id ->
            nodes[id] = MeasuredNode(id, Rect2(10.0, 20.0 + 30 * i, 80.0, 20.0), paintOrder = i + 1, visible = id != "hidden")
        }
        return LayoutSnapshot(doc.documentId, doc.revision, 7, nodes)
    }
    private fun plan(doc: AuthoringDocument = document(), l: LayoutSnapshot = layout(doc), y: Double = 70.0) =
        DropGeometry.plan(doc, l, viewport, "drag", Point2(15.0, y), catalog).requireValue()

    @Test fun columnMidpointsEqualityGapsAndPaddingAreDeterministic() {
        val doc = document(); val l = layout(doc)
        val before = plan(doc, l, 10.0)
        assertEquals(0, before.targetIndex); assertNull(before.previousId); assertEquals("a", before.nextId)
        assertEquals(Point2(10.0, 20.0), before.guideStart)
        val equal = plan(doc, l, 30.0)
        assertEquals(1, equal.targetIndex); assertEquals("hidden", equal.previousId); assertEquals("b", equal.nextId)
        assertEquals(Point2(10.0, 60.0), equal.guideStart)
        assertEquals(Point2(190.0, 60.0), equal.guideEnd)
        val end = plan(doc, l, 199.0)
        assertEquals(2, end.targetIndex); assertEquals("b", end.previousId); assertNull(end.nextId)
        assertEquals(100.0, end.guideStart.y)
    }

    @Test fun emptyContainerUsesContentStartAndNeverIndexesTheDraggedChild() {
        val doc = document(); val l = layout(doc)
        val empty = l.copy(nodes = l.nodes.mapValues { (id, n) -> if (id == "root") n else n.copy(visible = false) })
        val result = plan(doc, empty)
        assertEquals(0, result.targetIndex)
        assertEquals(Point2(10.0, 10.0), result.guideStart)
        assertEquals("b", result.previousId) // append after full-slot children, even when all are filtered
        assertNull(result.nextId)
        assertNotEquals("drag", result.previousId)
    }

    @Test fun filteredChildrenResolveTheFullSlotGapWithoutKeyCollisions() {
        val doc = document(); val l = layout(doc)
        val session = EditSession(doc, catalog)
        val gesture = DropGesture()
        assertTrue(gesture.begin(1, "drag", doc))
        assertTrue(gesture.move(1, doc, l, viewport, Point2(15.0, 60.0), catalog).isSuccess)
        assertEquals(doc, session.snapshot())
        assertTrue(gesture.finish(1, session, l.revision, viewport.revision, "move")!!.isSuccess)
        val children = AuthoringIndices(session.snapshot()).childrenByParentAndSlot.getValue(ParentSlot("root", "children"))
        assertEquals(listOf("a", "hidden", "drag", "b"), children.map { it.id })
        assertEquals(4, children.map { it.placement.orderKey }.toSet().size)
        assertTrue(session.undo("undo", session.snapshot().revision).isSuccess)
        assertEquals(doc.nodes, session.snapshot().nodes)
    }

    @Test fun rowUsesXAndTransformsTheGuide() {
        val original = document()
        val doc = original.copy(nodes = original.nodes + ("root" to original.nodes.getValue("root").copy(definition = "layout:row")))
        val l = layout(doc, FlowAxis.HORIZONTAL)
        val transformed = l.copy(nodes = l.nodes.mapValues { (id, n) ->
            if (id == "root") n.copy(transform = Affine2(tx = 40.0, ty = 50.0))
            else n.copy(rect = Rect2(n.rect.y, 10.0, 20.0, 80.0), transform = Affine2(tx = 40.0, ty = 50.0))
        })
        val view = CanvasViewport(2.0, Point2(5.0, 9.0), density = 1.5, revision = 3)
        val result = DropGeometry.plan(doc, transformed, view, "drag", view.toScreen(Point2(100.0, 65.0)), catalog).requireValue()
        assertEquals(1, result.targetIndex)
        assertEquals(view.toScreen(Point2(100.0, 60.0)), result.guideStart)
        assertEquals(view.toScreen(Point2(100.0, 240.0)), result.guideEnd)
    }

    @Test fun anchoredZoomAndAffineInversePreserveTheWorldPoint() {
        val view = CanvasViewport(1.25, Point2(12.0, -8.0), 2.0)
        val point = Point2(20.0, 35.0)
        assertEquals(point, view.toWorld(view.toScreen(point)))
        val screen = view.toScreen(point)
        assertEquals(point, view.zoomAt(screen, 3.0).toWorld(screen))
        val transform = Affine2(0.0, 2.0, -3.0, 0.0, 7.0, 11.0)
        assertEquals(point, transform.inverse()!!.apply(transform.apply(point)))
        assertNull(Affine2(a = 0.0, d = 0.0).inverse())
        assertFailsWith<IllegalArgumentException> { CanvasViewport(zoom = 0.0) }
        assertFailsWith<IllegalArgumentException> { Rect2(0.0, 0.0, -1.0, 4.0) }
    }

    @Test fun hitHonorsPaintOrderDepthClipsAndDegenerateBounds() {
        val doc = document(); val l = layout(doc)
        val overlapping = l.copy(nodes = l.nodes + ("b" to l.nodes.getValue("b").copy(rect = Rect2(10.0, 20.0, 80.0, 20.0))))
        assertEquals("b", DropGeometry.hit(doc, overlapping, viewport, Point2(20.0, 30.0)))
        val clipped = overlapping.copy(nodes = overlapping.nodes + ("b" to overlapping.nodes.getValue("b").copy(
            clips = listOf(ClipRegion(Rect2(0.0, 0.0, 5.0, 5.0))))))
        assertEquals("a", DropGeometry.hit(doc, clipped, viewport, Point2(20.0, 30.0)))
        assertNull(DropGeometry.hit(doc, l, viewport, Point2(300.0, 300.0)))
        val flat = l.copy(nodes = l.nodes.mapValues { (_, n) -> n.copy(paintOrder = 0) })
        assertEquals("a", DropGeometry.hit(doc, flat, viewport, Point2(20.0, 30.0)))
        assertFalse(MeasuredNode("z", Rect2(0.0, 0.0, 0.0, 8.0)).contains(Point2(0.0, 4.0)))
    }

    @Test fun nestedTargetsPreferTheFrontContainerAndExcludeSourceSubtree() {
        val doc = codec.importDocument("""{"component":"layout:column","id":"root","children":[{"component":"layout:column","id":"inner","children":[{"component":"layout:column","id":"desc"}]},{"component":"layout:text","id":"drag","properties":{"text":"X"}}]}""", "nested").requireValue()
        fun n(id: String, order: Int) = MeasuredNode(id, Rect2(0.0, 0.0, 100.0, 100.0), paintOrder = order,
            container = ContainerBounds(FlowAxis.VERTICAL, Rect2(0.0, 0.0, 100.0, 100.0)))
        val l = LayoutSnapshot(doc.documentId, doc.revision, 1, mapOf("root" to n("root", 0), "inner" to n("inner", 1), "desc" to n("desc", 2)))
        assertEquals("desc", DropGeometry.plan(doc, l, viewport, "drag", Point2(5.0, 5.0), catalog).requireValue().targetId)
        assertEquals("root", DropGeometry.plan(doc, l, viewport, "inner", Point2(5.0, 5.0), catalog).requireValue().targetId)
        assertTrue(DropGeometry.plan(doc, l, viewport, "root", Point2(5.0, 5.0), catalog).diagnostics.any { it.code == "INVALID_DRAG_SOURCE" })
    }

    @Test fun nonMonotonicFlowAndStaleOrSingularLayoutsCannotProduceADrop() {
        val doc = document(); val l = layout(doc)
        val reversed = l.copy(nodes = l.nodes + ("a" to l.nodes.getValue("a").copy(rect = Rect2(10.0, 150.0, 80.0, 20.0))))
        assertEquals("UNSUPPORTED_FLOW", DropGeometry.plan(doc, reversed, viewport, "drag", Point2(15.0, 60.0), catalog).diagnostics.single().code)
        assertEquals("STALE_LAYOUT", DropGeometry.plan(doc, l.copy(documentRevision = 9), viewport, "drag", Point2(15.0, 60.0), catalog).diagnostics.single().code)
        val singular = l.copy(nodes = l.nodes + ("root" to l.nodes.getValue("root").copy(transform = Affine2(a = 0.0, d = 0.0))))
        assertNull(DropGeometry.plan(doc, singular, viewport, "drag", Point2(15.0, 60.0), catalog).value)
    }

    @Test fun anUnrelatedFrontOverlayDoesNotDropIntoAContainerBehindIt() {
        val doc = codec.importDocument("""{"component":"layout:box","id":"root","children":[{"component":"layout:column","id":"back"},{"component":"layout:text","id":"front","properties":{"text":"Overlay"}},{"component":"layout:text","id":"drag","properties":{"text":"X"}}]}""", "overlay").requireValue()
        val rect = Rect2(0.0, 0.0, 100.0, 100.0)
        val l = LayoutSnapshot(doc.documentId, doc.revision, 1, mapOf(
            "root" to MeasuredNode("root", rect, container = ContainerBounds(FlowAxis.STACK, rect)),
            "back" to MeasuredNode("back", rect, paintOrder = 1, container = ContainerBounds(FlowAxis.VERTICAL, rect)),
            "front" to MeasuredNode("front", rect, paintOrder = 2)))
        assertEquals("root", DropGeometry.plan(doc, l, viewport, "drag", Point2(10.0, 10.0), catalog).requireValue().targetId)
    }

    @Test fun boxAppendsAndUsesItsOwnBottomGuide() {
        val original = document(); val doc = original.copy(nodes = original.nodes + ("root" to original.nodes.getValue("root").copy(definition = "layout:box")))
        val result = plan(doc, layout(doc, FlowAxis.STACK))
        assertNull(result.nextId); assertEquals("b", result.previousId)
        assertEquals(190.0, result.guideStart.y)
    }

    @Test fun captureCancelForeignPointerAndRepeatedReleaseNeverCommit() {
        val doc = document(); val l = layout(doc); val session = EditSession(doc, catalog); val gesture = DropGesture()
        assertTrue(gesture.begin(1, "drag", doc)); assertFalse(gesture.begin(2, "a", doc))
        assertNull(gesture.move(2, doc, l, viewport, Point2(15.0, 60.0), catalog).value)
        assertNull(gesture.finish(2, session, 7, 0, "foreign")); assertTrue(gesture.isActive)
        gesture.move(1, doc, l, viewport, Point2(15.0, 60.0), catalog)
        gesture.cancel()
        assertNull(gesture.preview); assertFalse(gesture.isActive)
        assertNull(gesture.finish(1, session, 7, 0, "cancelled"))
        assertEquals(doc, session.snapshot())
    }

    @Test fun everyRevisionIsCheckedAtReleaseAndDocumentChangesCancelMovement() {
        listOf("layout", "viewport", "document").forEach { changed ->
            val doc = document(); val l = layout(doc); val session = EditSession(doc, catalog); val gesture = DropGesture()
            gesture.begin(1, "drag", doc); gesture.move(1, doc, l, viewport, Point2(15.0, 60.0), catalog)
            if (changed == "document") session.execute(EditCommand("other", 0, listOf(EditOperation.PutProperty("a", "text", kotlinx.serialization.json.JsonPrimitive("changed")))))
            val before = session.snapshot()
            assertEquals("STALE_DROP", gesture.finish(1, session, if (changed == "layout") 8 else 7,
                if (changed == "viewport") 1 else 0, "drop")!!.diagnostics.single().code)
            assertEquals(before, session.snapshot()); assertFalse(gesture.isActive)
            assertNull(gesture.finish(1, session, 7, 0, "again"))
        }
        val doc = document(); val gesture = DropGesture(); gesture.begin(1, "drag", doc)
        assertEquals("STALE_GESTURE", gesture.move(1, doc.copy(revision = 1), layout(doc), viewport, Point2(15.0, 60.0), catalog).diagnostics.single().code)
        assertFalse(gesture.isActive)
    }
}
