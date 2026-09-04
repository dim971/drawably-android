package dev.drawably.compose.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.drawably.compose.sketch.DrawablyGeometry
import dev.drawably.compose.sketch.SketchLayer
import dev.drawably.compose.sketch.SketchRole
import dev.drawably.compose.sketch.drawablySketch
import dev.drawably.compose.sketch.rememberDrawablySketchState

/**
 * The box every text-entry control shares: an outline and a focus ring.
 * Upstream never re-sketches these on hover, so nor do we.
 */
internal fun fieldLayers(
    focused: State<Boolean>,
    extra: List<SketchLayer> = emptyList(),
): List<SketchLayer> = buildList {
    add(SketchLayer(SketchRole.Outline) { size, o ->
        DrawablyGeometry.fieldOutline(size.width.toDouble(), size.height.toDouble(), o)
    })
    addAll(extra)
    add(SketchLayer(SketchRole.Focus, visible = { focused.value }) { size, o ->
        DrawablyGeometry.fieldFocus(size.width.toDouble(), size.height.toDouble(), o)
    })
}

/**
 * A single-line text field in a sketched box.
 *
 * ```kotlin
 * DrawablyTextField(name, onValueChange = { name = it }, placeholder = "Your name")
 * ```
 */
@Composable
public fun DrawablyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    enabled: Boolean = true,
    textStyle: TextStyle = TextStyle.Default,
    seed: UInt? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    DrawablyField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = placeholder,
        enabled = enabled,
        singleLine = true,
        minHeight = Dp.Unspecified,
        textStyle = textStyle,
        seed = seed,
        interactionSource = interactionSource,
    )
}

/** A multi-line text area in a sketched box. */
@Composable
public fun DrawablyTextArea(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    enabled: Boolean = true,
    minHeight: Dp = 96.dp,
    textStyle: TextStyle = TextStyle.Default,
    seed: UInt? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    DrawablyField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = placeholder,
        enabled = enabled,
        singleLine = false,
        minHeight = minHeight,
        textStyle = textStyle,
        seed = seed,
        interactionSource = interactionSource,
    )
}

@Composable
private fun DrawablyField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier,
    placeholder: String?,
    enabled: Boolean,
    singleLine: Boolean,
    minHeight: Dp,
    textStyle: TextStyle,
    seed: UInt?,
    interactionSource: MutableInteractionSource,
) {
    val state = rememberDrawablySketchState(seed)
    val focused = interactionSource.collectIsFocusedAsState()
    val layers = remember { fieldLayers(focused) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .let { if (minHeight == Dp.Unspecified) it else it.defaultMinSize(minHeight = minHeight) }
            .drawablySketch(state, layers)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        if (value.isEmpty() && placeholder != null) {
            androidx.compose.foundation.text.BasicText(
                text = placeholder,
                style = textStyle.copy(color = state.theme.stroke),
                modifier = Modifier.alpha(0.45f),
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = singleLine,
            textStyle = textStyle.copy(color = state.theme.stroke),
            cursorBrush = SolidColor(state.theme.stroke),
            interactionSource = interactionSource,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
