package dev.drawably.compose.core

import com.google.gson.Gson
import dev.drawably.compose.sketch.DrawablyGeometry
import java.io.InputStreamReader

/**
 * Fixtures generated from the published `drawably` npm package by
 * `Tools/gen-goldens.mjs`. Regenerate them when tracking a new upstream
 * version; never hand-edit them.
 */
internal data class Goldens(
    val upstream: String,
    val prng: List<PrngCase>,
    val shapes: List<ShapeCase>,
    val variants: List<VariantCase>,
    val controls: List<ControlCase>,
    val arrows: List<ArrowCase>,
) {
    // seeds arrive as JSON numbers, so they are read as Long and narrowed here
    internal data class Options(val seed: Long, val roughness: Double, val boil: Double?) {
        val rough: RoughOptions
            get() = RoughOptions(seed.toUInt(), roughness, boil ?: 0.0)
    }

    internal data class PrngCase(val seed: Long, val values: List<Double>)

    internal data class ShapeCase(
        val fn: String,
        val args: List<Double>,
        val opts: Options,
        val d: String,
    )

    internal data class VariantCase(
        val fn: String,
        val args: List<Double>,
        val opts: Options,
        val n: Int,
        val ds: List<String>,
    )

    internal data class ControlCase(
        val layer: String,
        val w: Double,
        val h: Double,
        val opts: Options,
        val ds: List<String>,
    )

    internal data class ArrowCase(
        val layer: String,
        val args: List<Double>,
        val opts: Options,
        val ds: List<String>,
    )

    internal companion object {
        val shared: Goldens by lazy {
            val stream = checkNotNull(Goldens::class.java.getResourceAsStream("/goldens.json")) {
                "goldens.json is missing from the test resources"
            }
            InputStreamReader(stream).use { Gson().fromJson(it, Goldens::class.java) }
        }
    }
}

/** Rebuilds a primitive from the name and argument list the fixture records. */
internal fun generate(fn: String, a: List<Double>, o: RoughOptions): SketchPath = when (fn) {
    "roughLine" -> Rough.line(a[0], a[1], a[2], a[3], o)
    "roughRoundedRect" -> Rough.roundedRect(a[0], a[1], a[2], a[3], a[4], o)
    "roughCircle" -> Rough.circle(a[0], a[1], a[2], o)
    "roughEllipse" -> Rough.ellipse(a[0], a[1], a[2], a[3], o)
    "roughArrow" -> Rough.arrow(a[0], a[1], a[2], a[3], o)
    "roughCheckmark" -> Rough.checkmark(a[0], a[1], a[2], a[3], o)
    "scribbleFill" -> Rough.scribbleFill(a[0], a[1], a[2], a[3], o)
    else -> error("unknown primitive in fixtures: $fn")
}

/**
 * A lookup table rather than a `when`: the fixture's layer names map one-to-one
 * onto [DrawablyGeometry], and a table keeps that obvious.
 */
internal val layerGenerators: Map<String, (Double, Double, RoughOptions) -> SketchPath> = mapOf(
    "button.outline" to DrawablyGeometry::buttonOutline,
    "button.blob" to DrawablyGeometry::buttonBlob,
    "button.scribble" to DrawablyGeometry::buttonScribble,
    "button.focus" to DrawablyGeometry::buttonFocus,
    "card.outline" to DrawablyGeometry::cardOutline,
    "checkbox.outline" to DrawablyGeometry::checkboxOutline,
    "checkbox.check" to DrawablyGeometry::checkboxCheck,
    "checkbox.focus" to DrawablyGeometry::checkboxFocus,
    "radio.outline" to DrawablyGeometry::radioOutline,
    "radio.dot" to DrawablyGeometry::radioDot,
    "radio.focus" to DrawablyGeometry::radioFocus,
    "toggle.outline" to DrawablyGeometry::toggleOutline,
    "toggle.knob" to DrawablyGeometry::toggleKnob,
    "toggle.focus" to DrawablyGeometry::toggleFocus,
    "divider.outline" to DrawablyGeometry::dividerOutline,
    "field.outline" to DrawablyGeometry::fieldOutline,
    "field.focus" to DrawablyGeometry::fieldFocus,
    "select.chevron" to DrawablyGeometry::selectChevron,
    "select.checkMask" to DrawablyGeometry::selectCheckMask,
    "badge.outline" to DrawablyGeometry::badgeOutline,
    "badge.scribble" to DrawablyGeometry::badgeScribble,
    "list.dash" to { w, h, o -> DrawablyGeometry.listDash(w, h, o) },
    "list.check" to { w, h, o -> DrawablyGeometry.listCheck(w, h, o) },
    "underline.outline" to DrawablyGeometry::underline,
    "highlight.wash" to DrawablyGeometry::highlightWash,
    "circle.outline" to DrawablyGeometry::circleOutline,
)

internal fun generateLayer(name: String, w: Double, h: Double, o: RoughOptions): SketchPath {
    val gen = layerGenerators[name] ?: error("unknown layer in fixtures: $name")
    return gen(w, h, o)
}

/** One case where this port and upstream disagree. */
internal data class Mismatch(val label: String, val expected: String, val actual: String)

/**
 * Formats the first few mismatches so a failure says which case broke and where
 * the two strings diverge, rather than dumping thousands of characters.
 */
internal fun report(mismatches: List<Mismatch>): String {
    if (mismatches.isEmpty()) return ""
    val head = mismatches.take(3).joinToString("\n") { m ->
        val common = m.expected.zip(m.actual).takeWhile { (a, b) -> a == b }.count()
        val from = maxOf(0, common - 20)
        fun window(s: String) = s.substring(from, minOf(s.length, from + 60))
        """
        |${m.label}
        |  diverges at character $common
        |  expected …${window(m.expected)}…
        |  actual   …${window(m.actual)}…
        """.trimMargin()
    }
    return "${mismatches.size} mismatch(es):\n$head"
}
