package com.reactide.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.reactide.app.ui.screens.EditorScreen
import com.reactide.app.ui.screens.ProjectsScreen
import com.reactide.app.viewmodels.ProjectViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val viewModel: ProjectViewModel = viewModel()

    NavHost(navController = navController, startDestination = "projects") {
        composable("projects") {
            ProjectsScreen(
                viewModel = viewModel,
                onProjectSelected = {
                    navController.navigate("editor")
                }
            )
        }
        composable("editor") {
            EditorScreen(
                viewModel = viewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
