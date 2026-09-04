package dev.drawably.compose.sketch

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import dev.drawably.compose.theme.DrawablyTheme
import dev.drawably.compose.theme.LocalDrawablyTheme

/** Everything a control needs to draw itself, gathered in one place. */
@Immutable
public class DrawablySketchState internal constructor(
    public val seed: DrawablySeed,
    public val frame: State<Int>,
    public val theme: DrawablyTheme,
    public val reduceMotion: Boolean,
)

/**
 * The seed, the boil ticker and the theme for one control.
 *
 * The ticker stops when the device asks for reduced motion or the theme turns
 * boiling off, in which case only the first frame is ever drawn.
 */
@Composable
public fun rememberDrawablySketchState(
    seed: UInt? = null,
    boilPeriodMillis: Long = DRAWABLY_BOIL_PERIOD_MS,
): DrawablySketchState {
    val theme = LocalDrawablyTheme.current
    val reduceMotion = rememberDrawablyReduceMotion()
    return DrawablySketchState(
        seed = rememberDrawablySeed(seed),
        frame = rememberDrawablyBoilFrame(
            periodMillis = boilPeriodMillis,
            enabled = !reduceMotion && theme.boil != 0.0,
        ),
        theme = theme,
        reduceMotion = reduceMotion,
    )
}

/** Draws a sketch behind the content using an already-gathered [state]. */
public fun androidx.compose.ui.Modifier.drawablySketch(
    state: DrawablySketchState,
    layers: List<SketchLayer>,
    ink: androidx.compose.ui.graphics.Color? = null,
    lineWidth: Double? = null,
): androidx.compose.ui.Modifier = drawablySketch(
    layers = layers,
    seed = state.seed.value,
    theme = state.theme,
    frame = state.frame,
    ink = ink,
    lineWidth = lineWidth,
)
