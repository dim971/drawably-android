package dev.drawably.compose.core

import dev.drawably.compose.sketch.DrawablyTilt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

/**
 * The lean is a pure function of its seed, so it is checked here rather than by
 * measuring pixels on an emulator.
 */
internal class TiltTest {
    @Test
    fun `a seed always gives the same lean`() {
        assertEquals(DrawablyTilt.angle(42u), DrawablyTilt.angle(42u), 0f)
        assertNotEquals(DrawablyTilt.angle(42u), DrawablyTilt.angle(43u))
    }

    @Test
    fun `it leans both ways, and never further than asked`() {
        val angles = (0 until 500).map { DrawablyTilt.angle(it.toUInt(), maxDegrees = 2f) }
        assertTrue(angles.all { abs(it) <= 2f })
        assertTrue(angles.any { it < -0.5f })
        assertTrue(angles.any { it > 0.5f })
    }
}
