package dev.drawably.compose.sketch

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.drawably.compose.core.Rough
import dev.drawably.compose.core.RoughOptions
import dev.drawably.compose.core.SketchPath
import dev.drawably.compose.core.checkmark
import dev.drawably.compose.core.circle
import dev.drawably.compose.core.ellipse
import dev.drawably.compose.core.line
import dev.drawably.compose.core.roundedRect
import dev.drawably.compose.core.scribbleFill
import kotlin.math.min

/**
 * The per-control layer geometry, ported from upstream `src/controls.ts`.
 *
 * Each function is one drawn layer of one control, taking the control's box and
 * returning the shape for it. Components compose these; the golden tests pin
 * every one of them against the JS library's output.
 */
public object DrawablyGeometry {
    /**
     * How far a control's outline sits inside its box, leaving room for the
     * stroke and its jitter.
     */
    public const val INSET: Double = 3.0

    // region Shared rectangles

    public fun outlineRect(
        radius: Double,
        w: Double,
        h: Double,
        o: RoughOptions,
    ): SketchPath = Rough.roundedRect(INSET, INSET, w - 2 * INSET, h - 2 * INSET, radius, o)

    /** The focus ring sits just *outside* the box, unlike every other layer. */
    public fun focusRect(
        radius: Double,
        w: Double,
        h: Double,
        o: RoughOptions,
    ): SketchPath = Rough.roundedRect(-1.0, -1.0, w + 2, h + 2, radius, o)

    // endregion

    // region Button

    public fun buttonOutline(
        w: Double,
        h: Double,
        o: RoughOptions,
    ): SketchPath = outlineRect(8.0, w, h, o)

    /**
     * The `solid` variant's ink blob uses the same shape as the outline; only its
     * paint differs.
     */
    public fun buttonBlob(
        w: Double,
        h: Double,
        o: RoughOptions,
    ): SketchPath = outlineRect(8.0, w, h, o)

    public fun buttonScribble(
        w: Double,
        h: Double,
        o: RoughOptions,
    ): SketchPath = Rough.scribbleFill(INSET + 2, INSET + 2, w - 2 * INSET - 4, h - 2 * INSET - 4, o)

    public fun buttonFocus(
        w: Double,
        h: Double,
        o: RoughOptions,
    ): SketchPath = focusRect(10.0, w, h, o)

    // endregion

    public fun cardOutline(
        w: Double,
        h: Double,
        o: RoughOptions,
    ): SketchPath = outlineRect(10.0, w, h, o)

    // region Checkbox

    public fun checkboxOutline(
        w: Double,
        h: Double,
        o: RoughOptions,
    ): SketchPath = outlineRect(5.0, w, h, o)

    public fun checkboxCheck(
        w: Double,
        h: Double,
        o: RoughOptions,
    ): SketchPath = Rough.checkmark(w * 0.24, h * 0.2, w * 0.52, h * 0.5, o)

    public fun checkboxFocus(
        w: Double,
        h: Double,
        o: RoughOptions,
    ): SketchPath = focusRect(7.0, w, h, o)

    // endregion

    // region Radio

    public fun radioOutline(
        w: Double,
        h: Double,
        o: RoughOptions,
    ): SketchPath = Rough.circle(w / 2, h / 2, min(w, h) / 2 - INSET, o)

    public fun radioDot(
        w: Double,
        h: Double,
        o: RoughOptions,
    ): SketchPath = Rough.circle(w / 2, h / 2, min(w, h) * 0.18, o)

    public fun radioFocus(
        w: Double,
        h: Double,
        o: RoughOptions,
    ): SketchPath = Rough.circle(w / 2, h / 2, min(w, h) / 2 + 1, o)

    // endregion

    // region Toggle

    /** A pill: the corner radius is whatever makes the ends semicircular. */
    public fun toggleOutline(
        w: Double,
        h: Double,
        o: RoughOptions,
    ): SketchPath = outlineRect((h - 2 * INSET) / 2, w, h, o)

