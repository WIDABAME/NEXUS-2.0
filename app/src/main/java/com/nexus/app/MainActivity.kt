package com.nexus.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nexus.app.ui.screens.ChecklistsScreen
import com.nexus.app.ui.screens.CreateChecklistScreen
import com.nexus.app.ui.screens.CreateNoteScreen
import com.nexus.app.ui.screens.GraphScreen
import com.nexus.app.ui.screens.NoteDetailScreen
import com.nexus.app.ui.screens.NotesScreen
import com.nexus.app.ui.screens.SplashScreen
import com.nexus.app.ui.theme.NexusTheme
import com.nexus.app.viewmodel.NexusViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: NexusViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            NexusTheme {
                Surface {
                    NexusApp(viewModel)
                }
            }
        }
    }
}

@Composable
fun NexusApp(viewModel: NexusViewModel) {
    val navController = rememberNavController()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute == "notes" || currentRoute == "checklists" || currentRoute == "graph"

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = com.nexus.app.ui.theme.NexusSurface,
                    contentColor = com.nexus.app.ui.theme.NexusTextPrimary
                ) {
                    NavigationBarItem(
                        selected = currentRoute == "notes",
                        onClick = { navController.navigate("notes") { popUpTo(navController.graph.findStartDestination().id) } },
                        icon = { Icon(Icons.AutoMirrored.Filled.Notes, contentDescription = "Notas") },
                        label = { Text("Notas") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = com.nexus.app.ui.theme.NexusBackground,
                            selectedTextColor = com.nexus.app.ui.theme.NexusAccent,
                            indicatorColor = com.nexus.app.ui.theme.NexusAccent,
                            unselectedIconColor = com.nexus.app.ui.theme.NexusTextSecondary,
                            unselectedTextColor = com.nexus.app.ui.theme.NexusTextSecondary
                        )
                    )
                    NavigationBarItem(
                        selected = currentRoute == "checklists",
                        onClick = { navController.navigate("checklists") { popUpTo(navController.graph.findStartDestination().id) } },
                        icon = { Icon(Icons.Default.Checklist, contentDescription = "Lista") },
                        label = { Text("Lista") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = com.nexus.app.ui.theme.NexusBackground,
                            selectedTextColor = com.nexus.app.ui.theme.NexusAccent,
                            indicatorColor = com.nexus.app.ui.theme.NexusAccent,
                            unselectedIconColor = com.nexus.app.ui.theme.NexusTextSecondary,
                            unselectedTextColor = com.nexus.app.ui.theme.NexusTextSecondary
                        )
                    )
                    NavigationBarItem(
                        selected = currentRoute == "graph",
                        onClick = { navController.navigate("graph") { popUpTo(navController.graph.findStartDestination().id) } },
                        icon = { Icon(Icons.Default.AccountTree, contentDescription = "Grafo") },
                        label = { Text("Grafo") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = com.nexus.app.ui.theme.NexusBackground,
                            selectedTextColor = com.nexus.app.ui.theme.NexusAccent,
                            indicatorColor = com.nexus.app.ui.theme.NexusAccent,
                            unselectedIconColor = com.nexus.app.ui.theme.NexusTextSecondary,
                            unselectedTextColor = com.nexus.app.ui.theme.NexusTextSecondary
                        )
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "splash",
            modifier = androidx.compose.ui.Modifier.padding(padding)
        ) {
            composable("splash") {
                SplashScreen(
                    onSplashFinished = {
                        navController.navigate("notes") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                )
            }
            composable("notes") {
                NotesScreen(
                    viewModel = viewModel,
                    onOpenNote = { id -> navController.navigate("note/$id") },
                    onCreateNote = { navController.navigate("create") }
                )
            }
            composable("checklists") {
                ChecklistsScreen(
                    viewModel = viewModel,
                    onCreateChecklist = { navController.navigate("create-checklist") }
                )
            }
            composable("create-checklist") {
                CreateChecklistScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onCreated = { navController.popBackStack() }
                )
            }
            composable("graph") {
                GraphScreen(
                    viewModel = viewModel,
                    onOpenNote = { id -> navController.navigate("note/$id") }
                )
            }
            composable("create") {
                CreateNoteScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onCreated = { _ -> navController.popBackStack() }
                )
            }
            composable("note/{noteId}") { backStackEntry ->
                val noteId = backStackEntry.arguments?.getString("noteId")?.toLongOrNull() ?: return@composable
                NoteDetailScreen(
                    noteId = noteId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
