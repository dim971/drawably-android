package dev.drawably.compose.core

/** The knobs the stroke engine takes, mirroring upstream's `RoughOptions`. */
public data class RoughOptions(
    /** Base seed. The same seed always yields the same sketch. */
    val seed: UInt,
    /** Multiplies the jitter amplitude of the base sketch. Upstream default `1`. */
    val roughness: Double = 1.0,
    /** Amplitude, in pixels, of the per-frame flicker. `0` disables boiling. */
    val boil: Double = 0.0,
    /**
     * Seed for the boil pass. `null` means "no boil pass", which is what the raw
     * shape generators see; [Rough.variants] is what fills it in.
     */
    val boilSeed: UInt? = null,
)
