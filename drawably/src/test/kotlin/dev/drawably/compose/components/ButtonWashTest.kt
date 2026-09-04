package dev.drawably.compose.components

import androidx.compose.ui.graphics.Color
import dev.drawably.compose.theme.DrawablyPenBlue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * The press wash is a pure function of the button's state, so it is checked
 * here rather than through a synthetic touch on an emulator.
 */
internal class ButtonWashTest {
    private fun wash(
        variant: DrawablyButtonVariant = DrawablyButtonVariant.Outline,
        enabled: Boolean = true,
        pressed: Boolean = false,
        hovered: Boolean = false,
    ): Color? = drawablyButtonWash(DrawablyPenBlue, variant, enabled, pressed, hovered)

    @Test
    fun `a press washes the inside with the ink the border is drawn in`() {
        assertEquals(DrawablyPenBlue.copy(alpha = DrawablyButtonWash.PRESSED), wash(pressed = true))
        assertNotNull(wash(variant = DrawablyButtonVariant.Scribble, pressed = true))
    }

    @Test
    fun `a press reads stronger than a hover, and wins over it`() {
        assert(DrawablyButtonWash.PRESSED > DrawablyButtonWash.HOVER)
        assertEquals(DrawablyPenBlue.copy(alpha = DrawablyButtonWash.HOVER), wash(hovered = true))
        assertEquals(
            DrawablyPenBlue.copy(alpha = DrawablyButtonWash.PRESSED),
            wash(pressed = true, hovered = true),
        )
    }

    @Test
    fun `at rest, disabled, or solid there is no wash`() {
        assertNull(wash())
        assertNull(wash(enabled = false, pressed = true))
        // a solid button is already filled with ink
        assertNull(wash(variant = DrawablyButtonVariant.Solid, pressed = true))
    }
}
