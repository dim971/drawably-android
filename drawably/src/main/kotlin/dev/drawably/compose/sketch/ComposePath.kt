package dev.drawably.compose.sketch

import androidx.compose.ui.graphics.Path
import dev.drawably.compose.core.SketchPath

/**
 * Turns the engine's jittered polylines into a drawable [Path], using the same
 * midpoint smoothing the SVG output describes: a quadratic through each
 * interior point, landing on the midpoint of the next segment.
 */
public fun SketchPath.toComposePath(): Path {
    val path = Path()
    for (subpath in subpaths) {
        val points = subpath.points
        val first = points.firstOrNull() ?: continue
        path.moveTo(first.x.toFloat(), first.y.toFloat())
        for (i in 1 until points.size - 1) {
            val c = points[i]
            val next = points[i + 1]
            path.quadraticTo(
                c.x.toFloat(),
                c.y.toFloat(),
                ((c.x + next.x) / 2).toFloat(),
                ((c.y + next.y) / 2).toFloat(),
            )
        }
        val last = points.last()
        path.lineTo(last.x.toFloat(), last.y.toFloat())
        if (subpath.closed) path.close()
    }
    return path
}
