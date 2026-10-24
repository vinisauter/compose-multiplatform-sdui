package me.developes.humming.sdui.studio

import kotlin.math.hypot
import kotlin.test.*

class ConnectorGeometryTest {
    private val origin = Rect2(0.0, 0.0, 100.0, 80.0)
    private fun connection(target: Rect2) = assertNotNull(ConnectorGeometry.between("a", origin, "b", target))

    @Test fun anchorsAndTangentsFaceTheDestinationOnEverySide() {
        val cases = listOf(
            Triple(Rect2(300.0, 0.0, 100.0, 80.0), Point2(100.0,40.0), Point2(300.0,40.0)),
            Triple(Rect2(-300.0, 0.0, 100.0, 80.0), Point2(0.0,40.0), Point2(-200.0,40.0)),
            Triple(Rect2(0.0, 300.0, 100.0, 80.0), Point2(50.0,80.0), Point2(50.0,300.0)),
            Triple(Rect2(0.0, -300.0, 100.0, 80.0), Point2(50.0,0.0), Point2(50.0,-220.0)))
        for ((target, start, end) in cases) {
            val c = connection(target)
            assertEquals(start, c.point(0.0))
            assertEquals(end, c.point(1.0))
            val t = c.tangent(1.0)
            assertTrue(t.x*(end.x-start.x)+t.y*(end.y-start.y) > 0)
        }
    }

    @Test fun coincidentCentersAndOverlapsHaveDeterministicAnchorsButSelfIsRejected() {
        val c = connection(origin)
        assertEquals(Point2(100.0,40.0), c.p0)
        assertEquals(Point2(0.0,40.0), c.p3)
        assertTrue(ConnectorGeometry.flatten(c, CanvasViewport()).toleranceMet)
        assertNull(ConnectorGeometry.between("a", origin, "a", origin))
        assertNotNull(connection(Rect2(70.0, 30.0, 10.0, 10.0)))
    }

    @Test fun minimumAndMaximumHandlesAndHorizontalTieAreStable() {
        val short = connection(Rect2(101.0,0.0,100.0,80.0))
        assertEquals(24.0, short.p1.x-short.p0.x)
        val long = connection(Rect2(1000.0,0.0,100.0,80.0))
        assertEquals(160.0, long.p1.x-long.p0.x)
        assertEquals(Point2(100.0,40.0), connection(Rect2(100.0,100.0,100.0,80.0)).p0)
    }

    @Test fun collinearOvershootIsNotFlattenedToTheInitialChord() {
        val c = CubicConnector(Point2(0.0,0.0),Point2(24.0,0.0),Point2(-23.0,0.0),Point2(1.0,0.0))
        assertEquals(7.016, c.point(0.2).x, 1e-12)
        val viewport = CanvasViewport(zoom=2.0)
        val flat = ConnectorGeometry.flatten(c, viewport)
        assertTrue(flat.points.size > 2)
        assertTrue(flat.toleranceMet)
        assertTrue(flat.hit(viewport.toScreen(c.point(0.2)), 1.0))
        assertFalse(flat.hit(Point2(0.0,9.0)))
    }

    @Test fun screenSpaceHitAndArrowIncludeZoomPanAndDensityExactlyOnce() {
        val c = connection(Rect2(300.0,0.0,100.0,80.0))
        for (zoom in listOf(0.25,1.0,4.0)) for (density in listOf(1.0,2.5)) {
            val v = CanvasViewport(zoom, Point2(17.0,-9.0), density)
            val flat = ConnectorGeometry.flatten(c,v)
            val middle = v.toScreen(c.point(0.5))
            assertTrue(flat.hit(Point2(middle.x,middle.y+8)))
            assertFalse(flat.hit(Point2(middle.x,middle.y+8.01)))
            assertTrue(flat.hit(v.toScreen(c.p3),0.0))
            val arrow = assertNotNull(ConnectorGeometry.arrow(c,v))
            assertEquals(v.toScreen(c.p3), arrow.tip)
            assertEquals(8.0,hypot(arrow.left.x-arrow.right.x,arrow.left.y-arrow.right.y),1e-10)
            assertEquals(12.0,arrow.tip.x-(arrow.left.x+arrow.right.x)/2,1e-10)
        }
    }

    @Test fun subdivisionBudgetReportsUnmetToleranceAndIsBounded() {
        val c = CubicConnector(Point2(0.0,0.0),Point2(0.0,100.0),Point2(100.0,100.0),Point2(100.0,0.0))
        val limited = ConnectorGeometry.flatten(c,CanvasViewport(),maxDepth=0)
        assertFalse(limited.toleranceMet)
        assertEquals(100.0,limited.maxErrorPixels)
        assertEquals(2,limited.points.size)
        val accurate = ConnectorGeometry.flatten(c,CanvasViewport())
        assertTrue(accurate.toleranceMet)
        for (i in 0..100) assertTrue(accurate.hit(c.point(i/100.0),1.0))
        assertFailsWith<IllegalArgumentException> { ConnectorGeometry.flatten(c,CanvasViewport(),maxDepth=17) }
    }

    @Test fun degenerateCurvesHavePointHitButNoArrow() {
        val p = Point2(3.0,4.0)
        val c = CubicConnector(p,p,p,p)
        val flat = ConnectorGeometry.flatten(c,CanvasViewport())
        assertTrue(flat.toleranceMet)
        assertTrue(flat.hit(p,0.0))
        assertFalse(flat.hit(Point2(3.0,5.0),0.5))
        assertNull(ConnectorGeometry.arrow(c,CanvasViewport()))
        assertFailsWith<IllegalArgumentException> { c.point(Double.NaN) }
        assertFailsWith<IllegalArgumentException> { flat.hit(p,-1.0) }
    }
}
