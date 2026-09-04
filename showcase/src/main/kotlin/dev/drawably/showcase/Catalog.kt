package dev.drawably.showcase

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import dev.drawably.compose.components.DrawablyBadge
import dev.drawably.compose.components.DrawablyBadgeVariant
import dev.drawably.compose.components.DrawablyButton
import dev.drawably.compose.components.DrawablyButtonState
import dev.drawably.compose.components.DrawablyButtonVariant
import dev.drawably.compose.components.DrawablyCard
import dev.drawably.compose.components.DrawablyDecoratedText
import dev.drawably.compose.components.DrawablyDecoration
import dev.drawably.compose.components.DrawablyDivider
import dev.drawably.compose.components.DrawablyList
import dev.drawably.compose.components.DrawablyListMarker
import dev.drawably.compose.components.DrawablyTone
import dev.drawably.compose.sketch.drawablyTilt
import dev.drawably.compose.theme.DrawablyText

/** One demo on a component's screen: a live sample and the code behind it. */
class Demo(
    val title: String,
    val note: String? = null,
    val code: String,
    val sample: @Composable () -> Unit,
)

/**
 * One component in the catalog.
 *
 * Adding a component means adding an entry here — the home list, the component
 * screen and the previews all read from this one place.
 */
class CatalogEntry(
    val name: String,
    val summary: String,
    val preview: @Composable () -> Unit,
    val demos: List<Demo>,
) {
    val id: String get() = name
}

private val mono = TextStyle(fontFamily = FontFamily.Monospace)

