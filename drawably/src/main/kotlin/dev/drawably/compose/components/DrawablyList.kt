package dev.drawably.compose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.drawably.compose.sketch.DrawablyGeometry
import dev.drawably.compose.sketch.SketchLayer
import dev.drawably.compose.sketch.SketchRole
import dev.drawably.compose.sketch.drawablySketch
import dev.drawably.compose.sketch.rememberDrawablySketchState

/** What a list draws in its gutter. */
public enum class DrawablyListMarker {
    /** A short pen dash. */
    Dash,

    /** A pen tick. */
    Check,
}

/** The gutter the markers are drawn into. */
public val DrawablyListGutter: Dp = 24.dp

/**
 * A list whose bullets are drawn by hand in the gutter.
 *
 * ```kotlin
 * DrawablyList(steps, marker = DrawablyListMarker.Check) { step ->
 *     Text(step)
 * }
 * ```
 */
@Composable
public fun <T> DrawablyList(
    items: List<T>,
    modifier: Modifier = Modifier,
    marker: DrawablyListMarker = DrawablyListMarker.Dash,
    spacing: Dp = 6.dp,
    seed: UInt? = null,
    itemContent: @Composable (T) -> Unit,
) {
    val base = rememberDrawablySketchState(seed)
    Column(
        verticalArrangement = Arrangement.spacedBy(spacing),
        modifier = modifier.padding(start = DrawablyListGutter),
    ) {
        items.forEachIndexed { index, item ->
            val rowState = rememberDrawablySketchState(base.seed.value + index.toUInt())
            val layers =
                remember(marker) {
                    listOf(
                        SketchLayer(SketchRole.Marker) { size, o ->
                            // the marker is drawn to the left of the row's own box,
                            // so it lands in the list's leading padding
                            val height = size.height.toDouble()
                            when (marker) {
                                DrawablyListMarker.Dash ->
                                    DrawablyGeometry.listDash(
                                        size.width.toDouble(),
                                        height,
                                        o,
                                        lineHeight = height,
                                    )

                                DrawablyListMarker.Check ->
                                    DrawablyGeometry.listCheck(
                                        size.width.toDouble(),
                                        height,
                                        o,
                                        lineHeight = height,
                                    )
                            }
                        },
                    )
                }
            Box(
                Modifier
                    .fillMaxWidth()
                    .drawablySketch(rowState, layers),
            ) {
                itemContent(item)
            }
        }
    }
}
