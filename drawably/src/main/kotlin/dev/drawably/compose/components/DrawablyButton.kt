package dev.drawably.compose.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import dev.drawably.compose.sketch.DRAWABLY_BOIL_PERIOD_MS
import dev.drawably.compose.sketch.DRAWABLY_LOADING_BOIL_PERIOD_MS
import dev.drawably.compose.sketch.DrawablyGeometry
import dev.drawably.compose.sketch.ResketchOnInteraction
import dev.drawably.compose.sketch.SketchLayer
import dev.drawably.compose.sketch.SketchRole
import dev.drawably.compose.sketch.drawablySketch
import dev.drawably.compose.sketch.rememberDrawablyBoilFrame
import dev.drawably.compose.sketch.rememberDrawablyReduceMotion
import dev.drawably.compose.sketch.rememberDrawablySeed
import dev.drawably.compose.theme.DrawablyNeutral
import dev.drawably.compose.theme.DrawablyText
import dev.drawably.compose.theme.LocalDrawablyContentColor
import dev.drawably.compose.theme.LocalDrawablyTheme

/** How a button's box is filled in. */
public enum class DrawablyButtonVariant {
    /** Just the sketched border. */
    Outline,

    /** A solid ink blob behind the label. */
    Solid,

    /** Hatched fill. */
    Scribble,
}

/** The ink a control is drawn in, independent of what it is doing. */
public enum class DrawablyTone {
    /** The theme's own ink. */
    Standard,

    /** Warm grey, for secondary actions. */
    Neutral,

    /** The theme's error red, for destructive actions. */
    Danger,
}

/**
 * What a button is currently doing, which recolours its ink and,
 * for [Loading], makes the sketch boil faster.
 */
public enum class DrawablyButtonState {
    Idle,
    Loading,
    Error,
    Success,
}

/**
 * A button drawn as a pen sketch: a fresh one on first composition, and another
 * every time it is pressed.
 *
 * ```kotlin
 * DrawablyButton(onClick = ::submit, variant = DrawablyButtonVariant.Solid) {
 *     Text("Done")
 * }
 * ```
 */
@Composable
public fun DrawablyButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: DrawablyButtonVariant = DrawablyButtonVariant.Outline,
    tone: DrawablyTone = DrawablyTone.Standard,
    state: DrawablyButtonState = DrawablyButtonState.Idle,
    enabled: Boolean = true,
    seed: UInt? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit,
) {
    val base = LocalDrawablyTheme.current
    val theme =
        remember(base, tone) {
            when (tone) {
                DrawablyTone.Standard -> base
                DrawablyTone.Neutral -> base.copy(stroke = DrawablyNeutral, fill = DrawablyNeutral)
                DrawablyTone.Danger -> base.copy(stroke = base.error, fill = base.error)
            }
        }
    // `--drawably-ink`: a state recolours the whole sketch without touching the
    // theme's own ink.
    val ink: Color? =
        when (state) {
            DrawablyButtonState.Error -> base.error
            DrawablyButtonState.Success -> base.success
            else -> null
        }
    val clickable = enabled && state != DrawablyButtonState.Loading

    val drawablySeed = rememberDrawablySeed(seed)
    val reduceMotion = rememberDrawablyReduceMotion()
    drawablySeed.ResketchOnInteraction(interactionSource, enabled = clickable && !reduceMotion)

    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()
    // held as State, not read here: the focus ring appears without the layer
    // list being rebuilt and the sketch regenerated
    val focused = interactionSource.collectIsFocusedAsState()

    val frame =
        rememberDrawablyBoilFrame(
            periodMillis =
                if (state == DrawablyButtonState.Loading) {
                    DRAWABLY_LOADING_BOIL_PERIOD_MS
                } else {
                    DRAWABLY_BOIL_PERIOD_MS
                },
            enabled = !reduceMotion && theme.boil != 0.0,
        )

    val labelColor = ink ?: if (variant == DrawablyButtonVariant.Solid) theme.paper else theme.stroke

    val wash: () -> Color? = {
        drawablyButtonWash(
            ink = labelColor,
            variant = variant,
            enabled = clickable,
            pressed = isPressed,
            hovered = isHovered,
        )
    }
    // Only the variant changes which shapes exist; press, hover and focus change
    // how they are painted, which is a redraw rather than a regeneration. The
    // ink is a key because the wash lambda closes over it.
    val layers =
        remember(variant, labelColor, clickable) {
            buildButtonLayers(variant, wash) { focused.value }
        }

    val density = LocalDensity.current
    // the press thickens the outline; widths are in the same units as the
    // geometry, which is dp
    val strokeWidth =
        theme.width.value
            .toDouble()
            .let { if (isPressed) it * 1.4 else it }

    // the pen pushes into the paper on press and lifts on hover
    val lift by animateFloatAsState(
        targetValue =
            when {
                isPressed -> 1f
                isHovered && clickable -> -1f
                else -> 0f
            },
        label = "drawablyLift",
    )
    val squash by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        label = "drawablySquash",
    )

    CompositionLocalProvider(LocalDrawablyTheme provides theme) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                modifier
                    .graphicsLayer {
                        translationY = lift * density.density
                        scaleX = squash
                        scaleY = squash
                    }.alpha(buttonAlpha(enabled, state))
                    // Before the sketch, so the drawn box grows with the node
                    // rather than floating inside a larger touch target. See
                    // DrawablyTheme.minimumControlHeight.
                    .defaultMinSize(minHeight = theme.minimumControlHeight)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        enabled = clickable,
                        onClick = onClick,
                    ).drawablySketch(
                        layers = layers,
                        seed = drawablySeed.value,
                        theme = theme,
                        frame = frame,
                        ink = ink,
                        lineWidth = strokeWidth,
                    ).padding(horizontal = 14.dp, vertical = 6.dp),
        ) {
            CompositionLocalProvider(LocalDrawablyContentColor provides labelColor) {
                content()
            }
        }
    }
}

