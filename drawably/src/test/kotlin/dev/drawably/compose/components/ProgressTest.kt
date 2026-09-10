package dev.drawably.compose.components

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * How many boxes a track hatches is a pure function of the fraction and the
 * step count, so it is checked here rather than through a rendered view.
 */
internal class ProgressTest {
    private fun done(
        fraction: Float?,
        steps: Int,
    ): Int = drawablyProgressStepsDone(fraction, steps)

    @Test
    fun `the ends are exact`() {
        assertEquals(0, done(0f, 7))
        assertEquals(7, done(1f, 7))
    }

    @Test
    fun `a step boundary that floating point lands just under still fills`() {
        // 3f / 7f * 7f is 2.9999998, so truncation would show two boxes for a
        // learner standing on step three.
        for (total in 1..20) {
            for (step in 0..total) {
                assertEquals(step, done(step.toFloat() / total.toFloat(), total))
            }
        }
    }

    @Test
    fun `an indeterminate progress has nothing to fill`() {
        assertEquals(0, done(null, 7))
    }

    @Test
    fun `a fraction outside zero to one is clamped rather than overflowing`() {
        assertEquals(7, done(1.4f, 7))
        assertEquals(0, done(-0.2f, 7))
    }

    @Test
    fun `a fraction that is not a finite number carries no information`() {
        assertEquals(0, done(Float.NaN, 7))
        assertEquals(0, done(Float.POSITIVE_INFINITY, 7))
        assertEquals(0, done(Float.NEGATIVE_INFINITY, 7))
    }

    @Test
    fun `a track with no steps has nothing to fill`() {
        assertEquals(0, done(0.5f, 0))
    }
}
