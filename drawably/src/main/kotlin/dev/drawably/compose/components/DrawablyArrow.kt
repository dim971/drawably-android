package dev.drawably.compose.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import dev.drawably.compose.core.Rough
import dev.drawably.compose.core.RoughOptions
import dev.drawably.compose.core.arrow
import dev.drawably.compose.sketch.DrawablyGeometry
import dev.drawably.compose.sketch.rememberDrawablySketchState
import dev.drawably.compose.sketch.toComposePath
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

/** One arrow, from one named anchor to another. */
public data class DrawablyArrow(
    val from: Any,
    val to: Any,
    val seed: UInt? = null,
)

/** Lets content inside a [DrawablyArrowLayer] name itself as an anchor. */
@Stable
public class DrawablyArrowScope internal constructor() {
    internal val anchors = mutableStateMapOf<Any, Rect>()
    internal var layer: LayoutCoordinates? by mutableStateOf(null)

    /** Names this composable so the enclosing layer can point at it. */
    public fun Modifier.drawablyAnchor(id: Any): Modifier =
        onGloballyPositioned { coordinates ->
            val root = layer ?: return@onGloballyPositioned
            if (root.isAttached && coordinates.isAttached) {
                anchors[id] = root.localBoundingBoxOf(coordinates)
            }
        }
}

/**
 * Draws hand-sketched arrows between anchored composables inside it.
 *
 * ```kotlin
 * DrawablyArrowLayer(arrows = listOf(DrawablyArrow("hint", "submit"))) {
 *     Text("start here", Modifier.drawablyAnchor("hint"))
 *     DrawablyButton("Send", onClick = {}, modifier = Modifier.drawablyAnchor("submit"))
 * }
 * ```
 *
 * Upstream parks its arrows on `<body>` in document coordinates, which drift
 * when an anchor scrolls. Resolving anchors against this layer instead means
 * they stay put.
 */
@Composable
public fun DrawablyArrowLayer(
    arrows: List<DrawablyArrow>,
    modifier: Modifier = Modifier,
    content: @Composable DrawablyArrowScope.() -> Unit,
) {
    val scope = remember { DrawablyArrowScope() }
    val state = rememberDrawablySketchState()

    Box(
        modifier =
            modifier
                .onGloballyPositioned { scope.layer = it }
                .drawWithCache {
                    // anchors arrive in pixels; the sketch is generated in the
                    // density-independent units roughness is expressed in
                    val plans =
                        arrows.mapIndexedNotNull { index, arrow ->
                            val from =
                                scope.anchors[arrow.from]?.dividedBy(density)
                                    ?: return@mapIndexedNotNull null
                            val to =
                                scope.anchors[arrow.to]?.dividedBy(density)
                                    ?: return@mapIndexedNotNull null
                            val plan = planArrow(from, to, DrawablyGeometry.ARROW_GAP)
                            val options =
                                RoughOptions(
                                    seed = (arrow.seed ?: state.seed.value) + index.toUInt(),
                                    roughness = state.theme.roughness,
                                    boil = state.theme.boil,
                                )
                            plan to
                                Rough
                                    .variants(
                                        { o -> Rough.arrow(plan.x1, plan.y1, plan.x2, plan.y2, o) },
                                        options,
                                        if (state.theme.boil == 0.0) 1 else 3,
                                    ).map { it.toComposePath() }
                        }
                    val stroke =
                        Stroke(
                            width = state.theme.width.value,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round,
                        )
                    onDrawWithContent {
                        drawContent()
                        scale(density, density, pivot = Offset.Zero) {
                            for ((plan, paths) in plans) {
                                translate(left = plan.left, top = plan.top) {
                                    drawPath(
                                        path = paths[state.frame.value % paths.size],
                                        color = state.theme.stroke,
                                        style = stroke,
                                    )
                                }
                            }
                        }
                    }
                },
    ) {
        scope.content()
    }
}

/**
 * Where an arrow starts and ends: centre to centre, pulled back to each box's
 * edge plus a little clearance so it does not touch what it points at.
 */
internal class ArrowPlan(
    val left: Float,
    val top: Float,
    val x1: Double,
    val y1: Double,
    val x2: Double,
    val y2: Double,
)

private fun Rect.dividedBy(density: Float): Rect =
    Rect(left / density, top / density, right / density, bottom / density)

internal fun planArrow(
    from: Rect,
    to: Rect,
    gap: Double,
): ArrowPlan {
    val box =
        Rect(
            left = min(from.left, to.left),
            top = min(from.top, to.top),
            right = max(from.right, to.right),
            bottom = max(from.bottom, to.bottom),
        )
    val startX = (from.center.x - box.left).toDouble()
    val startY = (from.center.y - box.top).toDouble()
    val endX = (to.center.x - box.left).toDouble()
    val endY = (to.center.y - box.top).toDouble()
    val dx = endX - startX
    val dy = endY - startY
    val length = max(hypot(dx, dy), 1.0)
    val ux = dx / length
    val uy = dy / length

    // How far from a box's centre its edge is, along the connecting line.
    // A zero-size box gives 0/0 and means "no inset".
    fun exit(rect: Rect): Double {
        val candidates =
            listOf(
                rect.width / 2.0 / abs(ux),
                rect.height / 2.0 / abs(uy),
            ).filter { !it.isNaN() && it != 0.0 }
        return candidates.minOrNull() ?: 0.0
    }

    val head = min(exit(from) + gap, length / 2)
    val tail = min(exit(to) + gap, length / 2)
    return ArrowPlan(
        left = box.left,
        top = box.top,
        x1 = startX + ux * head,
        y1 = startY + uy * head,
        x2 = endX - ux * tail,
        y2 = endY - uy * tail,
    )
}
