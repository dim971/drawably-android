# Fidelity

This is a port, not a re-interpretation. The claim is specific: **the ported
engine emits byte-identical path data to the JavaScript original**, and it is
tested rather than asserted.

## How it is checked

`Tools/gen-goldens.mjs` drives the published npm package (`drawably@0.3.10`) and
records its SVG output:

- the first 20 values of `mulberry32` for six seeds, including `0xffffffff`
- every shape primitive across a matrix of seeds, roughness and argument sets
- the boil frames `variants()` produces
- all 26 per-control layers at representative sizes
- the arrow layer, whose two wings share a second PRNG stream

The result is committed as `drawably/src/test/resources/goldens.json`. The unit
tests rebuild every case through the Kotlin engine, serialise it with a debug
`toSvgString()` that matches upstream's formatting exactly, and compare strings.

```sh
./gradlew :drawably:testDebugUnitTest
```

A failure means the port has drifted. It never means a tolerance needs
widening — there is no tolerance.

## Regenerating

```sh
cd Tools
npm i drawably@0.3.10
node gen-goldens.mjs > ../drawably/src/test/resources/goldens.json
```

Do this when tracking a new upstream version, and expect to explain any diff.

## What it took

Three reconciliations, all in `core/JsMath.kt` and `core/SvgPath.kt`.

**Trigonometry.** V8 implements `cos`, `sin` and `atan2` with fdlibm, and so
does `StrictMath`; `java.lang.Math` uses intrinsics that differ in the last ulp.
That is invisible in a rounded coordinate but decides `ceil(length / step)` —
and an arrow head is exactly 12 long, sampled every 4. One extra sample point
shifts every later PRNG draw and changes the rest of the shape. This one cost
real time to find, because the symptom was a wrong-looking arrow, not a wrong
number.

**`Math.hypot`.** V8 scales by the larger component and takes the square root
before multiplying back. `java.lang.Math.hypot` is more accurate, which here
means different.

**Number formatting.** `Number.prototype.toFixed(2)` rounds exact halves away
from zero; `"%.2f"` rounds them to even. `0.125` is `0.13` upstream and would be
`0.12` here — and any coordinate that is a multiple of an eighth lands on one.

## A related trap, not about fidelity

Geometry is generated in density-independent units and the canvas scaled to
pixels around it. Roughness is an absolute amplitude, so generating against a
pixel size makes the jitter three times finer on a 3x screen than on the web:
the goldens still pass, but the controls come out looking like clean rectangles.

## Cross-platform

[drawably-ios](https://github.com/dim971/drawably-ios) replays the same
`goldens.json`. Both ports being pinned to the same strings is what makes a
given seed produce the same drawing on Android, on iOS and on the web.
