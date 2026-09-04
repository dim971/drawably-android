package dev.drawably.showcase

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.drawably.compose.theme.DrawablyError
import dev.drawably.compose.theme.DrawablyNeutral
import dev.drawably.compose.theme.DrawablyPenBlue
import dev.drawably.compose.theme.DrawablySuccess
import java.util.Locale

private val inks = listOf(DrawablyPenBlue, Color.Black, DrawablyError, DrawablySuccess, DrawablyNeutral)

/**
 * The pen settings, on every screen.
 *
 * Parametric jitter is the whole point of this library, so the controls that
 * drive it are always to hand rather than buried in a settings screen.
 */
@Composable
fun PenControls(modifier: Modifier = Modifier) {
    val settings = LocalShowcaseSettings.current
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Pen", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.weight(1f))
            TextButton(onClick = { settings.resketch() }) { Text("Re-sketch") }
        }

        PenSlider("Roughness", settings.theme.roughness.toFloat(), 0f..3f) {
            settings.theme = settings.theme.copy(roughness = it.toDouble())
        }
        PenSlider("Boil", settings.theme.boil.toFloat(), 0f..2f) {
            settings.theme = settings.theme.copy(boil = it.toDouble())
        }
        PenSlider("Width", settings.theme.width.value, 0.5f..6f) {
            settings.theme = settings.theme.copy(width = it.dp)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Ink", style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.width(10.dp))
            inks.forEach { ink ->
                Box(
                    Modifier
                        .padding(end = 8.dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(ink)
                        .clickable { settings.theme = settings.theme.copy(stroke = ink, fill = ink) },
                )
            }
            Spacer(Modifier.weight(1f))
            TextButton(onClick = { settings.reset() }) { Text("Reset") }
        }
    }
}

@Composable
private fun PenSlider(
    title: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onChange: (Float) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            title,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.width(72.dp),
        )
        Slider(
            value = value,
            onValueChange = onChange,
            valueRange = range,
            modifier = Modifier.weight(1f),
        )
        Text(
            String.format(Locale.US, "%.2f", value),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.width(44.dp).padding(start = 8.dp),
        )
    }
}
