# Drawably for Jetpack Compose — working notes

A Compose port of the web library [Drawably](https://www.drawably.dev)
(MIT, © 2026 Daniel Belyi). Its twin is
[drawably-ios](https://github.com/dim971/drawably-ios); the two are kept in step
deliberately.

## The one rule that matters

**The engine's output is pinned to the JavaScript original, byte for byte.**

`drawably/src/test/resources/goldens.json` is generated from the published npm
package. The tests rebuild each case through the Kotlin engine and compare
strings. There is no tolerance and there is not meant to be one.

If a golden fails, the port has drifted — find out why. Do not widen anything,
do not regenerate the fixtures to make a failure go away. Regenerating is only
correct when deliberately tracking a new upstream version.

Three JVM/JavaScript differences already cost time to find, all documented in
`docs/fidelity.md` and handled in `core/JsMath.kt` and `core/SvgPath.kt`:

- `java.lang.Math`'s `cos`/`sin`/`atan2` differ from V8's by an ulp; V8 uses
  fdlibm and so does `StrictMath`. Sample counts come from
  `ceil(length / step)`, so one ulp can add a point and desynchronise every
  later PRNG draw. The symptom is a wrong-looking *shape*, not a wrong number.
- `Math.hypot` differs the same way; V8's algorithm is reproduced.
- `toFixed(2)` rounds halves away from zero where `"%.2f"` rounds to even.

## Two traps that are not about fidelity

**Units.** Geometry is generated in density-independent units and the canvas
scaled to pixels around it. Roughness is an absolute amplitude, so generating
against a pixel size makes the jitter three times finer on a 3x screen — the
goldens still pass and the controls look like clean rectangles.

**Draw phase, not composition.** The frame index, a layer's trim, offset, scale,
visibility and fill are all lambdas or `State`, read inside the draw lambda.
Reading them during composition puts them in `drawWithCache`'s key and
regenerates the geometry every time a finger touches a control.

## Layout

```
drawably/src/main/kotlin/dev/drawably/compose/
  core/         pure maths — PRNG, sampling, jitter, shapes, JsMath. No Compose.
  theme/        DrawablyTheme, its CompositionLocal, the content colour
  sketch/       layer geometry, roles, the renderer, seeds, tilt
  components/   the fifteen controls
showcase/       the catalog app
Tools/          gen-goldens.mjs, gen-icon.mjs — Node, run by hand
```

`docs/architecture.md` explains how a sketch reaches the screen.

## Conventions worth keeping

- **Foundation, not Material.** The library must stay droppable into any Compose
  app. It publishes label colours through `LocalDrawablyContentColor` precisely
  because it cannot set Material's.
- **Zero warnings**, Kotlin and Android Lint.
- **The sketch carries no semantics.** Every control wraps a real Foundation
  control so TalkBack and focus keep working.
- **AGP is pinned to what Android Studio can sync**, not the newest release. A
  failed sync means no run configuration at all, and the showcase becomes
  unlaunchable from the IDE.
- Explicit API mode is on.
- Comments explain why, not what.

## Verifying

```sh
./gradlew :drawably:testDebugUnitTest      # goldens
./gradlew lint
./gradlew :showcase:installDebug
adb shell am start -n dev.drawably.showcase/.MainActivity
adb exec-out screencap -p > out.png
```

`adb shell input tap x y` and `input swipe x y x y <ms>` both work for driving
the app, including holding a press long enough to photograph a pressed state.

No `JAVA_HOME` is needed: the build declares its own daemon JVM criteria.

## Staying in step with iOS

A change to shared behaviour — geometry, theming, a control's states, a new
modifier — should land in both repositories. The Swift port mirrors this
structure and both replay the same `goldens.json`.
