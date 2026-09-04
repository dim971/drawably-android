package dev.drawably.compose.sketch

import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.drawably.compose.core.randomSeed

/**
 * The seed a control is drawn from.
 *
 * A pinned seed never changes, which is what previews and tests want; an
 * unpinned one is rolled again whenever the control is touched.
 */
@Stable
public class DrawablySeed internal constructor(initial: UInt, private val pinned: UInt?) {
    public var value: UInt by mutableStateOf(initial)
        private set

    public fun resketch() {
        if (pinned == null) value = randomSeed()
    }
}

@Composable
public fun rememberDrawablySeed(pinned: UInt? = null): DrawablySeed =
    remember(pinned) { DrawablySeed(pinned ?: randomSeed(), pinned) }

/**
 * Draws the control again from scratch when a pointer presses or arrives.
 *
 * Upstream does not even attach its listeners when the device asks for reduced
 * motion, so neither do we.
 */
@Composable
public fun DrawablySeed.ResketchOnInteraction(
    interactionSource: InteractionSource,
    enabled: Boolean = true,
) {
    LaunchedEffect(interactionSource, enabled) {
        if (!enabled) return@LaunchedEffect
        interactionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Press || interaction is HoverInteraction.Enter) {
                resketch()
            }
        }
    }
}
