package dev.drawably.compose.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** `#2724d1`, upstream's default ink. */
public val DrawablyPenBlue: Color = Color(0xFF2724D1)

/** `#d12724` */
public val DrawablyError: Color = Color(0xFFD12724)

/** `#188a42` */
public val DrawablySuccess: Color = Color(0xFF188A42)

/** `#6e675f`, the warm grey of the neutral tone. */
public val DrawablyNeutral: Color = Color(0xFF6E675F)

/**
 * The ink, paper and pen settings a sketch is drawn with.
 *
 * Mirrors upstream's CSS custom properties one for one, defaults included, and
 * travels down the tree the way they cascade.
 */
@Immutable
public data class DrawablyTheme(
    /** Line colour. */
    val stroke: Color = DrawablyPenBlue,
    /** Colour for filled layers: a solid button's blob, a radio's dot, a wash. */
    val fill: Color = DrawablyPenBlue,
    /** Background the ink sits on; a solid button's label is drawn in it. */
    val paper: Color = Color.White,
    /** Stroke width for ordinary layers. */
    val width: Dp = 2.dp,
    /** Ink for the danger tone and the error state. */
    val error: Color = DrawablyError,
    /** Ink for the success state. */
    val success: Color = DrawablySuccess,
    /** Jitter amplitude of the base sketch. */
    val roughness: Double = 1.0,
    /** Per-frame flicker amplitude. `0` renders a still sketch. */
    val boil: Double = 0.3,
)

public val LocalDrawablyTheme: ProvidableCompositionLocal<DrawablyTheme> =
    staticCompositionLocalOf { DrawablyTheme() }

/** Applies a theme to every Drawably control inside [content]. */
@Composable
public fun DrawablyTheme(
    theme: DrawablyTheme = DrawablyTheme(),
    content: @Composable () -> Unit,
) {
    androidx.compose.runtime.CompositionLocalProvider(
        LocalDrawablyTheme provides theme,
        content = content,
    )
}
