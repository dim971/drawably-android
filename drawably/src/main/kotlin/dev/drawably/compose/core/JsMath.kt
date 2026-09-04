package dev.drawably.compose.core

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

/**
 * The handful of maths functions where the JVM's default answer differs from
 * JavaScript's, wrapped so the engine can stay a literal transcription.
 *
 * A one-ulp difference is invisible in a coordinate rounded to two decimals,
 * but sample counts come from `ceil(length / step)`, and an arrow head is
 * exactly 12 long sampled every 4. Landing on the wrong side of that boundary
 * adds a point, which shifts every subsequent PRNG draw and changes the rest of
 * the shape — so these have to agree exactly, not approximately.
 */

/**
 * V8 implements `Math.cos`, `Math.sin` and `Math.atan2` with fdlibm, and so
 * does [StrictMath]. `java.lang.Math` is allowed to use faster intrinsics that
 * differ in the last ulp.
 */
internal fun jsCos(x: Double): Double = StrictMath.cos(x)

internal fun jsSin(x: Double): Double = StrictMath.sin(x)

internal fun jsAtan2(y: Double, x: Double): Double = StrictMath.atan2(y, x)

/**
 * `Math.hypot`, as V8 computes it: scale by the larger component, then `sqrt`
 * before multiplying back. `java.lang.Math.hypot` is more accurate, which here
 * means "different".
 */
internal fun jsHypot(x: Double, y: Double): Double {
    val ax = abs(x)
    val ay = abs(y)
    val largest = max(ax, ay)
    if (largest == 0.0) return 0.0
    if (largest.isInfinite()) return Double.POSITIVE_INFINITY
    val nx = ax / largest
    val ny = ay / largest
    return sqrt(nx * nx + ny * ny) * largest
}
