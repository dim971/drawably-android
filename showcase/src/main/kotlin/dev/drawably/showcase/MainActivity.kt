package dev.drawably.showcase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { ShowcaseApp() }
    }
}

private enum class Section(
    val label: String,
) {
    Catalog("Catalog"),
    About("About"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShowcaseApp() {
    val settings = remember { ShowcaseSettings() }
    val navController = rememberNavController()
    var section by remember { mutableStateOf(Section.Catalog) }
    var title by remember { mutableStateOf("Drawably") }

    MaterialTheme {
        CompositionLocalProvider(LocalShowcaseSettings provides settings) {
            Scaffold(
                topBar = { TopAppBar(title = { Text(title) }) },
                bottomBar = {
                    NavigationBar {
                        Section.entries.forEach { entry ->
                            NavigationBarItem(
                                selected = section == entry,
                                onClick = {
                                    section = entry
                                    if (entry == Section.Catalog) {
                                        title = "Drawably"
                                        navController.popBackStack("catalog", inclusive = false)
                                    } else {
                                        title = "About"
                                    }
                                },
                                // the app ships no icon pack; the label alone
                                // is enough for two destinations
                                icon = { Text(if (entry == Section.Catalog) "▤" else "ⓘ") },
                                label = { Text(entry.label) },
                            )
                        }
                    }
                },
            ) { padding ->
                when (section) {
                    Section.About -> AboutScreen(padding)
                    Section.Catalog ->
                        NavHost(navController, startDestination = "catalog") {
                            composable("catalog") {
                                title = "Drawably"
                                CatalogHomeScreen(
                                    onOpen = { navController.navigate("component/$it") },
                                    contentPadding = padding,
                                )
                            }
                            composable("component/{id}") { entry ->
                                val id = entry.arguments?.getString("id")
                                val item = catalog.firstOrNull { it.id == id }
                                if (item != null) {
                                    title = item.name
                                    ComponentScreen(item, padding)
                                }
                            }
                        }
                }
            }
        }
    }
}
