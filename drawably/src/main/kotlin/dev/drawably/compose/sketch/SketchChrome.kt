package dev.drawably.compose.sketch

import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalContext
import dev.drawably.compose.core.Rough
import dev.drawably.compose.core.RoughOptions
import dev.drawably.compose.core.SketchPath
import dev.drawably.compose.theme.DrawablyTheme
import kotlinx.coroutines.delay

/**
 * One drawn layer of a control: what shape to generate, and the presentation
 * state that animates it.
 *
 * Every piece of presentation is a lambda so it is read in the draw phase
 * rather than during composition — changing a trim or an offset then redraws
 * without invalidating the generated geometry.
 */
public class SketchLayer(
    public val role: SketchRole,
    /** Hidden layers keep their geometry, so a focus ring does not re-sketch. */
    public val visible: () -> Boolean = { true },
    /** How much of the stroke is drawn, for the checkbox's tick. */
    public val trim: () -> Float = { 1f },
    /** Horizontal travel, for the toggle's knob. */
    public val offsetX: () -> Float = { 0f },
    /** Scale about the centre, for the radio's dot. */
    public val scale: () -> Float = { 1f },
    /**
     * Paints the inside of a normally unfilled layer, as a press or hover wash
     * does. A lambda like the rest, so touching a control redraws it rather
     * than regenerating its geometry.
     */
    public val fill: () -> Color? = { null },
    public val generate: (Size, RoughOptions) -> SketchPath,
)

/** Upstream boils at 1200ms across three frames. */
public const val DRAWABLY_BOIL_PERIOD_MS: Long = 400

/** A button in its loading state boils at 450ms instead. */
public const val DRAWABLY_LOADING_BOIL_PERIOD_MS: Long = 150

private const val BOIL_FRAMES = 3

/**
 * Whether the device is asking for less movement.
 *
 * Android has no direct equivalent of `prefers-reduced-motion`; turning
 * animations off in developer options or accessibility settings zeroes the
 * animator duration scale, which is the signal apps are expected to honour.
 */
@Composable
public fun rememberDrawablyReduceMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        ) == 0f
    }
}

/**
 * The index of the boil frame currently showing.
 *
 * Held as state and read in the draw phase, so a tick redraws without
 * recomposing anything or throwing away the generated frames.
 */
@Composable
public fun rememberDrawablyBoilFrame(
    periodMillis: Long = DRAWABLY_BOIL_PERIOD_MS,
    enabled: Boolean = true,
): State<Int> =
    produceState(0, periodMillis, enabled) {
        if (!enabled) {
            value = 0
            return@produceState
        }
        while (true) {
            delay(periodMillis)
            value = (value + 1) % BOIL_FRAMES
        }
    }

/**
 * Draws a sketch behind the content.
 *
 * The boil frames are generated once per box size, seed and options inside
 * [drawWithCache]'s cache block — never in a draw pass — and the draw pass only
 * picks which one to stroke.
 *
 * Geometry is generated in density-independent units and the canvas is scaled
 * to pixels around it. Upstream works in CSS pixels, and roughness is an
 * absolute amplitude — generating against a pixel size would make the jitter
 * three times finer on a 3x screen than on the web, which reads as a clean
 * rectangle rather than a drawn one.
 */
public fun Modifier.drawablySketch(
    layers: List<SketchLayer>,
    seed: UInt,
    theme: DrawablyTheme,
    frame: State<Int>,
    ink: Color? = null,
    lineWidth: Double? = null,
): Modifier =
    drawWithCache {
        val options = RoughOptions(seed = seed, roughness = theme.roughness, boil = theme.boil)
        val count = if (theme.boil == 0.0) 1 else BOIL_FRAMES
        val boxDp = Size(size.width / density, size.height / density)
        val generated: List<List<Path>> =
            layers.map { layer ->
                Rough
                    .variants({ o -> layer.generate(boxDp, o) }, options, count)
                    .map { it.toComposePath() }
            }

        onDrawBehind {
            scale(density, density, pivot = Offset.Zero) {
                layers.forEachIndexed { index, layer ->
                    if (!layer.visible()) return@forEachIndexed
                    val frames = generated[index]
                    drawLayer(
                        layer = layer,
                        path = frames[frame.value % frames.size],
                        theme = theme,
                        ink = ink,
                        lineWidth = lineWidth,
                    )
                }
            }
        }
    }

private fun DrawScope.drawLayer(
    layer: SketchLayer,
    path: Path,
    theme: DrawablyTheme,
    ink: Color?,
    lineWidth: Double?,
) {
    val color = ink ?: if (layer.role.usesFillColor) theme.fill else theme.stroke
    // the canvas is already scaled to pixels, so widths stay in the same
    // density-independent units the geometry uses
    val width = (layer.role.fixedLineWidth ?: lineWidth ?: theme.width.value.toDouble()).toFloat()
    val stroke =
        Stroke(
            width = width,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        )
    val trim = layer.trim()
    if (trim <= 0f) return

    withTransform({
        val travel = layer.offsetX()
        if (travel != 0f) translate(left = travel)
        val factor = layer.scale()
        if (factor != 1f) scale(factor, factor, pivot = center)
    }) {
        if (layer.role.isFilled) {
            drawPath(path, color, alpha = layer.role.opacity, style = Fill, blendMode = layer.role.blendMode)
        } else {
            layer.fill()?.let { drawPath(path, it, style = Fill) }
        }
        drawPath(
            path = if (trim >= 1f) path else path.trimmed(trim),
            color = color,
            alpha = layer.role.opacity,
            style = stroke,
            blendMode = layer.role.blendMode,
        )
    }
}

/**
 * The leading [fraction] of the path, so a tick can be drawn on rather than
 * faded in — upstream animates `stroke-dashoffset` for the same effect.
 */
private fun Path.trimmed(fraction: Float): Path {
    val measure = PathMeasure()
    measure.setPath(this, false)
    val destination = Path()
    measure.getSegment(0f, measure.length * fraction, destination, true)
    return destination
}
