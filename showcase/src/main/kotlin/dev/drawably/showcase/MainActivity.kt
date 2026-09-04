package dev.drawably.showcase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.drawably.compose.components.DrawablyArrow
import dev.drawably.compose.components.DrawablyArrowLayer
import dev.drawably.compose.components.DrawablyBadge
import dev.drawably.compose.components.DrawablyBadgeVariant
import dev.drawably.compose.components.DrawablyButton
import dev.drawably.compose.components.DrawablyButtonState
import dev.drawably.compose.components.DrawablyButtonVariant
import dev.drawably.compose.components.DrawablyCard
import dev.drawably.compose.components.DrawablyCheckbox
import dev.drawably.compose.components.DrawablyDecoratedText
import dev.drawably.compose.components.DrawablyDecoration
import dev.drawably.compose.components.DrawablyDivider
import dev.drawably.compose.components.DrawablyList
import dev.drawably.compose.components.DrawablyListMarker
import dev.drawably.compose.components.DrawablyRadioButton
import dev.drawably.compose.components.DrawablySelect
import dev.drawably.compose.components.DrawablySwitch
import dev.drawably.compose.components.DrawablyTextArea
import dev.drawably.compose.components.DrawablyTextField
import dev.drawably.compose.components.DrawablyTone
import dev.drawably.compose.theme.DrawablyText
import dev.drawably.compose.theme.DrawablyTheme
import dev.drawably.compose.theme.LocalDrawablyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DrawablyTheme {
                ScratchScreen()
            }
        }
    }
}

private val heading = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)

@Composable
private fun Heading(text: String) {
    BasicText(text, style = heading)
}

/** A temporary page for eyeballing components as they land. */
@Composable
private fun ScratchScreen() {
    var agreed by remember { mutableStateOf(true) }
    var subscribed by remember { mutableStateOf(false) }
    var boiling by remember { mutableStateOf(true) }
    var tool by remember { mutableStateOf("Pen") }
    var name by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("Boils like a doodle.") }
    var weight by remember { mutableStateOf("Medium") }
    val ink = LocalDrawablyTheme.current.stroke

    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .padding(top = 32.dp),
    ) {
        Heading("Decorations")
        DrawablyDecoratedText("boils like a doodle", DrawablyDecoration.Underline)
        DrawablyDecoratedText("real inputs", DrawablyDecoration.Highlight)
        DrawablyDecoratedText("zero dependencies", DrawablyDecoration.Circle)

        Heading("Arrow")
        DrawablyArrowLayer(arrows = listOf(DrawablyArrow("hint", "send"))) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 20.dp),
            ) {
                BasicText("start here", modifier = Modifier.drawablyAnchor("hint"))
                Spacer(Modifier.width(120.dp))
                DrawablyButton(
                    text = "Send",
                    onClick = {},
                    variant = DrawablyButtonVariant.Solid,
                    modifier = Modifier.drawablyAnchor("send"),
                )
            }
        }

        Heading("Buttons")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DrawablyButton("Done", {}, variant = DrawablyButtonVariant.Solid)
            DrawablyButton("Save", {}, variant = DrawablyButtonVariant.Scribble)
            DrawablyButton("Cancel", {}, tone = DrawablyTone.Neutral)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DrawablyButton("Delete", {}, tone = DrawablyTone.Danger)
            DrawablyButton("Retry", {}, state = DrawablyButtonState.Error)
            DrawablyButton("Saved", {}, state = DrawablyButtonState.Success)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DrawablyButton("Wait", {}, state = DrawablyButtonState.Loading)
            DrawablyButton("Off", {}, enabled = false)
        }

        Heading("Choice")
        DrawablyCheckbox(agreed, { agreed = it }) { DrawablyText("Ship it") }
        DrawablyCheckbox(subscribed, { subscribed = it }) { DrawablyText("Subscribe") }
        DrawablySwitch(boiling, { boiling = it }) { DrawablyText("Boil") }
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            DrawablyRadioButton(tool == "Pen", { tool = "Pen" }) { DrawablyText("Pen") }
            DrawablyRadioButton(tool == "Pencil", { tool = "Pencil" }) { DrawablyText("Pencil") }
        }

        Heading("Fields")
        DrawablyTextField(name, { name = it }, placeholder = "Your name")
        DrawablyTextArea(notes, { notes = it }, minHeight = 72.dp)
        DrawablySelect(weight, listOf("Light", "Medium", "Heavy"), { weight = it })

        Heading("List")
        DrawablyList(listOf("Sketch it", "Boil it", "Ship it"), marker = DrawablyListMarker.Check) {
            DrawablyText(it)
        }

        Heading("Badges")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DrawablyBadge { DrawablyText("v0.1.0", color = ink) }
            DrawablyBadge(variant = DrawablyBadgeVariant.Scribble) {
                DrawablyText("MIT", color = ink)
            }
        }

        Heading("Divider")
        DrawablyDivider()

        Heading("Card")
        DrawablyCard {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DrawablyText("npm i drawably", color = ink)
                DrawablyText("Every render a fresh pen sketch.", color = ink)
            }
        }
    }
}
