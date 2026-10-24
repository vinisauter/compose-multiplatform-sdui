package me.developes.humming.sdui.studio

import kotlin.math.abs
import kotlin.math.hypot

/** Derived world-space geometry. Never persisted in AuthoringDocument. */
data class CubicConnector(val p0: Point2, val p1: Point2, val p2: Point2, val p3: Point2) {
    fun point(t: Double): Point2 {
        require(t.isFinite() && t in 0.0..1.0)
        val u = 1 - t
        return combine(u*u*u, 3*u*u*t, 3*u*t*t, t*t*t)
    }

    fun tangent(t: Double): Point2 {
        require(t.isFinite() && t in 0.0..1.0)
        val u = 1 - t
        return Point2(3*u*u*(p1.x-p0.x)+6*u*t*(p2.x-p1.x)+3*t*t*(p3.x-p2.x),
            3*u*u*(p1.y-p0.y)+6*u*t*(p2.y-p1.y)+3*t*t*(p3.y-p2.y))
    }

    private fun combine(a: Double, b: Double, c: Double, d: Double) =
        Point2(a*p0.x+b*p1.x+c*p2.x+d*p3.x, a*p0.y+b*p1.y+c*p2.y+d*p3.y)
}

data class ConnectorArrow(val tip: Point2, val left: Point2, val right: Point2)

/** Points and error are physical screen pixels, including density exactly once. */
data class FlattenedConnector(val points: List<Point2>, val maxErrorPixels: Double, val toleranceMet: Boolean) {
    fun hit(pointer: Point2, radiusPixels: Double = 8.0): Boolean {
        require(radiusPixels.isFinite() && radiusPixels >= 0)
        return points.zipWithNext().any { (a, b) -> segmentDistance(pointer, a, b) <= radiusPixels }
    }
}

object ConnectorGeometry {
    /** A visual self-loop needs separate routing; navigation cycles remain valid in the compiler. */
    fun between(sourceId: String, source: Rect2, targetId: String, target: Rect2): CubicConnector? {
        if (sourceId == targetId) return null
        val dx = target.x + target.width/2 - source.x - source.width/2
        val dy = target.y + target.height/2 - source.y - source.height/2
        val horizontal = abs(dx) >= abs(dy)
        val sign = if ((if (horizontal) dx else dy) < 0) -1.0 else 1.0
        val normal = if (horizontal) Point2(sign, 0.0) else Point2(0.0, sign)
        fun anchor(rect: Rect2, direction: Double) = if (horizontal)
            Point2(if (direction > 0) rect.right else rect.x, rect.y + rect.height/2)
        else Point2(rect.x + rect.width/2, if (direction > 0) rect.bottom else rect.y)
        val start = anchor(source, sign)
        val end = anchor(target, -sign)
        val handle = (hypot(end.x-start.x, end.y-start.y)/2).coerceIn(24.0, 160.0)
        return CubicConnector(start, Point2(start.x+handle*normal.x, start.y+handle*normal.y),
            Point2(end.x-handle*normal.x, end.y-handle*normal.y), end)
    }

    /** Arrow dimensions stay constant in physical pixels at every zoom and density. */
    fun arrow(curve: CubicConnector, viewport: CanvasViewport, lengthPixels: Double = 12.0,
              widthPixels: Double = 8.0): ConnectorArrow? {
        require(lengthPixels.isFinite() && lengthPixels > 0 && widthPixels.isFinite() && widthPixels > 0)
        val tangent = curve.tangent(1.0)
        val magnitude = hypot(tangent.x, tangent.y)
        if (magnitude == 0.0) return null
        val vx = tangent.x/magnitude
        val vy = tangent.y/magnitude
        val tip = viewport.toScreen(curve.p3)
        val bx = tip.x-lengthPixels*vx
        val by = tip.y-lengthPixels*vy
        return ConnectorArrow(tip, Point2(bx-widthPixels*vy/2, by+widthPixels*vx/2),
            Point2(bx+widthPixels*vy/2, by-widthPixels*vx/2))
    }

    /** Finite-chord flatness catches collinear overshoot. At most 2^maxDepth segments. */
    fun flatten(curve: CubicConnector, viewport: CanvasViewport, tolerancePixels: Double = 1.0,
                maxDepth: Int = 12): FlattenedConnector {
        require(tolerancePixels.isFinite() && tolerancePixels > 0)
        require(maxDepth in 0..16)
        val screen = CubicConnector(viewport.toScreen(curve.p0), viewport.toScreen(curve.p1),
            viewport.toScreen(curve.p2), viewport.toScreen(curve.p3))
        val points = mutableListOf(screen.p0)
        var maxError = 0.0
        fun split(c: CubicConnector, depth: Int) {
            val error = maxOf(segmentDistance(c.p1, c.p0, c.p3), segmentDistance(c.p2, c.p0, c.p3))
            if (error <= tolerancePixels || depth == maxDepth) {
                maxError = maxOf(maxError, error)
                points.add(c.p3)
                return
            }
            val a = midpoint(c.p0, c.p1)
            val b = midpoint(c.p1, c.p2)
            val d = midpoint(c.p2, c.p3)
            val e = midpoint(a, b)
            val f = midpoint(b, d)
            val m = midpoint(e, f)
            split(CubicConnector(c.p0, a, e, m), depth+1)
            split(CubicConnector(m, f, d, c.p3), depth+1)
        }
        split(screen, 0)
        return FlattenedConnector(points.toList(), maxError, maxError <= tolerancePixels)
    }
}

private fun midpoint(a: Point2, b: Point2) = Point2(a.x/2+b.x/2, a.y/2+b.y/2)

private fun segmentDistance(q: Point2, a: Point2, b: Point2): Double {
    val dx = b.x-a.x
    val dy = b.y-a.y
    val length = hypot(dx, dy)
    if (length == 0.0) return hypot(q.x-a.x, q.y-a.y)
    val ux = dx/length
    val uy = dy/length
    val projection = ((q.x-a.x)*ux+(q.y-a.y)*uy).coerceIn(0.0, length)
    return hypot(q.x-a.x-projection*ux, q.y-a.y-projection*uy)
}
