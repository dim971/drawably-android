package dev.drawably.compose.core

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * `jsToFixed2` only exists so the golden strings can be compared verbatim, so
 * the cases that matter are the ones where it must *not* behave like `%.2f`.
 */
internal class FormattingTest {
    @Test
    fun `rounds exact halves away from zero unlike printf`() {
        // "%.2f" would give 0.12 / 11.38 / 0.62 here — round-half-to-even
        assertEquals("0.13", jsToFixed2(0.125))
        assertEquals("0.38", jsToFixed2(0.375))
        assertEquals("0.63", jsToFixed2(0.625))
        assertEquals("11.38", jsToFixed2(11.375))
        assertEquals("-0.13", jsToFixed2(-0.125))
    }

    @Test
    fun `handles zero, tiny negatives and plain values`() {
        assertEquals("0.00", jsToFixed2(0.0))
        assertEquals("0.00", jsToFixed2(-0.0))
        assertEquals("-0.00", jsToFixed2(-0.001))
        assertEquals("0.01", jsToFixed2(0.005))
        assertEquals("7.69", jsToFixed2(7.6923076923076925))
        assertEquals("-12.34", jsToFixed2(-12.344))
        assertEquals("100.00", jsToFixed2(100.0))
    }
}
