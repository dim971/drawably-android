# Contributing

Thanks for taking a look. Issues and pull requests are both welcome.

## Getting set up

```sh
git clone https://github.com/dim971/drawably-android
cd drawably-android
./gradlew :drawably:testDebugUnitTest   # the engine goldens
./gradlew :showcase:installDebug        # the catalog app
```

The build declares its own daemon JVM criteria, so Gradle picks a JDK 21 itself,
so there is no `JAVA_HOME` to set. In Android Studio, open the project and pick the
**showcase** run configuration; it is checked in.

AGP is pinned to a version Android Studio can sync rather than the newest
release. Studio refuses to sync a project built with an AGP newer than it
supports, and a failed sync means no run configuration at all. Raise it when the
IDE does.

## Before you open a pull request

```sh
./gradlew :drawably:testDebugUnitTest
./gradlew ktlintCheck      # ktlintFormat fixes most of what it finds
./gradlew lint
./gradlew :showcase:assembleDebug
```

Three things the review will look for:

**No warnings.** Not from Kotlin, not from Android Lint. A warning that is
tolerated becomes a warning that is ignored.

**The goldens still pass.** If you touch anything under `core/` or
`sketch/DrawablyGeometry.kt`, the fixtures are the contract. See
[docs/fidelity.md](docs/fidelity.md). Changing them means changing what this
library claims to be, so say why in the pull request.

**Parity with iOS.** This library has a
[twin](https://github.com/dim971/drawably-ios). A change to shared behaviour
(geometry, theming, a control's states) should land in both, or say plainly why
it should not.

## Conventions

[docs/coding-style.md](docs/coding-style.md) is the full version: the
[Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
as they apply here, including their library rules, which explicit API mode
enforces. The short version:

- The library sits on Compose Foundation, not Material. Please keep it that way:
  it is what lets the library drop into any Compose app.
- Explicit API mode is on. Public declarations need explicit visibility and
  return types.
- Anything that moves is read in the **draw** phase, not during composition, so
  touching a control redraws it instead of regenerating its geometry.
- Geometry is generated in density-independent units, never pixels.
- Comments explain *why*, not *what*. If a constant looks arbitrary, say where
  it came from.

## Reporting a bug

A seed makes a sketch reproducible. If the report is about how something is
drawn, pass a pinned `seed` and include it: it turns "it looks wrong sometimes"
into something anyone can reproduce.

## Code of conduct

Taking part means following the [Code of Conduct](CODE_OF_CONDUCT.md).
