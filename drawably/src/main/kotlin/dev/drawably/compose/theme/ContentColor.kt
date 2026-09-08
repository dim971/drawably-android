package dev.drawably.compose.theme

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

/**
 * The colour a control's own label should be drawn in.
 *
 * This library sits on Compose Foundation rather than Material, so it cannot
 * set Material's `LocalContentColor`. A solid button's label has to be paper
 * rather than ink, so it publishes the colour here instead: [DrawablyText]
 * reads it, and a slot filled with someone else's text component can too.
 */
public val LocalDrawablyContentColor: ProvidableCompositionLocal<Color> =
    compositionLocalOf { DrawablyPenBlue }

/** Text in the colour the surrounding Drawably control asks for. */
@Composable
public fun DrawablyText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = LocalDrawablyContentColor.current,
    style: TextStyle = TextStyle.Default,
) {
    BasicText(
        text = text,
        modifier = modifier,
        style = style.copy(color = color),
    )
}