private fun buttonAlpha(
    enabled: Boolean,
    state: DrawablyButtonState,
): Float =
    when {
        !enabled -> 0.45f
        state == DrawablyButtonState.Loading -> 0.6f
        else -> 1f
    }

private fun buildButtonLayers(
    variant: DrawablyButtonVariant,
    wash: () -> Color?,
    focused: () -> Boolean,
): List<SketchLayer> =
    buildList {
        if (variant == DrawablyButtonVariant.Solid) {
            add(
                SketchLayer(SketchRole.Blob) { size, o ->
                    DrawablyGeometry.buttonBlob(size.width.toDouble(), size.height.toDouble(), o)
                },
            )
        }
        if (variant == DrawablyButtonVariant.Scribble) {
            add(
                SketchLayer(SketchRole.Scribble) { size, o ->
                    DrawablyGeometry.buttonScribble(size.width.toDouble(), size.height.toDouble(), o)
                },
            )
        }
        add(
            SketchLayer(SketchRole.Outline, fill = wash) { size, o ->
                DrawablyGeometry.buttonOutline(size.width.toDouble(), size.height.toDouble(), o)
            },
        )
        add(
            SketchLayer(SketchRole.Focus, visible = focused) { size, o ->
                DrawablyGeometry.buttonFocus(size.width.toDouble(), size.height.toDouble(), o)
            },
        )
    }

/** The common case: a button with a plain text label. */
@Composable
public fun DrawablyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: DrawablyButtonVariant = DrawablyButtonVariant.Outline,
    tone: DrawablyTone = DrawablyTone.Standard,
    state: DrawablyButtonState = DrawablyButtonState.Idle,
    enabled: Boolean = true,
    seed: UInt? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    DrawablyButton(
        onClick = onClick,
        modifier = modifier,
        variant = variant,
        tone = tone,
        state = state,
        enabled = enabled,
        seed = seed,
        interactionSource = interactionSource,
    ) {
        DrawablyText(text)
    }
}

/** How much ink a button washes its inside with. */
public object DrawablyButtonWash {
    /** Upstream's hover wash. */
    public const val HOVER: Float = 0.1f

    /**
     * A press gets a stronger one, and gets it on touch devices, where hover
     * never happens and the sink-and-thicken alone is easy to miss under a
     * fingertip.
     */
    public const val PRESSED: Float = 0.18f
}

/**
 * The wash painted inside a button's outline, or `null` for none.
 *
 * A solid button is already filled, so there would be nothing to see; a
 * disabled one does not react at all.
 */
public fun drawablyButtonWash(
    ink: Color,
    variant: DrawablyButtonVariant,
    enabled: Boolean,
    pressed: Boolean,
    hovered: Boolean,
): Color? =
    when {
        !enabled || variant == DrawablyButtonVariant.Solid -> null
        pressed -> ink.copy(alpha = DrawablyButtonWash.PRESSED)
        hovered -> ink.copy(alpha = DrawablyButtonWash.HOVER)
        else -> null
    }