    @Suppress("UNUSED_PARAMETER")
    public fun toggleKnob(
        w: Double,
        h: Double,
        o: RoughOptions,
    ): SketchPath = Rough.circle(h / 2, h / 2, h / 2 - INSET - 3, o)

    public fun toggleFocus(
        w: Double,
        h: Double,
        o: RoughOptions,
    ): SketchPath = focusRect(12.0, w, h, o)

    /** How far the knob slides, for the default 44×24 pill. */
    public fun toggleKnobTravel(
        w: Double,
        h: Double,
    ): Double = w - h

    // endregion

    public fun dividerOutline(
        w: Double,
        h: Double,
        o: RoughOptions,
    ): SketchPath = Rough.line(INSET, h / 2, w - INSET, h / 2, o)

    // region Text fields, text areas and selects

    public fun fieldOutline(
        w: Double,
        h: Double,
        o: RoughOptions,
    ): SketchPath = outlineRect(6.0, w, h, o)

    public fun fieldFocus(
        w: Double,
        h: Double,
        o: RoughOptions,
    ): SketchPath = focusRect(8.0, w, h, o)

    public const val CHEVRON_WIDTH: Double = 12.0
    public const val CHEVRON_HEIGHT: Double = 6.0
    public const val CHEVRON_RIGHT: Double = 12.0

    /** At chevron scale, full roughness turns the V into noise. */
    public const val CHEVRON_ROUGHNESS: Double = 0.4

    public fun selectChevron(
        w: Double,
        h: Double,
        o: RoughOptions,
    ): SketchPath {
        val x = w - CHEVRON_RIGHT - CHEVRON_WIDTH
        val y = h / 2 - CHEVRON_HEIGHT / 2
        val co = o.copy(roughness = o.roughness * CHEVRON_ROUGHNESS)
        return Rough.line(x, y, x + CHEVRON_WIDTH / 2, y + CHEVRON_HEIGHT, co) +
            Rough.line(
                x + CHEVRON_WIDTH / 2,
                y + CHEVRON_HEIGHT,
                x + CHEVRON_WIDTH,
                y,
                co.copy(seed = o.seed + 1u),
            )
    }

    public const val CHECK_BOX: Double = 14.0
    public const val CHECK_INSET: Double = 2.0

    /** The tick drawn next to the chosen option in a picker. */
    @Suppress("UNUSED_PARAMETER")
    public fun selectCheckMask(
        w: Double,
        h: Double,
        o: RoughOptions,
    ): SketchPath {
        val side = CHECK_BOX - CHECK_INSET * 2
        return Rough.checkmark(CHECK_INSET, CHECK_INSET, side, side, o)
    }

    // endregion

    /**
     * How tall the tail is. The popup reserves this much at its top, so the
     * tail is drawn inside the box rather than hanging outside it.
     */
    public const val POPUP_TAIL_HEIGHT: Double = 10.0
    public const val POPUP_TAIL_WIDTH: Double = 18.0

    /** The popup's frame, which starts below the space the tail occupies. */
    public fun popupFrame(
        w: Double,
        h: Double,
        o: RoughOptions,
    ): SketchPath =
        Rough.roundedRect(
            INSET,
            INSET + POPUP_TAIL_HEIGHT,
            w - 2 * INSET,
            h - 2 * INSET - POPUP_TAIL_HEIGHT,
            6.0,
            o,
        )

    /**
     * A pen tail on the popup's top edge, pointing back at the control it
     * belongs to. Two strokes meeting at a point, drawn the way an arrow head
     * is — without it the popup floats unattached, since it carries none of the
     * platform's own chrome.
     */
    @Suppress("UNUSED_PARAMETER")
    public fun popupTail(
        w: Double,
        h: Double,
        o: RoughOptions,
    ): SketchPath {
        // centred, because the popup is centred under the control it belongs to
        val left = (w - POPUP_TAIL_WIDTH) / 2
        val apexX = left + POPUP_TAIL_WIDTH / 2
        val baseY = INSET + POPUP_TAIL_HEIGHT
        return Rough.line(left, baseY, apexX, INSET, o) +
            Rough.line(apexX, INSET, left + POPUP_TAIL_WIDTH, baseY, o.copy(seed = o.seed + 1u))
    }

