# Coding style

The baseline is the [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html),
including their **Coding conventions for libraries** section, which this project
follows in full. Read those first; this document only records how they apply
here, and the few places this project deliberately does something else.

Formatting is not discussed below because it is not negotiated: ktlint decides
it, configured by `.editorconfig`, and `./gradlew ktlintCheck` is the arbiter.
`./gradlew ktlintFormat` fixes most of it for you.

## Library conventions, which are not optional here

The conventions list three extra rules for libraries. All three are enforced by
the compiler through explicit API mode (`-Xexplicit-api=strict`):

- **Always specify member visibility.** No accidental public API.
- **Always specify function return types and property types.** So a change to
  an implementation cannot silently change a signature.
- **KDoc on every public member.**

If explicit API mode complains, the answer is to write the type or the
visibility, not to relax the mode.

## Naming

### Composables carry class-style names

The conventions carve this out explicitly: a `@Composable` function returning
`Unit` follows the class-naming convention. So `DrawablyButton`, not
`drawablyButton`. ktlint knows, via `ktlint_function_naming_ignore_when_annotated_with`
in `.editorconfig`.

Modifier extensions are ordinary functions and stay camel case:
`Modifier.drawablyTilt()`, `Modifier.drawablySketch()`.

### Prefixing

Everything public is prefixed `Drawably` (`DrawablyButton`, `DrawablyTheme`,
`DrawablyGeometry`) because this library is designed to sit alongside whatever
design system an app already uses, and `Button` would collide.

`Rough`, `SketchPath`, `Subpath` and `Pt` are the exception. They are the
engine's own vocabulary, they mirror upstream's names, and prefixing them would
make the transcription harder to check against the original.

### Constants

Screaming snake case for `const val` and object `val`s holding immutable data,
as the conventions say:

```kotlin
public const val INSET: Double = 3.0
public const val POPUP_TAIL_WIDTH: Double = 18.0
```

### Names say what a thing does

Verbs for functions that act (`resketch`), nouns for values (`angle`,
`outlineReach`). Booleans read as assertions: `isFilled`, `usesFillColor`.

### Test names

Backticked sentences, as the conventions permit:

```kotlin
@Test
fun `a press reads stronger than a hover, and wins over it`() { … }
```

Say what should be true, not what the test does. `mulberry32 produces the same
stream` beats `testPrng`.

## Documentation

KDoc on every public member. Follow the conventions' advice and skip `@param` /
`@return` tags. Fold the description into the prose and link parameters with
brackets:

```kotlin
/**
 * The angle a seed produces, drawn from the same PRNG the sketches use so a
 * pinned seed gives a pinned lean.
 */
public fun angle(seed: UInt, maxDegrees: Float = DEFAULT_MAX_DEGREES): Float
```

## Comments explain why, not what

The single rule this codebase cares most about, because a lot of its constants
came from somewhere non-obvious and would otherwise look arbitrary:

```kotlin
/** At chevron scale, full roughness turns the V into noise. */
public const val CHEVRON_ROUGHNESS: Double = 0.4
```

```kotlin
// the label has to clear the sketched outline, which moves with the theme's
// stroke width and roughness
```

If a number looks arbitrary, say where it came from. If a line exists to work
around something, say what.

## Idiomatic Kotlin, as the conventions describe it

The ones that come up most in this codebase:

- **Immutability.** `val` unless it has to change; immutable collection types in
  signatures.
- **Expression bodies** for single-expression functions.
- **Default parameters** rather than overloads. Every control's `seed` is a
  defaulted parameter, not a second function.
- **Named arguments** for booleans and same-typed parameters:
  `Rough.line(x, y, x + width, y, options)` is fine; `drawablyTilt(seed = 3u)`
  is clearer than a bare `3u`.
- **`if` for two branches, `when` for three or more.**

## Rules particular to this project

**Foundation, not Material.** The library depends on Compose Foundation only.
That is what lets it drop into any Compose app whatever design system it
already uses, and it is why label colours travel through
`LocalDrawablyContentColor` rather than Material's `LocalContentColor`.

**Geometry is generated in density-independent units.** Never pixels. Roughness
is an absolute amplitude in the engine, so generating against a pixel size makes
the jitter three times finer on a 3x screen: the tests still pass and the
controls come out looking like clean rectangles.

**Anything that moves is read in the draw phase.** The boil frame, a layer's
trim, offset, scale, visibility and fill are lambdas or `State`, read inside the
draw lambda. Reading them during composition puts them in `drawWithCache`'s key
and regenerates the geometry every time a finger touches a control.

**The engine is a transcription, not an interpretation.** Anything under `core/`
mirrors upstream's structure and operation order, including things that would
otherwise be refactored away. The golden fixtures enforce this. See
[fidelity.md](fidelity.md).

**The sketch carries no semantics.** Every control wraps a real Foundation
control. If a change makes the sketch itself interactive, it is the wrong
change.

## What is enforced mechanically

| Rule | By |
| --- | --- |
| Formatting, naming, file names, import order, trailing commas | `ktlint` (`.editorconfig`) |
| Explicit visibility, explicit return types | the compiler (`-Xexplicit-api=strict`) |
| Android correctness and API level use | `./gradlew lint` |
| Byte-identical engine output | `./gradlew :drawably:testDebugUnitTest` |
| Zero warnings | CI |

Everything else in this document is a review conversation.
