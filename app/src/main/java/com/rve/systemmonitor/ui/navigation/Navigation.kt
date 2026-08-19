package com.rve.systemmonitor.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.rve.systemmonitor.RvSystemMonitorApp
import com.rve.systemmonitor.ui.components.ScreenWrapper
import com.rve.systemmonitor.ui.screens.AboutScreen
import com.rve.systemmonitor.ui.screens.AppSettingsScreen
import com.rve.systemmonitor.ui.screens.AppearanceSettingsScreen
import com.rve.systemmonitor.ui.screens.AutoToggleSettingsScreen
import com.rve.systemmonitor.ui.screens.GPUScreen
import com.rve.systemmonitor.ui.screens.MonitoringSettingsScreen
import com.rve.systemmonitor.ui.screens.OverlaySettingsScreen
import com.rve.systemmonitor.ui.screens.RustLibraryScreen
import com.rve.systemmonitor.ui.screens.SettingsScreen
import com.rve.systemmonitor.ui.screens.SetupScreen

@Composable
fun AppNavigation(isSetupCompleted: Boolean) {
    val startDestination: NavKey = remember {
        if (isSetupCompleted) Route.Main else Route.Setup(isTestFlow = false)
    }

    val backStack = rememberNavBackStack(startDestination)

    val entryProvider = entryProvider<NavKey> {
        entry<Route.Setup> { setup ->
            SetupScreen(
                onSetupCompleted = {
                    if (setup.isTestFlow) {
                        backStack.removeLastOrNull()
                    } else {
                        backStack.clear()
                        backStack.add(Route.Main)
                    }
                },
            )
        }

        entry<Route.Main> { mainRoute ->
            ScreenWrapper(backStack = backStack, myRoute = mainRoute) {
                RvSystemMonitorApp(
                    onNavigateToSettings = { backStack.add(Route.Settings) },
                    onNavigateToGPU = { backStack.add(Route.GPU) },
                )
            }
        }

        entry<Route.GPU> { route ->
            ScreenWrapper(backStack = backStack, myRoute = route) {
                GPUScreen(
                    onNavigateBack = { backStack.removeLastOrNull() },
                )
            }
        }

        entry<Route.Settings> { settingsRoute ->
            ScreenWrapper(backStack = backStack, myRoute = settingsRoute) {
                SettingsScreen(
                    onNavigateBack = { backStack.removeLastOrNull() },
                    onNavigateToApp = { backStack.add(Route.AppSettings) },
                    onNavigateToAppearance = { backStack.add(Route.AppearanceSettings) },
                    onNavigateToMonitoring = { backStack.add(Route.MonitoringSettings) },
                    onNavigateToOverlay = { backStack.add(Route.OverlaySettings) },
                    onNavigateToRustLibrary = { backStack.add(Route.RustLibrary) },
                    onNavigateToAbout = { backStack.add(Route.About) },
                )
            }
        }

        entry<Route.RustLibrary> { route ->
            ScreenWrapper(backStack = backStack, myRoute = route) {
                RustLibraryScreen(
                    onNavigateBack = { backStack.removeLastOrNull() },
                )
            }
        }

        entry<Route.AppSettings> { route ->
            ScreenWrapper(backStack = backStack, myRoute = route) {
                AppSettingsScreen(
                    onNavigateBack = { backStack.removeLastOrNull() },
                    onNavigateToSetup = { backStack.add(Route.Setup(isTestFlow = true)) },
                )
            }
        }

        entry<Route.About> { route ->
            ScreenWrapper(backStack = backStack, myRoute = route) {
                AboutScreen(
                    onNavigateBack = { backStack.removeLastOrNull() },
                )
            }
        }

        entry<Route.AppearanceSettings> { route ->
            ScreenWrapper(backStack = backStack, myRoute = route) {
                AppearanceSettingsScreen(
                    onNavigateBack = { backStack.removeLastOrNull() },
                )
            }
        }

        entry<Route.MonitoringSettings> { route ->
            ScreenWrapper(backStack = backStack, myRoute = route) {
                MonitoringSettingsScreen(
                    onNavigateBack = { backStack.removeLastOrNull() },
                )
            }
        }

        entry<Route.OverlaySettings> { route ->
            ScreenWrapper(backStack = backStack, myRoute = route) {
                OverlaySettingsScreen(
                    onNavigateBack = { backStack.removeLastOrNull() },
                    onNavigateToAutoToggle = { backStack.add(Route.AutoToggleSettings) },
                )
            }
        }

        entry<Route.AutoToggleSettings> { route ->
            ScreenWrapper(backStack = backStack, myRoute = route) {
                AutoToggleSettingsScreen(
                    onNavigateBack = { backStack.removeLastOrNull() },
                )
            }
        }
    }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider,
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
        transitionSpec = {
            aospSharedAxisEnter() togetherWith aospSharedAxisExit()
        },
        popTransitionSpec = {
            aospSharedAxisPopEnter() togetherWith aospSharedAxisPopExit()
        },
        predictivePopTransitionSpec = {
            aospSharedAxisPopEnter() togetherWith aospSharedAxisPopExit()
        },
    )
}
