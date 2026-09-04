package dev.drawably.compose.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dev.drawably.compose.core.RoughOptions
import dev.drawably.compose.core.SketchPath
import dev.drawably.compose.sketch.DrawablyGeometry
import dev.drawably.compose.sketch.SketchLayer
import dev.drawably.compose.sketch.SketchRole
import dev.drawably.compose.sketch.drawablySketch
import dev.drawably.compose.sketch.rememberDrawablySketchState

/** The three marks you can make over a piece of text. */
public enum class DrawablyDecoration {
    /** A pen line just under the text. */
    Underline,

    /** A translucent marker swipe behind it. */
    Highlight,

    /** A loop around it, overshooting the way a hand does. */
    Circle,
    ;

    internal val role: SketchRole
        get() = if (this == Highlight) SketchRole.Wash else SketchRole.Outline

    internal fun shape(
        width: Double,
        height: Double,
        o: RoughOptions,
    ): SketchPath =
        when (this) {
            Underline -> DrawablyGeometry.underline(width, height, o)
            Highlight -> DrawablyGeometry.highlightWash(width, height, o)
            Circle -> DrawablyGeometry.circleOutline(width, height, o)
        }
}

/**
 * Text decorations sit on body copy, where the 2dp control stroke reads heavy.
 */
public const val DRAWABLY_DECORATION_WIDTH: Double = 1.5

/**
 * Marks whatever it is applied to as a single box.
 *
 * [DrawablyDecoratedText] marks every line a run of text wraps onto, the way
 * upstream reads `getClientRects()`; this is the one-box version for anything
 * that is not text.
 */
@Composable
public fun Modifier.drawablyDecoration(
    decoration: DrawablyDecoration,
    seed: UInt? = null,
): Modifier {
    val state = rememberDrawablySketchState(seed)
    val layers =
        remember(decoration) {
            listOf(
                SketchLayer(decoration.role) { size, o ->
                    decoration.shape(size.width.toDouble(), size.height.toDouble(), o)
                },
            )
        }
    return drawablySketch(state, layers, lineWidth = DRAWABLY_DECORATION_WIDTH)
}

/** Draws a pen line under this composable. */
@Composable
public fun Modifier.drawablyUnderline(seed: UInt? = null): Modifier =
    drawablyDecoration(DrawablyDecoration.Underline, seed)

/** Washes a marker swipe behind this composable. */
@Composable
public fun Modifier.drawablyHighlight(seed: UInt? = null): Modifier =
    drawablyDecoration(DrawablyDecoration.Highlight, seed)

/** Loops a pen circle around this composable. */
@Composable
public fun Modifier.drawablyCircle(seed: UInt? = null): Modifier = drawablyDecoration(DrawablyDecoration.Circle, seed)
