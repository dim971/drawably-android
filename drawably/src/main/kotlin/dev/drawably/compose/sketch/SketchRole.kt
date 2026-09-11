package dev.drawably.compose.sketch

import androidx.compose.ui.graphics.BlendMode

/**
 * What a drawn layer *is*, which decides how it is painted.
 *
 * One entry per path class in upstream's stylesheet, so the paint rules can be
 * read against it directly.
 */
public enum class SketchRole {
    /** The control's border. */
    Outline,

    /** A solid ink fill, as under a solid button. */
    Blob,

    /** Hatched fill. */
    Scribble,

    /** The ring shown while the control has focus. */
    Focus,

    /** A radio's centre dot. */
    Dot,

    /** A toggle's sliding knob. */
    Knob,

    /** A checkbox's tick. */
    Check,

    /** A select's V. */
    Chevron,

    /** A list row's bullet. */
    Marker,

    /** A highlighter's translucent swipe. */
    Wash,
    ;

    /** Whether the shape is filled as well as stroked. */
    public val isFilled: Boolean get() = this == Blob || this == Dot || this == Knob

    /** Which of the theme's two ink colours the layer takes. */
    public val usesFillColor: Boolean
        get() = this == Blob || this == Dot || this == Knob || this == Wash

    /** `null` means "whatever the theme's stroke width is". */
    public val fixedLineWidth: Double?
        get() =
            when (this) {
                Blob, Knob -> 4.0
                Scribble, Focus -> 1.5
                Wash -> 6.0
                else -> null
            }

    public val opacity: Float get() = if (this == Wash) 0.3f else 1f

    /**
     * How strongly this layer is drawn, given the theme it is drawn with.
     *
     * Only two roles are ever less than solid: a wash, which is a wash by
     * definition, and a scribble, whose strength the theme owns so a small
     * control can keep its hatching without losing its label.
     */
    public fun opacity(scribble: Float): Float =
        when (this) {
            Wash -> 0.3f
            Scribble -> scribble
            else -> 1f
        }

    public val blendMode: BlendMode get() = if (this == Wash) BlendMode.Multiply else BlendMode.SrcOver
}
