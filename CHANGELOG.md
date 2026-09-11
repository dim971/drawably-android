# Changelog

All notable changes to this project are documented here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project
adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.2.1] - 2026-09-11

### Fixed

- `DrawablyProgress` no longer overflows its row. Each box was a fixed 34dp
  wide, so a track of twelve steps measured wider than a phone and pushed
  whatever sat beside it off the screen. The width is now a cap: boxes share the
  row and shrink when there are more of them than fit. A track with room to
  spare is unchanged. The iOS port carries the same fix, under the same tag.

## [0.2.0] - 2026-09-10

### Added

- `DrawablyProgress`: a row of pen boxes, hatched one by one as steps complete.
  This is the first control here with no upstream counterpart, so it carries no
  golden fixture; the rest of the library remains a byte-identical
  transcription. It is built from the public engine and the existing layer
  conventions, and it carries the platform's own progress semantics.
  A lambda overload takes a fraction, the way `LinearProgressIndicator` does.

## [0.1.0] - 2026-09-08

First release. A Jetpack Compose port of [Drawably](https://www.drawably.dev)
0.3.10.

### Added

- The stroke engine, ported from upstream's `prng.ts` and `rough.ts` and pinned
  to byte-identical output by fixtures generated from the published npm package.
- All fifteen upstream controls: button, card, checkbox, radio button, switch,
  text field, text area, select, divider, badge, list, underline, highlight,
  circle, arrow.
- `DrawablyTheme`, carrying upstream's CSS custom properties through a
  `CompositionLocal`.
- Boiling on a ticker, stopped when the system's animator duration scale is
  zero.
- A showcase catalog app with live previews, per-component screens and pen
  controls.

### Changed from upstream

- A pressed button washes its inside with 18% ink. Upstream only does this on
  hover, at 10%, which never happens on a touch device.
- The select's list carries a drawn tail and is positioned below the field by a
  `PopupPositionProvider`, so the tail always points at something.
- A badge's padding is derived from the theme, so the label stays clear of the
  outline at any stroke width or roughness.
- `Modifier.drawablyTilt()` is new: on drawably.dev the scatter is the demo
  page's own CSS rather than part of the library.

[Unreleased]: https://github.com/dim971/drawably-android/compare/0.2.0...HEAD
[0.2.0]: https://github.com/dim971/drawably-android/releases/tag/0.2.0
[0.1.0]: https://github.com/dim971/drawably-android/releases/tag/0.1.0
