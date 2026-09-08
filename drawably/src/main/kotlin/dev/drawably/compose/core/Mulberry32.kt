package dev.drawably.compose.core

/**
 * The seeded PRNG every sketch is drawn from.
 *
 * A direct port of upstream `src/prng.ts`. JavaScript's `Math.imul`, `>>>` and
 * `|` all work on 32-bit patterns, so this [UInt] arithmetic is bit-identical,
 * which is what lets the golden fixtures generated from the npm package pin
 * this port down exactly.
 */
public class Mulberry32(
    seed: UInt,
) {
    private var state: UInt = seed

    /** The next value in `[0, 1)`. */
    public fun next(): Double {
        state += 0x6d2b79f5u
        var t = state
        t = (t xor (t shr 15)) * (t or 1u)
        t = t xor (t + ((t xor (t shr 7)) * (t or 61u)))
        return (t xor (t shr 14)).toDouble() / 4294967296.0
    }
}

/** A fresh random seed, as upstream's `randomSeed()` produces. */
public fun randomSeed(): UInt =
    kotlin.random.Random
        .nextInt()
        .toUInt()
