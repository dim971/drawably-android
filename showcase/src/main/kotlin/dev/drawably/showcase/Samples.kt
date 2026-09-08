package dev.drawably.showcase

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.drawably.compose.components.DrawablyArrow
import dev.drawably.compose.components.DrawablyArrowLayer
import dev.drawably.compose.components.DrawablyButton
import dev.drawably.compose.components.DrawablyButtonState
import dev.drawably.compose.components.DrawablyButtonVariant
import dev.drawably.compose.components.DrawablyCheckbox
import dev.drawably.compose.components.DrawablyRadioButton
import dev.drawably.compose.components.DrawablySelect
import dev.drawably.compose.components.DrawablySwitch
import dev.drawably.compose.components.DrawablyTextArea
import dev.drawably.compose.components.DrawablyTextField
import dev.drawably.compose.theme.DrawablyText
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Small stateful wrappers so each demo is genuinely interactive rather than a
// picture of a control.

@Composable
fun CheckboxSample() {
    var agreed by remember { mutableStateOf(true) }
    var subscribed by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        DrawablyCheckbox(agreed, { agreed = it }) { DrawablyText("Ship it") }
        DrawablyCheckbox(subscribed, { subscribed = it }) { DrawablyText("Subscribe") }
        DrawablyCheckbox(agreed, { agreed = it })
    }
}

@Composable
fun CheckboxPreview() {
    var on by remember { mutableStateOf(true) }
    DrawablyCheckbox(on, { on = it })
}

@Composable
fun RadioSample() {
    var tool by remember { mutableStateOf("Pen") }
    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
        listOf("Pen", "Pencil", "Marker").forEach { option ->
            DrawablyRadioButton(tool == option, { tool = option }) { DrawablyText(option) }
        }
    }
}

@Composable
fun RadioPreview() {
    DrawablyRadioButton(selected = true, onClick = {})
}

@Composable
fun SwitchSample() {
    var boiling by remember { mutableStateOf(true) }
    var muted by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        DrawablySwitch(boiling, { boiling = it }) { DrawablyText("Boil") }
        DrawablySwitch(muted, { muted = it }) { DrawablyText("Mute") }
    }
}

@Composable
fun SwitchPreview() {
    var on by remember { mutableStateOf(true) }
    DrawablySwitch(on, { on = it })
}

@Composable
fun TextFieldSample() {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("ada@example.com") }
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        DrawablyTextField(name, { name = it }, placeholder = "Your name")
        DrawablyTextField(email, { email = it }, placeholder = "Email")
    }
}

@Composable
fun TextFieldPreview() {
    DrawablyTextField("", {}, placeholder = "text", modifier = Modifier.width(110.dp))
}

@Composable
fun TextAreaSample() {
    var notes by remember {
        mutableStateOf("Every render a fresh pen sketch,\nboiling like a doodle.")
    }
    DrawablyTextArea(notes, { notes = it }, minHeight = 90.dp)
}

@Composable
fun SelectSample() {
    var weight by remember { mutableStateOf("Medium") }
    DrawablySelect(weight, listOf("Light", "Medium", "Heavy"), { weight = it })
}

@Composable
fun SelectPreview() {
    DrawablySelect("Medium", listOf("Medium"), {})
}

@Composable
fun ArrowSample() {
    DrawablyArrowLayer(arrows = listOf(DrawablyArrow("hint", "send"))) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 24.dp),
        ) {
            DrawablyText("start here", modifier = Modifier.drawablyAnchor("hint"))
            Spacer(Modifier.width(90.dp))
            DrawablyButton(
                text = "Send",
                onClick = {},
                variant = DrawablyButtonVariant.Solid,
                modifier = Modifier.drawablyAnchor("send"),
            )
        }
    }
}

@Composable
fun ArrowPreview() {
    DrawablyArrowLayer(arrows = listOf(DrawablyArrow("a", "b"))) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.width(1.dp).drawablyAnchor("a"))
            Spacer(Modifier.width(80.dp))
            Spacer(Modifier.width(1.dp).drawablyAnchor("b"))
        }
    }
}

/** Tap it: a loading button boils at 450ms instead of 1200ms. */
@Composable
fun LoadingButtonSample() {
    var state by remember { mutableStateOf(DrawablyButtonState.Idle) }
    val scope = rememberCoroutineScope()
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        DrawablyButton(
            text = "Save",
            onClick = {
                scope.launch {
                    state = DrawablyButtonState.Loading
                    delay(1400)
                    state = DrawablyButtonState.Success
                    delay(1400)
                    state = DrawablyButtonState.Idle
                }
            },
            state = state,
        )
        Text("Tap it: a loading button boils at 450ms instead of 1200ms.")
    }
}
