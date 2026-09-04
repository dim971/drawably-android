package dev.drawably.compose.components

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import dev.drawably.compose.core.Rough
import dev.drawably.compose.core.RoughOptions
import dev.drawably.compose.sketch.rememberDrawablySketchState
import dev.drawably.compose.sketch.toComposePath

/**
 * Text with a mark drawn on every line it occupies.
 *
 * Upstream reads `getClientRects()` to get one box per wrapped line; the
 * equivalent here is the [TextLayoutResult] the text reports back, which gives
 * the same per-line boxes.
 */
@Composable
public fun DrawablyDecoratedText(
    text: String,
    decoration: DrawablyDecoration,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default,
    seed: UInt? = null,
) {
    val state = rememberDrawablySketchState(seed)
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    val color = if (decoration.role.usesFillColor) state.theme.fill else state.theme.stroke

    BasicText(
        text = text,
        style = style.copy(color = state.theme.stroke),
        onTextLayout = { layout = it },
        modifier =
            modifier.drawWithCache {
                val result = layout
                // line boxes arrive in pixels; the sketch is generated in the
                // density-independent units roughness is expressed in
                val frames =
                    result?.let {
                        buildLineFrames(
                            layout = it,
                            density = density,
                            seed = state.seed.value,
                            roughness = state.theme.roughness,
                            boil = state.theme.boil,
                            decoration = decoration,
                        )
                    } ?: emptyList()

                onDrawWithContent {
                    // a highlight goes behind the words; a line or a loop goes over
                    if (decoration == DrawablyDecoration.Highlight) {
                        drawMarks(frames, decoration, color, state.frame.value)
                    }
                    drawContent()
                    if (decoration != DrawablyDecoration.Highlight) {
                        drawMarks(frames, decoration, color, state.frame.value)
                    }
                }
            },
    )
}

/** One line's boil frames, and where on the page they belong. */
private class LineFrames(
    val left: Float,
    val top: Float,
    val paths: List<Path>,
)

private fun buildLineFrames(
    layout: TextLayoutResult,
    density: Float,
    seed: UInt,
    roughness: Double,
    boil: Double,
    decoration: DrawablyDecoration,
): List<LineFrames> =
    (0 until layout.lineCount).map { line ->
        val left = layout.getLineLeft(line) / density
        val top = layout.getLineTop(line) / density
        val size =
            Size(
                layout.getLineRight(line) / density - left,
                layout.getLineBottom(line) / density - top,
            )
        val options = RoughOptions(seed = seed + line.toUInt(), roughness = roughness, boil = boil)
        LineFrames(
            left = left,
            top = top,
            paths =
                Rough
                    .variants(
                        { o -> decoration.shape(size.width.toDouble(), size.height.toDouble(), o) },
                        options,
                        if (boil == 0.0) 1 else 3,
                    ).map { it.toComposePath() },
        )
    }

private fun DrawScope.drawMarks(
    lines: List<LineFrames>,
    decoration: DrawablyDecoration,
    color: androidx.compose.ui.graphics.Color,
    frame: Int,
) {
    val role = decoration.role
    val stroke =
        Stroke(
            width = (role.fixedLineWidth ?: DRAWABLY_DECORATION_WIDTH).toFloat(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        )
    scale(density, density, pivot = Offset.Zero) {
        for (line in lines) {
            translate(left = line.left, top = line.top) {
                drawPath(
                    path = line.paths[frame % line.paths.size],
                    color = color,
                    alpha = role.opacity,
                    style = stroke,
                    blendMode = role.blendMode,
                )
            }
        }
    }
}
