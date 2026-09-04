# Getting started

## Install

```kotlin
dependencies {
    implementation("dev.drawably:drawably-compose:0.1.0")
}
```

minSdk 24, compileSdk 37. The library depends on Compose Foundation, not
Material, so it brings nothing else with it.

## Your first control

```kotlin
DrawablyButton("Done", onClick = ::submit, variant = DrawablyButtonVariant.Solid)
```

That is a real clickable with a sketch drawn behind it. It picks a fresh sketch
on first composition, flickers gently while idle, and draws itself again when
you press it.

## Labels and the content colour

The library sits below Material, so it cannot set Material's `LocalContentColor`.
A solid button's label has to be paper rather than ink, so it publishes the
colour through `LocalDrawablyContentColor` instead. `DrawablyText` reads it:

```kotlin
DrawablyButton(onClick = ::submit, variant = DrawablyButtonVariant.Solid) {
    DrawablyText("Done")
}
```

If you would rather use Material's `Text`, read the local yourself:

```kotlin
DrawablyButton(onClick = ::submit) {
    Text("Done", color = LocalDrawablyContentColor.current)
}
```

The `DrawablyButton(text = …)` overload does this for you.

## Setting the pen

The theme cascades through a `CompositionLocal`, so set it once high up:

```kotlin
DrawablyTheme(DrawablyTheme(stroke = Color.Black, fill = Color.Black, roughness = 1.4)) {
    App()
}
```

See [theming.md](theming.md) for every property.

## Pinning a sketch

Every control takes an optional `seed`. Without one it picks a fresh sketch each
time it enters composition, which is the point — but previews and screenshot
tests want the same drawing every run:

```kotlin
DrawablyButton("Done", {}, seed = 42u)
```

A pinned seed also stops the control re-sketching under a press.

## Annotating

```kotlin
DrawablyDecoratedText("a fresh pen sketch", DrawablyDecoration.Underline)
DrawablyDecoratedText("real inputs", DrawablyDecoration.Highlight)
DrawablyDecoratedText("zero dependencies", DrawablyDecoration.Circle)
```

For anything that is not text, the same marks are modifiers:

```kotlin
Box(Modifier.drawablyHighlight()) { … }
```

Arrows join two named anchors inside a layer:

```kotlin
DrawablyArrowLayer(arrows = listOf(DrawablyArrow("hint", "send"))) {
    Row {
        DrawablyText("start here", modifier = Modifier.drawablyAnchor("hint"))
        DrawablyButton("Send", {}, modifier = Modifier.drawablyAnchor("send"))
    }
}
```

## Making a screen look hand-placed

```kotlin
DrawablyButton("Done", {}, modifier = Modifier.drawablyTilt())
DrawablyCard(modifier = Modifier.drawablyTilt(degrees = -1.5f)) { … }
```

## Where to go next

- [components.md](components.md) — the full reference
- [theming.md](theming.md) — the theme, seeds, and drawing your own shapes
- [architecture.md](architecture.md) — how a sketch reaches the screen
