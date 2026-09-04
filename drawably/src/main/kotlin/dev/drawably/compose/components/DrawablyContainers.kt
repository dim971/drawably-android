package dev.drawably.compose.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.drawably.compose.sketch.DrawablyGeometry
import dev.drawably.compose.sketch.SketchLayer
import dev.drawably.compose.sketch.SketchRole
import dev.drawably.compose.sketch.drawablySketch
import dev.drawably.compose.sketch.rememberDrawablySketchState

/**
 * A sketched box to group content in.
 *
 * ```kotlin
 * DrawablyCard {
 *     Text("npm i drawably")
 * }
 * ```
 */
@Composable
public fun DrawablyCard(
    modifier: Modifier = Modifier,
    seed: UInt? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val state = rememberDrawablySketchState(seed)
    val layers = remember {
        listOf(
            SketchLayer(SketchRole.Outline) { size, o ->
                DrawablyGeometry.cardOutline(size.width.toDouble(), size.height.toDouble(), o)
            },
        )
    }
    Box(
        modifier = modifier
            .drawablySketch(state, layers)
            .padding(16.dp),
        content = content,
    )
}

/** A pen line across the available width. */
@Composable
public fun DrawablyDivider(
    modifier: Modifier = Modifier,
    seed: UInt? = null,
) {
    val state = rememberDrawablySketchState(seed)
    val layers = remember {
        listOf(
            SketchLayer(SketchRole.Outline) { size, o ->
                DrawablyGeometry.dividerOutline(size.width.toDouble(), size.height.toDouble(), o)
            },
        )
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(10.dp)
            .drawablySketch(state, layers),
    )
}

/** How a badge's box is filled in. */
public enum class DrawablyBadgeVariant {
    Outline,
    Scribble,
}

/**
 * A small sharp-cornered tag, drawn round a monospaced label.
 *
 * ```kotlin
 * DrawablyBadge("v0.3.10")
 * ```
 */
@Composable
public fun DrawablyBadge(
    modifier: Modifier = Modifier,
    variant: DrawablyBadgeVariant = DrawablyBadgeVariant.Outline,
    seed: UInt? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val state = rememberDrawablySketchState(seed)
    val layers = remember(variant) {
        buildList {
            if (variant == DrawablyBadgeVariant.Scribble) {
                add(SketchLayer(SketchRole.Scribble) { size, o ->
                    DrawablyGeometry.badgeScribble(size.width.toDouble(), size.height.toDouble(), o)
                })
            }
            add(SketchLayer(SketchRole.Outline) { size, o ->
                DrawablyGeometry.badgeOutline(size.width.toDouble(), size.height.toDouble(), o)
            })
        }
    }
    Box(
        modifier = modifier
            .drawablySketch(state, layers)
            // the label has to clear the sketched outline, which moves with
            // the theme's stroke width and roughness
            .padding(DrawablyGeometry.badgePadding(state.theme.width, state.theme.roughness)),
        content = content,
    )
}