    // region Badge

    /**
     * How far an outline's stroke can reach inside its box: the inset it is
     * drawn at, half its own width, and the jitter of the second, wider pass.
     *
     * Anything that has to stay clear of the stroke — a label inside a tight
     * box, say — has to allow for all three, and both of the last two come from
     * the theme.
     */
    public fun outlineReach(
        width: Dp,
        roughness: Double,
    ): Dp = (INSET + width.value / 2 + 1.5 * roughness * 1.4).dp

    /**
     * Upstream sets 1dp above and below, which leaves the label inside the
     * stroke's own reach — at the default width it lands on the text, and a
     * thicker pen or a rougher hand makes it worse.
     */
    public fun badgePadding(
        width: Dp,
        roughness: Double,
    ): PaddingValues {
        val reach = outlineReach(width, roughness)
        return PaddingValues(horizontal = reach + 5.dp, vertical = reach + 2.dp)
    }

    public fun badgeOutline(
        w: Double,
        h: Double,
        o: RoughOptions,
    ): SketchPath = outlineRect(2.0, w, h, o)

    public fun badgeScribble(
        w: Double,
        h: Double,
        o: RoughOptions,
    ): SketchPath = Rough.scribbleFill(INSET + 1, INSET + 1, w - 2 * INSET - 2, h - 2 * INSET - 2, o)

    // endregion

    // region List markers

    /**
     * Markers are drawn in the gutter to the left of the row, so their x is
     * negative relative to the row's box.
     */
    public const val MARKER_LEFT: Double = -18.0
    public const val MARKER_WIDTH: Double = 10.0
    public const val MARKER_LINE: Double = 22.0

    @Suppress("UNUSED_PARAMETER")
    public fun listDash(
        w: Double,
        h: Double,
        o: RoughOptions,
        lineHeight: Double = MARKER_LINE,
    ): SketchPath = Rough.line(MARKER_LEFT, lineHeight / 2, MARKER_LEFT + MARKER_WIDTH, lineHeight / 2, o)

    @Suppress("UNUSED_PARAMETER")
    public fun listCheck(
        w: Double,
        h: Double,
        o: RoughOptions,
        lineHeight: Double = MARKER_LINE,
    ): SketchPath =
        Rough.checkmark(
            MARKER_LEFT,
            lineHeight / 2 - MARKER_WIDTH / 2,
            MARKER_WIDTH,
            MARKER_WIDTH,
            o,
        )

    // endregion

    // region Text decorations

    public const val UNDERLINE_GAP: Double = 2.0

    /**
     * The loop overshoots the text box the way a hand circles a word rather than
     * tracing it.
     */
    public const val CIRCLE_PAD_X: Double = 1.15
    public const val CIRCLE_PAD_Y: Double = 1.4
    public const val CIRCLE_PAD: Double = 4.0

    public fun underline(
        w: Double,
        h: Double,
        o: RoughOptions,
    ): SketchPath = Rough.line(0.0, h + UNDERLINE_GAP, w, h + UNDERLINE_GAP, o)

    public fun highlightWash(
        w: Double,
        h: Double,
        o: RoughOptions,
    ): SketchPath = Rough.scribbleFill(0.0, 0.0, w, h, o)

    public fun circleOutline(
        w: Double,
        h: Double,
        o: RoughOptions,
    ): SketchPath =
        Rough.ellipse(
            w / 2,
            h / 2,
            (w / 2) * CIRCLE_PAD_X + CIRCLE_PAD,
            (h / 2) * CIRCLE_PAD_Y + CIRCLE_PAD,
            o,
        )

    // endregion

    /** Breathing room between an anchor's edge and the arrow's end. */
    public const val ARROW_GAP: Double = 6.0
}
