package dev.drawably.compose.core

import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.max

/** A point in the sketch's own coordinate space. */
public data class Pt(
    val x: Double,
    val y: Double,
)

/**
 * One continuous pen stroke: a jittered polyline that gets smoothed through the
 * midpoints between its points when it is turned into a path.
 */
public data class Subpath(
    val points: List<Pt>,
    val closed: Boolean,
)

/**
 * A whole sketched shape. Upstream concatenates SVG `d` strings; the same
 * concatenation here is a list of strokes, so nothing is lost and the geometry
 * stays inspectable.
 */
public data class SketchPath(
    val subpaths: List<Subpath> = emptyList(),
) {
    public operator fun plus(other: SketchPath): SketchPath = SketchPath(subpaths + other.subpaths)

    public val isEmpty: Boolean get() = subpaths.isEmpty()
}

/** The stroke engine. A direct port of upstream `src/rough.ts`. */
public object Rough {
    // region Sampling

    /** Walks a straight edge in [step]-pixel increments. */
    public fun sampleLine(
        x1: Double,
        y1: Double,
        x2: Double,
        y2: Double,
        step: Double = 8.0,
    ): List<Pt> {
        val n = max(2, ceil(jsHypot(x2 - x1, y2 - y1) / step).toInt())
        return (0..n).map { i ->
            val t = i.toDouble() / n
            Pt(x1 + (x2 - x1) * t, y1 + (y2 - y1) * t)
        }
    }

    public fun ellipsePoints(
        cx: Double,
        cy: Double,
        rx: Double,
        ry: Double,
        a0: Double,
        a1: Double,
        n: Int,
    ): List<Pt> =
        (0..n).map { i ->
            val a = a0 + (a1 - a0) * i.toDouble() / n
            Pt(cx + rx * jsCos(a), cy + ry * jsSin(a))
        }

    internal fun arcPoints(
        cx: Double,
        cy: Double,
        r: Double,
        a0: Double,
        a1: Double,
        n: Int = 4,
    ): List<Pt> = ellipsePoints(cx, cy, r, r, a0, a1, n)

    internal fun roundedRectPoints(
        x: Double,
        y: Double,
        w: Double,
        h: Double,
        radius: Double,
    ): List<Pt> {
        val r = minOf(radius, w / 2, h / 2)
        val pi = PI
        return sampleLine(x + r, y, x + w - r, y) +
            arcPoints(x + w - r, y + r, r, -pi / 2, 0.0) +
            sampleLine(x + w, y + r, x + w, y + h - r) +
            arcPoints(x + w - r, y + h - r, r, 0.0, pi / 2) +
            sampleLine(x + w - r, y + h, x + r, y + h) +
            arcPoints(x + r, y + h - r, r, pi / 2, pi) +
            sampleLine(x, y + h - r, x, y + r) +
            arcPoints(x + r, y + r, r, pi, pi * 1.5)
    }

    // endregion

    // region Jitter

    /**
     * Nudges every point by up to [amp] in each axis. The x draw always precedes
     * the y draw; reordering them would desynchronise the whole PRNG stream and
     * change every sketch downstream.
     */
    public fun jitter(
        points: List<Pt>,
        rand: Mulberry32,
        amp: Double,
    ): List<Pt> =
        points.map { p ->
            val dx = (rand.next() * 2 - 1) * amp
            val dy = (rand.next() * 2 - 1) * amp
            Pt(p.x + dx, p.y + dy)
        }

    internal fun boilPass(
        points: List<Pt>,
        o: RoughOptions,
    ): List<Pt> {
        val boilSeed = o.boilSeed
        if (o.boil == 0.0 || boilSeed == null) return points
        return jitter(points, Mulberry32(boilSeed), o.boil)
    }

    /**
     * Draws the shape twice from one PRNG stream, the second pass 1.4× wider than
     * the first; the overlap is what reads as a pen going over a line.
     */
    internal fun doubleStroke(
        points: List<Pt>,
        o: RoughOptions,
        close: Boolean,
    ): SketchPath {
        val rand = Mulberry32(o.seed)
        val amp = 1.5 * o.roughness
        val first = boilPass(jitter(points, rand, amp), o)
        val second = boilPass(jitter(points, rand, amp * 1.4), o)
        return SketchPath(listOf(Subpath(first, close), Subpath(second, close)))
    }

    // endregion

    /**
     * The [count] pre-computed frames the boil animation cycles through, each
     * re-jittered off a prime-spaced offset of the base seed.
     */
    public fun variants(
        generate: (RoughOptions) -> SketchPath,
        o: RoughOptions,
        count: Int = 3,
    ): List<SketchPath> =
        (0 until count).map { i ->
            generate(o.copy(boilSeed = o.seed + ((i + 1) * 7919).toUInt()))
        }
}
