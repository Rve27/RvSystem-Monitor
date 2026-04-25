package com.rve.systemmonitor.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rve.systemmonitor.RvSystemMonitorApp
import com.rve.systemmonitor.ui.screens.SettingsScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Route.Main,
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
    ) {
        composable<Route.Main> {
            RvSystemMonitorApp(
                onNavigateToSettings = { navController.navigate(Route.Settings) },
            )
        }

        composable<Route.Settings>(
            enterTransition = { enterTransition() },
            exitTransition = { exitTransition() },
            popEnterTransition = { popEnterTransition() },
            popExitTransition = { popExitTransition() },
        ) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}
