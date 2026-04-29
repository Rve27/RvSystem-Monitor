package com.rve.systemmonitor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rve.systemmonitor.ui.navigation.AppNavigation
import com.rve.systemmonitor.ui.theme.RvSystemMonitorTheme
import com.rve.systemmonitor.ui.viewmodel.MainUiState
import com.rve.systemmonitor.ui.viewmodel.MainViewModel
import com.rve.systemmonitor.utils.ThemeMode
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.isNavigationBarContrastEnforced = false

        splashScreen.setKeepOnScreenCondition {
            viewModel.uiState.value is MainUiState.Loading
        }

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            if (uiState is MainUiState.Success) {
                val themeMode = (uiState as MainUiState.Success).themeMode
                val isSetupCompleted = (uiState as MainUiState.Success).isSetupCompleted
                val darkTheme = when (themeMode) {
                    ThemeMode.LIGHT -> false
                    ThemeMode.DARK -> true
                    ThemeMode.SYSTEM -> isSystemInDarkTheme()
                }

                RvSystemMonitorTheme(darkTheme) {
                    AppNavigation(isSetupCompleted)
                }
            }
        }
    }
}
