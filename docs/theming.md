# Theming

## The theme

`DrawablyTheme` mirrors upstream's CSS custom properties one for one, defaults
included, and travels down the tree the way they cascade.

| Property | Type | Default | What it does |
| --- | --- | --- | --- |
| `stroke` | `Color` | `#2724d1` | Line colour for outlines, ticks, chevrons, markers |
| `fill` | `Color` | `#2724d1` | Filled layers: a solid button's blob, a radio's dot, a switch's knob, a highlight's wash |
| `paper` | `Color` | `White` | What the ink sits on; a solid button's label is drawn in it, and a select's list uses it as its background |
| `width` | `Dp` | `2.dp` | Stroke width for ordinary layers. Blobs and knobs are always 4, scribbles and focus rings 1.5, a highlight's wash 6 |
| `error` | `Color` | `#d12724` | The `Danger` tone and the `Error` state |
| `success` | `Color` | `#188a42` | The `Success` state |
| `roughness` | `Double` | `1.0` | Multiplies the jitter amplitude of the base sketch |
| `boil` | `Double` | `0.3` | Per-frame flicker amplitude. `0` renders one still frame |
| `minimumControlHeight` | `Dp` | `48.dp` | The smallest a control is drawn, so a finger can hit it. `0.dp` gives the web library's own proportions |

## Applying it

```kotlin
DrawablyTheme(DrawablyTheme(stroke = Color.Black, fill = Color.Black)) {
    App()
}
```

Because it is a `CompositionLocal`, a subtree can differ from its parent:

```kotlin
Column {
    DrawablyButton("Normal", {})
    DrawablyTheme(LocalDrawablyTheme.current.copy(width = 4.dp)) {
        DrawablyButton("Heavier", {})
    }
}
```

## Colours

```kotlin
DrawablyPenBlue   // #2724d1, upstream's ink
DrawablyError     // #d12724
DrawablySuccess   // #188a42
DrawablyNeutral   // #6e675f, the warm grey of the neutral tone
```

## Seeds

A control without a `seed` picks a fresh one on first composition, and rolls
another whenever it is pressed or hovered. That is the library's whole idea: the
sketch is redrawn, not reused.

Pass a `seed` to pin it: the same seed always produces the same drawing, on
this platform and on iOS:

```kotlin
DrawablyButton("Done", {}, seed = 42u)
```

## Units

Geometry is generated in **density-independent units** and the canvas is scaled
to pixels around it. Roughness is an absolute amplitude in the engine, so
generating against a pixel size would make the jitter three times finer on a 3x
screen than on the web: the controls come out looking like clean rectangles.
Anything you draw yourself should do the same.

## Turning the movement off

Setting `boil` to `0` renders one still frame instead of three, and skips the
ticker entirely. Zeroing the system's animator duration scale does the same, and
additionally stops the press and hover re-sketch, matching what upstream does
under `prefers-reduced-motion`.

## Drawing your own shapes

The engine is public:

```kotlin
val options = RoughOptions(seed = 42u, roughness = 1.0, boil = 0.0)
val path = Rough.roundedRect(3.0, 3.0, 114.0, 30.0, 8.0, options).toComposePath()
```

Available: `Rough.line`, `roundedRect`, `circle`, `ellipse`, `arrow`,
`checkmark`, `scribbleFill`, and `Rough.variants` to get the boil frames of any
of them.

`DrawablyGeometry` holds the per-control geometry (`buttonOutline`,
`checkboxCheck`, `toggleKnob` and the rest) if you want a shape that matches an
existing control exactly. `DrawablyGeometry.outlineReach(width, roughness)`
tells you how far inside its box a sketched outline can reach, which is what to
allow if you are putting your own content inside one.
