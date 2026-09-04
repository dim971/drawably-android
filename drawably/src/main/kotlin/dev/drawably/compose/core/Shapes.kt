package dev.drawably.compose.core

import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

public fun Rough.line(
    x1: Double,
    y1: Double,
    x2: Double,
    y2: Double,
    o: RoughOptions,
): SketchPath = doubleStroke(sampleLine(x1, y1, x2, y2), o, false)

public fun Rough.circle(cx: Double, cy: Double, r: Double, o: RoughOptions): SketchPath =
    ellipse(cx, cy, r, r, o)

/**
 * Ramanujan's perimeter approximation, sampled every 8 pixels like the lines
 * are, so a big ellipse doesn't come out smoother than a short edge.
 */
public fun Rough.ellipse(
    cx: Double,
    cy: Double,
    rx: Double,
    ry: Double,
    o: RoughOptions,
): SketchPath {
    val flattening = (rx - ry) / (rx + ry)
    val h = flattening * flattening
    val perimeter = PI * (rx + ry) * (1 + (3 * h) / (10 + sqrt(4 - 3 * h)))
    val n = max(8, ceil(perimeter / 8).toInt())
    val points = ellipsePoints(cx, cy, rx, ry, 0.0, PI * 2, n).dropLast(1)
    return doubleStroke(points, o, true)
}

public fun Rough.roundedRect(
    x: Double,
    y: Double,
    w: Double,
    h: Double,
    r: Double,
    o: RoughOptions,
): SketchPath = doubleStroke(roundedRectPoints(x, y, w, h, r), o, true)

public const val ARROW_HEAD: Double = 12.0
public val ARROW_HEAD_ANGLE: Double = PI / 6

public fun Rough.arrow(
    x1: Double,
    y1: Double,
    x2: Double,
    y2: Double,
    o: RoughOptions,
): SketchPath {
    val a = jsAtan2(y2 - y1, x2 - x1)
    fun wing(da: Double) = Pt(x2 - ARROW_HEAD * jsCos(a + da), y2 - ARROW_HEAD * jsSin(a + da))

    // The shaft runs its own stream off the same seed; both wings then share a
    // second stream, in that order.
    val shaft = line(x1, y1, x2, y2, o)
    val rand = Mulberry32(o.seed)
    val amp = 1.2 * o.roughness
    fun head(p: Pt) = Subpath(boilPass(jitter(sampleLine(x2, y2, p.x, p.y, 4.0), rand, amp), o), false)

    return shaft + SketchPath(listOf(head(wing(ARROW_HEAD_ANGLE)), head(wing(-ARROW_HEAD_ANGLE))))
}

public fun Rough.checkmark(
    x: Double,
    y: Double,
    w: Double,
    h: Double,
    o: RoughOptions,
): SketchPath {
    val rand = Mulberry32(o.seed)
    // The shared vertex is deliberately duplicated: midpoint smoothing would
    // otherwise round the corner off.
    val pts = sampleLine(x, y + h * 0.6, x + w * 0.35, y + h, 4.0) +
        sampleLine(x + w * 0.35, y + h, x + w, y, 4.0)
    return SketchPath(listOf(Subpath(boilPass(jitter(pts, rand, 1.2 * o.roughness), o), false)))
}

/** A single back-and-forth 45° hatch, drawn as one continuous stroke. */
public fun Rough.scribbleFill(
    x: Double,
    y: Double,
    w: Double,
    h: Double,
    o: RoughOptions,
): SketchPath {
    val rand = Mulberry32(o.seed)
    val gap = 6.0
    val pts = mutableListOf<Pt>()
    var flip = false
    var t = gap
    while (t < w + h) {
        val a = Pt(x + max(0.0, t - h), y + min(t, h))
        val b = Pt(x + min(t, w), y + max(0.0, t - w))
        if (flip) { pts += b; pts += a } else { pts += a; pts += b }
        flip = !flip
        t += gap
    }
    if (pts.size < 2) return SketchPath()
    return SketchPath(listOf(Subpath(boilPass(jitter(pts, rand, 1.2 * o.roughness), o), false)))
}
