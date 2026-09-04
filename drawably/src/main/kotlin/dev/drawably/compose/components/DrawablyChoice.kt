package dev.drawably.compose.components

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.drawably.compose.sketch.DrawablyGeometry
import dev.drawably.compose.sketch.ResketchOnInteraction
import dev.drawably.compose.sketch.SketchLayer
import dev.drawably.compose.sketch.SketchRole
import dev.drawably.compose.sketch.drawablySketch
import dev.drawably.compose.sketch.rememberDrawablySketchState

/** Upstream's `--drawably-ease`, `cubic-bezier(0.2, 0, 0, 1)`. */
public val DrawablyEase: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

/** Upstream sizes a checkbox and a radio at 22px square. */
public val DrawablyCheckboxSize: Dp = 22.dp

/** Upstream sizes a toggle at 44×24px. */
public val DrawablyToggleWidth: Dp = 44.dp
public val DrawablyToggleHeight: Dp = 24.dp

/**
 * A checkbox whose tick is drawn on, stroke by stroke, when it is ticked.
 *
 * ```kotlin
 * DrawablyCheckbox(checked = agreed, onCheckedChange = { agreed = it }) {
 *     Text("Ship it")
 * }
 * ```
 */
@Composable
public fun DrawablyCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    seed: UInt? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    label: @Composable (RowScope.() -> Unit)? = null,
) {
    val state = rememberDrawablySketchState(seed)
    state.seed.ResketchOnInteraction(interactionSource, enabled = enabled && !state.reduceMotion)
    val focused = interactionSource.collectIsFocusedAsState()

    // the tick is drawn on rather than faded in, the way upstream animates
    // stroke-dashoffset
    val drawn by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = tween(durationMillis = 240, easing = DrawablyEase),
        label = "drawablyCheck",
    )
    val layers =
        remember {
            listOf(
                SketchLayer(SketchRole.Outline) { size, o ->
                    DrawablyGeometry.checkboxOutline(size.width.toDouble(), size.height.toDouble(), o)
                },
                SketchLayer(SketchRole.Check, trim = { drawn }) { size, o ->
                    DrawablyGeometry.checkboxCheck(size.width.toDouble(), size.height.toDouble(), o)
                },
                SketchLayer(SketchRole.Focus, visible = { focused.value }) { size, o ->
                    DrawablyGeometry.checkboxFocus(size.width.toDouble(), size.height.toDouble(), o)
                },
            )
        }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier.toggleable(
                value = checked,
                onValueChange = onCheckedChange,
                enabled = enabled,
                role = Role.Checkbox,
                interactionSource = interactionSource,
                indication = null,
            ),
    ) {
        Box(
            Modifier
                .size(DrawablyCheckboxSize)
                .drawablySketch(state, layers),
        )
        if (label != null) {
            Spacer(Modifier.width(8.dp))
            label()
        }
    }
}

/**
 * A pill switch with an ink blob that slides across it.
 *
 * ```kotlin
 * DrawablySwitch(checked = boiling, onCheckedChange = { boiling = it })
 * ```
 */
@Composable
public fun DrawablySwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    seed: UInt? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    label: @Composable (RowScope.() -> Unit)? = null,
) {
    val state = rememberDrawablySketchState(seed)
    state.seed.ResketchOnInteraction(interactionSource, enabled = enabled && !state.reduceMotion)
    val focused = interactionSource.collectIsFocusedAsState()

    // the knob is drawn at the left end and slid across; its travel is the
    // pill's width less its height, so the circle lands centred either way
    val travelDp =
        DrawablyGeometry
            .toggleKnobTravel(
                DrawablyToggleWidth.value.toDouble(),
                DrawablyToggleHeight.value.toDouble(),
            ).toFloat()
    val travel by animateFloatAsState(
        targetValue = if (checked) travelDp else 0f,
        animationSpec = tween(durationMillis = 160, easing = DrawablyEase),
        label = "drawablyKnob",
    )
    val layers =
        remember {
            listOf(
                SketchLayer(SketchRole.Outline) { size, o ->
                    DrawablyGeometry.toggleOutline(size.width.toDouble(), size.height.toDouble(), o)
                },
                SketchLayer(SketchRole.Knob, offsetX = { travel }) { size, o ->
                    DrawablyGeometry.toggleKnob(size.width.toDouble(), size.height.toDouble(), o)
                },
                SketchLayer(SketchRole.Focus, visible = { focused.value }) { size, o ->
                    DrawablyGeometry.toggleFocus(size.width.toDouble(), size.height.toDouble(), o)
                },
            )
        }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier.toggleable(
                value = checked,
                onValueChange = onCheckedChange,
                enabled = enabled,
                role = Role.Switch,
                interactionSource = interactionSource,
                indication = null,
            ),
    ) {
        if (label != null) {
            label()
            Spacer(Modifier.width(8.dp))
        }
        Box(
            Modifier
                .width(DrawablyToggleWidth)
                .height(DrawablyToggleHeight)
                .drawablySketch(state, layers),
        )
    }
}

/**
 * One option in a radio group: a sketched ring that gains a dot when picked.
 *
 * ```kotlin
 * DrawablyRadioButton(selected = tool == Pen, onClick = { tool = Pen })
 * ```
 */
@Composable
public fun DrawablyRadioButton(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    seed: UInt? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    label: @Composable (RowScope.() -> Unit)? = null,
) {
    val state = rememberDrawablySketchState(seed)
    state.seed.ResketchOnInteraction(interactionSource, enabled = enabled && !state.reduceMotion)
    val focused = interactionSource.collectIsFocusedAsState()

    // the dot pops in from half size rather than fading
    val pop by animateFloatAsState(
        targetValue = if (selected) 1f else 0.5f,
        animationSpec = tween(durationMillis = 160, easing = DrawablyEase),
        label = "drawablyDot",
    )
    val layers =
        remember {
            listOf(
                SketchLayer(SketchRole.Outline) { size, o ->
                    DrawablyGeometry.radioOutline(size.width.toDouble(), size.height.toDouble(), o)
                },
                SketchLayer(
                    SketchRole.Dot,
                    visible = { selected },
                    scale = { pop },
                ) { size, o ->
                    DrawablyGeometry.radioDot(size.width.toDouble(), size.height.toDouble(), o)
                },
                SketchLayer(SketchRole.Focus, visible = { focused.value }) { size, o ->
                    DrawablyGeometry.radioFocus(size.width.toDouble(), size.height.toDouble(), o)
                },
            )
        }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier.selectable(
                selected = selected,
                onClick = onClick,
                enabled = enabled,
                role = Role.RadioButton,
                interactionSource = interactionSource,
                indication = null,
            ),
    ) {
        Box(
            Modifier
                .size(DrawablyCheckboxSize)
                .drawablySketch(state, layers),
        )
        if (label != null) {
            Spacer(Modifier.width(8.dp))
            label()
        }
    }
}
