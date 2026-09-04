package dev.drawably.compose.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * The colour a control's own label should be drawn in.
 *
 * This library sits on Compose Foundation rather than Material, so it cannot
 * set Material's `LocalContentColor`. A solid button's label has to be paper
 * rather than ink, so it publishes the colour here instead — [DrawablyText]
 * reads it, and a slot filled with someone else's text component can too.
 */
public val LocalDrawablyContentColor: ProvidableCompositionLocal<Color> =
    compositionLocalOf { DrawablyPenBlue }

/** Text in the colour the surrounding Drawably control asks for. */
@Composable
public fun DrawablyText(
    text: String,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
    color: Color = LocalDrawablyContentColor.current,
    style: androidx.compose.ui.text.TextStyle = androidx.compose.ui.text.TextStyle.Default,
) {
    androidx.compose.foundation.text.BasicText(
        text = text,
        modifier = modifier,
        style = style.copy(color = color),
    )
}
