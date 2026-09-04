# Architecture

How a sketch gets from the engine to the screen.

```
drawably/src/main/kotlin/dev/drawably/compose/
  core/          pure maths — PRNG, sampling, jitter, shapes, JsMath. No UI types.
  theme/         DrawablyTheme, its CompositionLocal, the content colour
  sketch/        layer geometry, roles, the renderer, seeds, tilt
  components/    the fifteen controls
```

## The engine

`Rough` is a direct port of upstream's `rough.ts`. Every shape is a list of
sampled points, jittered twice from one PRNG stream — the second pass 1.4× wider
than the first — and smoothed with a quadratic through the midpoint of each
segment. That double stroke is what reads as a pen going over a line.

Nothing in `core` knows about Compose. It produces `SketchPath`, a list of
strokes; `SketchPath.toComposePath()` turns that into a `Path`.

`core/JsMath.kt` exists because the JVM's answers differ from JavaScript's in
ways that matter here — see [fidelity.md](fidelity.md).

## Layers

A control is a stack of layers, each a shape plus a role. `SketchRole` says how
a layer is painted — which of the theme's two inks it takes, whether it is
filled, what stroke width overrides the theme, whether it blends. The entries
map one for one onto the path classes in upstream's stylesheet.

`DrawablyGeometry` holds the shapes themselves, ported from `controls.ts`. These
are public — a shape matching an existing control exactly is sometimes what you
want.

## Modifier.drawablySketch

The renderer. It generates the boil frames inside `drawWithCache`'s cache block —
once per box size, seed and options — and the draw pass only picks which one to
stroke.

Everything that moves is read in the **draw** phase rather than during
composition: the frame index, a layer's trim, offset, scale, visibility and
fill are all lambdas or `State`. That is why a tick being drawn on, a knob
sliding or a focus ring appearing redraws the control instead of throwing away
its geometry and regenerating it.

Geometry is generated in density-independent units and the canvas scaled to
pixels around it. Roughness is an absolute amplitude, so generating against a
pixel size would make the jitter three times finer on a 3x screen than on the
web.

Upstream animates by stepping a registered CSS custom property through 0, 1, 2.
There is no equivalent here, so a coroutine ticker drives the index, and a
zeroed animator duration scale collapses it to a single still frame.

## The controls

Each control is a thin composition over `Modifier.drawablySketch` and a real
Foundation control — `toggleable`, `selectable`, `BasicTextField`,
`clickable` — so TalkBack, focus and keyboard keep working exactly as they would
without the sketch. That mirrors upstream, where the SVG is `aria-hidden` and
the real `<input>` stays in the DOM.

The library deliberately does not depend on Material. Since it cannot set
Material's `LocalContentColor`, it publishes a control's label colour through
`LocalDrawablyContentColor` instead.

## Where the port stops

The optional "Drawably Pen" TrueType font, the React wrappers, and the
Chromium-only customisable-`<select>` chrome are out of scope. The tilt is the
one thing here that upstream does not have: on drawably.dev the scatter is the
demo page's own CSS.
