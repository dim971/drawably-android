package dev.drawably.showcase

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import dev.drawably.compose.theme.DrawablyTheme

/**
 * The live theme every screen draws with, plus the "draw it all again" button.
 *
 * Sliding roughness or boil re-renders the whole catalog, which doubles as the
 * manual test that theming actually reaches every control.
 */
@Stable
class ShowcaseSettings {
    var theme by mutableStateOf(DrawablyTheme())

    /**
     * Bumping this re-keys the content, so every control picks a fresh seed:
     * the closest thing to upstream's `resketch()` across a whole page.
     */
    var resketchToken by mutableIntStateOf(0)
        private set

    fun resketch() {
        resketchToken++
    }

    fun reset() {
        theme = DrawablyTheme()
        resketch()
    }
}

val LocalShowcaseSettings = staticCompositionLocalOf { ShowcaseSettings() }
