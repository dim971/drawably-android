package dev.drawably.showcase

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.drawably.compose.components.DrawablyBadge
import dev.drawably.compose.components.DrawablyBadgeVariant
import dev.drawably.compose.components.DrawablyCard
import dev.drawably.compose.components.DrawablyDivider
import dev.drawably.compose.components.DrawablyList
import dev.drawably.compose.components.DrawablyListMarker
import dev.drawably.compose.theme.DrawablyText
import dev.drawably.compose.theme.LocalDrawablyTheme

/**
 * Wraps content in the live theme and re-keys it on "re-sketch", so every
 * control inside picks a fresh seed.
 */
@Composable
private fun Sketched(content: @Composable () -> Unit) {
    val settings = LocalShowcaseSettings.current
    CompositionLocalProvider(LocalDrawablyTheme provides settings.theme) {
        key(settings.resketchToken) { content() }
    }
}

/** Every component, each row showing the real thing rather than a screenshot. */
@Composable
fun CatalogHomeScreen(
    onOpen: (String) -> Unit,
    contentPadding: PaddingValues,
) {
    LazyColumn(
        contentPadding = contentPadding,
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            PenControls(Modifier.padding(16.dp))
        }
        item {
            Text(
                "Components",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
            )
        }
        items(catalog) { entry ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable { onOpen(entry.id) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Column(Modifier.weight(1f)) {
                    Text(entry.name, fontWeight = FontWeight.Medium)
                    Text(
                        entry.summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.padding(6.dp))
                Box(Modifier.widthIn(max = 130.dp)) {
                    Sketched { entry.preview() }
                }
            }
            HorizontalDivider()
        }
    }
}

/** One component: what it is, every variant of it live, and the code for each. */
@Composable
fun ComponentScreen(
    entry: CatalogEntry,
    contentPadding: PaddingValues,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(contentPadding)
                .padding(16.dp),
    ) {
        Text(entry.summary, color = MaterialTheme.colorScheme.onSurfaceVariant)
        PenControls()

        entry.demos.forEach { demo ->
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(demo.title, fontWeight = FontWeight.SemiBold)
                demo.note?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                ) {
                    Sketched { demo.sample() }
                }
                CodeSnippet(demo.code)
            }
        }
    }
}

/** What this is, where it came from, and who to credit. */
@Composable
fun AboutScreen(contentPadding: PaddingValues) {
    Column(
        verticalArrangement = Arrangement.spacedBy(18.dp),
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(contentPadding)
                .padding(24.dp),
    ) {
        Text(
            "Hand-drawn UI controls. Every composition a fresh pen sketch, boiling like a doodle.",
            style = MaterialTheme.typography.titleMedium,
        )
        Sketched {
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DrawablyBadge { DrawablyText("v0.1.0") }
                    DrawablyBadge(variant = DrawablyBadgeVariant.Scribble) { DrawablyText("MIT") }
                }
                DrawablyDivider()
                DrawablyCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        DrawablyText("A Compose port of Drawably")
                        DrawablyText(
                            "The stroke engine is a direct port of the web library's, checked " +
                                "against fixtures generated from the published npm package: the " +
                                "same PRNG stream, the same sample counts, the same boil frames.",
                        )
                        DrawablyText("drawably.dev")
                    }
                }
                DrawablyList(
                    listOf(
                        "Zero dependencies beyond Compose Foundation",
                        "Real controls underneath, so TalkBack and focus work",
                        "Boils on a ticker, not a render loop",
                    ),
                    marker = DrawablyListMarker.Check,
                ) {
                    DrawablyText(it)
                }
            }
        }
        Text(
            "Upstream Drawably is © 2026 Daniel Belyi, MIT licensed.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