/** Every component in the library, in the order the docs introduce them. */
val catalog: List<CatalogEntry> =
    listOf(
        CatalogEntry(
            name = "Button",
            summary = "Three variants, three tones, four states. Re-sketches when pressed.",
            preview = { DrawablyButton("Done", {}, variant = DrawablyButtonVariant.Solid) },
            demos =
                listOf(
                    Demo(
                        title = "Variants",
                        code =
                            """
                            DrawablyButton("Done", ::submit, variant = DrawablyButtonVariant.Solid)
                            DrawablyButton("Save", ::save, variant = DrawablyButtonVariant.Scribble)
                            DrawablyButton("Cancel", ::dismiss)
                            """.trimIndent(),
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            DrawablyButton("Done", {}, variant = DrawablyButtonVariant.Solid)
                            DrawablyButton("Save", {}, variant = DrawablyButtonVariant.Scribble)
                            DrawablyButton("Cancel", {})
                        }
                    },
                    Demo(
                        title = "Tones",
                        code =
                            """
                            DrawablyButton("Keep", {}, tone = DrawablyTone.Standard)
                            DrawablyButton("Later", {}, tone = DrawablyTone.Neutral)
                            DrawablyButton("Delete", {}, tone = DrawablyTone.Danger)
                            """.trimIndent(),
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            DrawablyButton("Keep", {})
                            DrawablyButton("Later", {}, tone = DrawablyTone.Neutral)
                            DrawablyButton("Delete", {}, tone = DrawablyTone.Danger)
                        }
                    },
                    Demo(
                        title = "States",
                        note = "A state recolours the ink without touching the theme.",
                        code =
                            """
                            DrawablyButton("Wait", {}, state = DrawablyButtonState.Loading)
                            DrawablyButton("Retry", {}, state = DrawablyButtonState.Error)
                            DrawablyButton("Saved", {}, state = DrawablyButtonState.Success)
                            """.trimIndent(),
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            DrawablyButton("Wait", {}, state = DrawablyButtonState.Loading)
                            DrawablyButton("Retry", {}, state = DrawablyButtonState.Error)
                            DrawablyButton("Saved", {}, state = DrawablyButtonState.Success)
                        }
                    },
                    Demo(
                        title = "Live",
                        code =
                            """
                            var state by remember { mutableStateOf(DrawablyButtonState.Idle) }

                            DrawablyButton("Save", ::save, state = state)
                            """.trimIndent(),
                        sample = { LoadingButtonSample() },
                    ),
                ),
        ),
        CatalogEntry(
            name = "Card",
            summary = "A sketched box to group content in.",
            preview = { DrawablyCard { DrawablyText("Card") } },
            demos =
                listOf(
                    Demo(
                        title = "Card",
                        code =
                            """
                            DrawablyCard {
                                Text("npm i drawably")
                            }
                            """.trimIndent(),
                    ) {
                        DrawablyCard {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                DrawablyText("npm i drawably", style = mono)
                                DrawablyText("Every render a fresh pen sketch.")
                            }
                        }
                    },
                ),
        ),
        CatalogEntry(
            name = "Checkbox",
            summary = "The tick is drawn on stroke by stroke, not faded in.",
            preview = { CheckboxPreview() },
            demos =
                listOf(
                    Demo(
                        title = "Checkbox",
                        code =
                            """
                            var agreed by remember { mutableStateOf(true) }

                            DrawablyCheckbox(agreed, { agreed = it }) { Text("Ship it") }
                            """.trimIndent(),
                        sample = { CheckboxSample() },
                    ),
                ),
        ),
        CatalogEntry(
            name = "Radio",
            summary = "A ring that gains a dot when picked.",
            preview = { RadioPreview() },
            demos =
                listOf(
                    Demo(
                        title = "Radio group",
                        code =
                            """
                            var tool by remember { mutableStateOf("Pen") }

                            DrawablyRadioButton(tool == "Pen", { tool = "Pen" }) { Text("Pen") }
                            """.trimIndent(),
                        sample = { RadioSample() },
                    ),
                ),
        ),
        CatalogEntry(
            name = "Switch",
            summary = "A pill with an ink blob that slides across it.",
            preview = { SwitchPreview() },
            demos =
                listOf(
                    Demo(
                        title = "Switch",
                        code =
                            """
                            var boiling by remember { mutableStateOf(true) }

                            DrawablySwitch(boiling, { boiling = it }) { Text("Boil") }
                            """.trimIndent(),
                        sample = { SwitchSample() },
                    ),
                ),
        ),
        CatalogEntry(
            name = "Text field",
            summary = "One line of text in a sketched box.",
            preview = { TextFieldPreview() },
            demos =
                listOf(
                    Demo(
                        title = "Text field",
                        code =
                            """
                            var name by remember { mutableStateOf("") }

                            DrawablyTextField(name, { name = it }, placeholder = "Your name")
                            """.trimIndent(),
                        sample = { TextFieldSample() },
                    ),
                ),
        ),
        CatalogEntry(
            name = "Text area",
            summary = "Several lines of it, in the same box.",
            preview = { TextFieldPreview() },
            demos =
                listOf(
                    Demo(
                        title = "Text area",
                        code =
                            """
                            var notes by remember { mutableStateOf("") }

                            DrawablyTextArea(notes, { notes = it }, minHeight = 90.dp)
                            """.trimIndent(),
                        sample = { TextAreaSample() },
                    ),
                ),
        ),
        CatalogEntry(
            name = "Select",
            summary = "A pen chevron, opening a sketched list tailed back to the field.",
            preview = { SelectPreview() },
            demos =
                listOf(
                    Demo(
                        title = "Select",
                        note = "The box is already as wide as the widest option, so picking never shifts the layout.",
                        code =
                            """
                            var weight by remember { mutableStateOf("Medium") }

                            DrawablySelect(weight, listOf("Light", "Medium", "Heavy"), { weight = it })
                            """.trimIndent(),
                        sample = { SelectSample() },
                    ),
                ),
        ),
        CatalogEntry(
            name = "Divider",
            summary = "A pen line across the available width.",
            preview = { DrawablyDivider() },
            demos =
                listOf(
                    Demo(title = "Divider", code = "DrawablyDivider()") { DrawablyDivider() },
                ),
        ),
        CatalogEntry(
            name = "Badge",
            summary = "A small sharp-cornered tag round a monospaced label.",
            preview = { DrawablyBadge { DrawablyText("v0.1.0", style = mono) } },
            demos =
                listOf(
                    Demo(
                        title = "Variants",
                        code =
                            """
                            DrawablyBadge { Text("v0.1.0") }
                            DrawablyBadge(variant = DrawablyBadgeVariant.Scribble) { Text("MIT") }
                            """.trimIndent(),
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            DrawablyBadge { DrawablyText("v0.1.0", style = mono) }
                            DrawablyBadge(variant = DrawablyBadgeVariant.Scribble) {
                                DrawablyText("MIT", style = mono)
                            }
                        }
                    },
                ),
        ),
        CatalogEntry(
            name = "List",
            summary = "Bullets drawn by hand in the gutter.",
            preview = {
                DrawablyList(listOf("one", "two"), marker = DrawablyListMarker.Check) {
                    DrawablyText(it)
                }
            },
            demos =
                listOf(
                    Demo(
                        title = "Markers",
                        code =
                            """
                            DrawablyList(steps, marker = DrawablyListMarker.Check) { step ->
                                Text(step)
                            }
                            """.trimIndent(),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                            DrawablyList(listOf("zero deps", "real inputs", "boils in CSS")) {
                                DrawablyText(it)
                            }
                            DrawablyList(
                                listOf("Sketch it", "Boil it", "Ship it"),
                                marker = DrawablyListMarker.Check,
                            ) {
                                DrawablyText(it)
                            }
                        }
                    },
                ),
        ),
        CatalogEntry(
            name = "Underline",
            summary = "A pen line under the words, one per line they wrap onto.",
            preview = { DrawablyDecoratedText("underline", DrawablyDecoration.Underline) },
            demos =
                listOf(
                    Demo(
                        title = "Underline",
                        code = """DrawablyDecoratedText("boils like a doodle", DrawablyDecoration.Underline)""",
                    ) {
                        DrawablyDecoratedText(
                            "boils like a doodle, and every line it wraps onto gets its own",
                            DrawablyDecoration.Underline,
                        )
                    },
                ),
        ),
        CatalogEntry(
            name = "Highlight",
            summary = "A marker swipe behind them.",
            preview = { DrawablyDecoratedText("highlight", DrawablyDecoration.Highlight) },
            demos =
                listOf(
                    Demo(
                        title = "Highlight",
                        code = """DrawablyDecoratedText("real inputs", DrawablyDecoration.Highlight)""",
                    ) {
                        DrawablyDecoratedText(
                            "real inputs, so keyboard and screen readers work as usual",
                            DrawablyDecoration.Highlight,
                        )
                    },
                ),
        ),
        CatalogEntry(
            name = "Circle",
            summary = "A loop around them, overshooting the way a hand does.",
            preview = { DrawablyDecoratedText("circle", DrawablyDecoration.Circle) },
            demos =
                listOf(
                    Demo(
                        title = "Circle",
                        code = """DrawablyDecoratedText("zero dependencies", DrawablyDecoration.Circle)""",
                    ) {
                        Column(Modifier.padding(8.dp)) {
                            DrawablyDecoratedText("zero dependencies", DrawablyDecoration.Circle)
                        }
                    },
                ),
        ),
        CatalogEntry(
            name = "Tilt",
            summary = "A small hand-placed lean, on any control.",
            preview = {
                DrawablyBadge(modifier = Modifier.drawablyTilt(5u, maxDegrees = 4f), seed = 5u) {
                    DrawablyText("tilt", style = mono)
                }
            },
            demos =
                listOf(
                    Demo(
                        title = "Tilt",
                        note = "The lean is picked once and held, so a control does not shift when it re-sketches.",
                        code =
                            """
                            DrawablyButton("Done", ::submit, modifier = Modifier.drawablyTilt())

                            DrawablyCard(modifier = Modifier.drawablyTilt(degrees = -1.5f)) { }
                            """.trimIndent(),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                DrawablyButton(
                                    "Done",
                                    {},
                                    variant = DrawablyButtonVariant.Solid,
                                    modifier = Modifier.drawablyTilt(3u),
                                )
                                DrawablyButton(
                                    "Save",
                                    {},
                                    variant = DrawablyButtonVariant.Scribble,
                                    modifier = Modifier.drawablyTilt(11u),
                                )
                                DrawablyButton("Cancel", {}, modifier = Modifier.drawablyTilt(19u))
                            }
                            DrawablyCard(modifier = Modifier.drawablyTilt(degrees = -1.5f), seed = 7u) {
                                DrawablyText("pinned to -1.5°")
                            }
                        }
                    },
                ),
        ),
        CatalogEntry(
            name = "Arrow",
            summary = "A sketched arrow between two named anchors.",
            preview = { ArrowPreview() },
            demos =
                listOf(
                    Demo(
                        title = "Arrow",
                        code =
                            """
                            DrawablyArrowLayer(arrows = listOf(DrawablyArrow("hint", "send"))) {
                                Text("start here", Modifier.drawablyAnchor("hint"))
                                DrawablyButton("Send", {}, modifier = Modifier.drawablyAnchor("send"))
                            }
                            """.trimIndent(),
                        sample = { ArrowSample() },
                    ),
                ),
        ),
    )
