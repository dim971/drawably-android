package dev.drawably.compose.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import dev.drawably.compose.sketch.DrawablyGeometry
import dev.drawably.compose.sketch.SketchLayer
import dev.drawably.compose.sketch.SketchRole
import dev.drawably.compose.sketch.drawablySketch
import dev.drawably.compose.sketch.rememberDrawablySketchState

/**
 * A select drawn as a sketched box with a pen chevron, opening a sketched
 * popup rather than the platform's own — the option list carries a hand-drawn
 * frame and tick, the way upstream draws them into a customisable `<select>`.
 *
 * ```kotlin
 * DrawablySelect(selected = weight, options = listOf("Light", "Medium"), onSelect = { weight = it })
 * ```
 */
@Composable
public fun <T> DrawablySelect(
    selected: T,
    options: List<T>,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = TextStyle.Default,
    seed: UInt? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    title: (T) -> String = { it.toString() },
) {
    val state = rememberDrawablySketchState(seed)
    val focused = interactionSource.collectIsFocusedAsState()
    var isOpen by remember { mutableStateOf(false) }

    val layers = remember {
        fieldLayers(
            focused,
            extra = listOf(
                SketchLayer(SketchRole.Chevron) { size, o ->
                    DrawablyGeometry.selectChevron(size.width.toDouble(), size.height.toDouble(), o)
                },
            ),
        )
    }

    Box(modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                ) { isOpen = true }
                .drawablySketch(state, layers)
                .padding(start = 12.dp, top = 8.dp, bottom = 8.dp)
                // the gutter the sketched chevron is drawn into
                .padding(end = 34.dp),
        ) {
            // Every option is laid out invisibly under the chosen one, so the
            // box is already as wide as the widest — picking never shifts the
            // layout around it.
            Box {
                options.forEach { option ->
                    BasicText(
                        text = title(option),
                        style = textStyle.copy(color = state.theme.stroke),
                        modifier = Modifier.alpha(0f),
                    )
                }
                BasicText(text = title(selected), style = textStyle.copy(color = state.theme.stroke))
            }
        }

        if (isOpen) {
            Popup(onDismissRequest = { isOpen = false }) {
                DrawablySelectOptions(
                    options = options,
                    selected = selected,
                    title = title,
                    textStyle = textStyle,
                    seed = state.seed.value,
                    onSelect = {
                        onSelect(it)
                        isOpen = false
                    },
                )
            }
        }
    }
}

/** The popup: a sketched frame round the options, and a pen tick beside the chosen one. */
@Composable
private fun <T> DrawablySelectOptions(
    options: List<T>,
    selected: T,
    title: (T) -> String,
    textStyle: TextStyle,
    seed: UInt,
    onSelect: (T) -> Unit,
) {
    val state = rememberDrawablySketchState(seed)
    val frameLayers = remember {
        listOf(
            SketchLayer(SketchRole.Outline) { size, o ->
                DrawablyGeometry.fieldOutline(size.width.toDouble(), size.height.toDouble(), o)
            },
        )
    }
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .drawBehind { drawRect(state.theme.paper) }
            .drawablySketch(state, frameLayers)
            .padding(6.dp),
    ) {
        options.forEachIndexed { index, option ->
            val tickLayers = remember(option == selected) {
                listOf(
                    SketchLayer(SketchRole.Check, visible = { option == selected }) { size, o ->
                        DrawablyGeometry.selectCheckMask(
                            size.width.toDouble(),
                            size.height.toDouble(),
                            o,
                        )
                    },
                )
            }
            // each row gets its own sketch, the way upstream seeds list items
            val rowState = rememberDrawablySketchState(seed + index.toUInt())
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(option) }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Box(
                    Modifier
                        .size(DrawablyGeometry.CHECK_BOX.dp)
                        .drawablySketch(rowState, tickLayers),
                )
                Spacer(Modifier.width(8.dp))
                BasicText(text = title(option), style = textStyle.copy(color = state.theme.stroke))
            }
        }
    }
}
