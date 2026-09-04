package dev.drawably.compose.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Every test here compares this port's output against strings produced by the
 * JavaScript library itself. A failure means the port has drifted, not that a
 * tolerance needs widening.
 */
internal class EngineGoldenTest {
    private val goldens = Goldens.shared

    @Test
    fun `mulberry32 produces the same stream`() {
        for (case in goldens.prng) {
            val rand = Mulberry32(case.seed.toUInt())
            val produced = List(case.values.size) { rand.next() }
            assertEquals("seed ${case.seed}", case.values, produced)
        }
    }

    @Test
    fun `shape primitives match`() {
        val mismatches =
            goldens.shapes.mapNotNull { case ->
                val actual = generate(case.fn, case.args, case.opts.rough).toSvgString()
                if (actual == case.d) {
                    null
                } else {
                    Mismatch(
                        "${case.fn}(${case.args}) seed ${case.opts.seed} roughness ${case.opts.roughness}",
                        case.d,
                        actual,
                    )
                }
            }
        assertTrue(report(mismatches), mismatches.isEmpty())
    }

    @Test
    fun `boil frames match`() {
        val mismatches =
            goldens.variants.flatMap { case ->
                val produced =
                    Rough.variants(
                        { generate(case.fn, case.args, it) },
                        case.opts.rough,
                        case.n,
                    )
                assertEquals(case.ds.size, produced.size)
                produced.mapIndexedNotNull { i, path ->
                    val actual = path.toSvgString()
                    if (actual == case.ds[i]) {
                        null
                    } else {
                        Mismatch(
                            "${case.fn} frame $i seed ${case.opts.seed} boil ${case.opts.boil}",
                            case.ds[i],
                            actual,
                        )
                    }
                }
            }
        assertTrue(report(mismatches), mismatches.isEmpty())
    }

    @Test
    fun `every control layer matches`() {
        val mismatches =
            goldens.controls.flatMap { case ->
                val produced =
                    Rough.variants(
                        { generateLayer(case.layer, case.w, case.h, it) },
                        case.opts.rough,
                        case.ds.size,
                    )
                produced.mapIndexedNotNull { i, path ->
                    val actual = path.toSvgString()
                    if (actual == case.ds[i]) {
                        null
                    } else {
                        Mismatch(
                            "${case.layer} ${case.w}×${case.h} frame $i roughness ${case.opts.roughness}",
                            case.ds[i],
                            actual,
                        )
                    }
                }
            }
        assertTrue(report(mismatches), mismatches.isEmpty())
    }

    @Test
    fun `arrow layer matches`() {
        val mismatches =
            goldens.arrows.flatMap { case ->
                val a = case.args
                val produced =
                    Rough.variants(
                        { Rough.arrow(a[0], a[1], a[2], a[3], it) },
                        case.opts.rough,
                        case.ds.size,
                    )
                produced.mapIndexedNotNull { i, path ->
                    val actual = path.toSvgString()
                    if (actual == case.ds[i]) {
                        null
                    } else {
                        Mismatch(
                            "arrow frame $i seed ${case.opts.seed} roughness ${case.opts.roughness}",
                            case.ds[i],
                            actual,
                        )
                    }
                }
            }
        assertTrue(report(mismatches), mismatches.isEmpty())
    }

    @Test
    fun `the fixtures cover the whole surface`() {
        assertEquals("drawably@0.3.10", goldens.upstream)
        assertTrue(goldens.shapes.size > 200)
        assertEquals(
            26,
            goldens.controls
                .map { it.layer }
                .toSet()
                .size,
        )
    }
}
