package dev.drawably.compose.core

import kotlin.math.abs
import kotlin.math.floor

/**
 * `Number.prototype.toFixed(2)`, reproduced.
 *
 * `String.format("%.2f", …)` rounds halves to even, JavaScript rounds them away
 * from zero, so a value like `0.125` formats as `0.12` in one and `0.13` in the
 * other. Those exact halves are common in this library's geometry, and the
 * golden fixtures are upstream's strings, hence the explicit tie branch.
 */
internal fun jsToFixed2(value: Double): String {
    if (value.isNaN()) return "NaN"
    val negative = value < 0
    val magnitude = abs(value)
    if (magnitude.isInfinite()) return if (negative) "-Infinity" else "Infinity"

    val halves = magnitude * 200
    val n =
        if (halves == floor(halves) && halves.mod(2.0) == 1.0) {
            // exactly .xx5: ECMAScript takes the larger candidate
            (halves + 1) / 2
        } else {
            val scaled = magnitude * 100
            val down = floor(scaled)
            if (scaled - down >= 0.5) down + 1 else down
        }

    val digits = n.toLong().toString().padStart(3, '0')
    val text = digits.dropLast(2) + "." + digits.takeLast(2)
    return if (negative) "-$text" else text
}

/**
 * The SVG `d` string upstream would emit for this stroke: a move, then a
 * quadratic through each interior point to the midpoint of the next segment,
 * then a line to the last point.
 *
 * Only the tests need this (rendering builds a `Path` from the same traversal
 * instead), but it is what makes "faithful port" checkable.
 */
public fun Subpath.toSvgString(): String {
    val first = points.firstOrNull() ?: return ""
    val d = StringBuilder("M${jsToFixed2(first.x)} ${jsToFixed2(first.y)}")
    for (i in 1 until points.size - 1) {
        val c = points[i]
        val mx = (c.x + points[i + 1].x) / 2
        val my = (c.y + points[i + 1].y) / 2
        d.append("Q${jsToFixed2(c.x)} ${jsToFixed2(c.y)} ${jsToFixed2(mx)} ${jsToFixed2(my)}")
    }
    val last = points.last()
    d.append("L${jsToFixed2(last.x)} ${jsToFixed2(last.y)}")
    if (closed) d.append("Z")
    return d.toString()
}

public fun SketchPath.toSvgString(): String = subpaths.joinToString("") { it.toSvgString() }
