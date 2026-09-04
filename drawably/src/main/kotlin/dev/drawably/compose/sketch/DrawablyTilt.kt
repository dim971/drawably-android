package dev.drawably.compose.sketch

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import dev.drawably.compose.core.Mulberry32
import dev.drawably.compose.core.randomSeed

/** How far a tilt leans, and where the angle comes from. */
public object DrawablyTilt {
    /**
     * A couple of degrees: enough to read as placed by hand, not enough to look
     * broken.
     */
    public const val DEFAULT_MAX_DEGREES: Float = 2f

    /**
     * The angle a seed produces, drawn from the same PRNG the sketches use so a
     * pinned seed gives a pinned lean.
     */
    public fun angle(
        seed: UInt,
        maxDegrees: Float = DEFAULT_MAX_DEGREES,
    ): Float {
        val rand = Mulberry32(seed)
        return ((rand.next() * 2 - 1) * maxDegrees).toFloat()
    }
}

/**
 * Leans this composable by a small random angle, so a group of controls looks
 * laid out by hand rather than by a layout engine.
 *
 * ```kotlin
 * DrawablyButton("Done", onClick = {}, modifier = Modifier.drawablyTilt())
 * ```
 *
 * The lean is picked once and held: it is a placement, not part of the sketch,
 * so it does not change when the control re-sketches under a finger. Pass a
 * [seed] to pin it.
 */
@Composable
public fun Modifier.drawablyTilt(
    seed: UInt? = null,
    maxDegrees: Float = DrawablyTilt.DEFAULT_MAX_DEGREES,
): Modifier {
    val resolved = remember(seed) { seed ?: randomSeed() }
    return drawablyTilt(DrawablyTilt.angle(resolved, maxDegrees))
}

/** Leans this composable by an exact angle. */
public fun Modifier.drawablyTilt(degrees: Float): Modifier = graphicsLayer { rotationZ = degrees }
