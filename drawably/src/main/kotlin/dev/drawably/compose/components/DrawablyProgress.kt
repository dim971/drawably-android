package dev.drawably.compose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.drawably.compose.sketch.DrawablyGeometry
import dev.drawably.compose.sketch.SketchLayer
import dev.drawably.compose.sketch.SketchRole
import dev.drawably.compose.sketch.drawablySketch
import dev.drawably.compose.sketch.rememberDrawablySketchState
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/** How many boxes a track has when the call site does not say. */
public const val DRAWABLY_PROGRESS_DEFAULT_STEPS: Int = 10

/**
 * How many of a track's steps are drawn as done.
 *
 * Free, and pure, so the rounding is tested without composing anything: a
 * fraction that lands a hair under a step boundary, as `3f / 7f * 7f` does,
 * still fills three boxes rather than two.
 */
public fun drawablyProgressStepsDone(
    fraction: Float?,
    steps: Int,
): Int {
    if (steps <= 0 || fraction == null || !fraction.isFinite()) return 0
    return (min(max(fraction, 0f), 1f) * steps).roundToInt()
}

/**
 * A row of pen boxes, hatched one by one as the steps are completed.
 *
 * ```kotlin
 * DrawablyProgress(step = 3, total = 7)
 * ```
 *
 * Unlike the rest of the library this control has no upstream counterpart; see
 * `docs/components.md`.
 */
@Composable
public fun DrawablyProgress(
    step: Int,
    total: Int,
    modifier: Modifier = Modifier,
    seed: UInt? = null,
) {
    val steps = max(1, total)
    val done = min(max(0, step), steps)
    DrawablyProgress(
        progress = { done.toFloat() / steps.toFloat() },
        modifier = modifier,
        steps = steps,
        seed = seed,
    )
}

/**
 * The same track driven by a fraction rather than by a step count.
 *
 * [progress] is a lambda, the way `LinearProgressIndicator` takes one, so a
 * moving value redraws without regenerating the track's geometry.
 *
 * A fraction that is not a finite number draws an empty track. It says nothing
 * about how far along the work is, and the control does not guess.
 */
@Composable
public fun DrawablyProgress(
    progress: () -> Float,
    modifier: Modifier = Modifier,
    steps: Int = DRAWABLY_PROGRESS_DEFAULT_STEPS,
    seed: UInt? = null,
) {
    val count = max(1, steps)
    val state = rememberDrawablySketchState(seed)
    // Held in a state so the draw-phase lambdas below read the caller's latest
    // closure rather than the one captured on first composition.
    val currentProgress = rememberUpdatedState(progress)
    val layers =
        remember(count) {
            List(count) { index ->
                listOf(
                    SketchLayer(SketchRole.Outline) { size, o ->
                        DrawablyGeometry.progressSegment(size.width.toDouble(), size.height.toDouble(), o)
                    },
                    SketchLayer(
                        SketchRole.Scribble,
                        visible = {
                            index < drawablyProgressStepsDone(currentProgress.value(), count)
                        },
                    ) { size, o ->
                        DrawablyGeometry.progressScribble(size.width.toDouble(), size.height.toDouble(), o)
                    },
                )
            }
        }
    val done = drawablyProgressStepsDone(progress(), count)
    Row(
        modifier =
            modifier.semantics {
                progressBarRangeInfo =
                    ProgressBarRangeInfo(done.toFloat() / count.toFloat(), 0f..1f, count)
            },
        horizontalArrangement = Arrangement.spacedBy(DrawablyGeometry.PROGRESS_SEGMENT_GAP.dp),
    ) {
        layers.forEachIndexed { index, segment ->
            Box(
                modifier =
                    Modifier
                        // A cap, not a width. A track of twelve boxes at a
                        // fixed 34dp is wider than a phone, and the row it sits
                        // in was pushing its neighbours off the screen rather
                        // than wrapping. Boxes now share whatever the row has
                        // and never grow past the size they were drawn at, so a
                        // short track looks exactly as it did and a long one
                        // still fits.
                        .weight(1f, fill = false)
                        .widthIn(max = DrawablyGeometry.PROGRESS_SEGMENT_WIDTH.dp)
                        .fillMaxWidth()
                        .height(DrawablyGeometry.PROGRESS_SEGMENT_HEIGHT.dp)
                        // Each box runs off its own seed. Sharing one would draw
                        // the same rectangle seven times, which reads as a
                        // printed rule rather than as a hand.
                        .drawablySketch(
                            layers = segment,
                            seed = state.seed.value + index.toUInt(),
                            theme = state.theme,
                            frame = state.frame,
                        ),
            )
        }
    }
}
