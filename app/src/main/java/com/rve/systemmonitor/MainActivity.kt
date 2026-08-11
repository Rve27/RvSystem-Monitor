package com.rve.systemmonitor

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rve.systemmonitor.ui.components.UpdateDialog
import com.rve.systemmonitor.ui.navigation.AppNavigation
import com.rve.systemmonitor.ui.theme.RvSystemMonitorTheme
import com.rve.systemmonitor.ui.viewmodel.MainUiState
import com.rve.systemmonitor.ui.viewmodel.MainViewModel
import com.rve.systemmonitor.ui.viewmodel.UpdateViewModel
import com.rve.systemmonitor.utils.ThemeMode
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private val updateViewModel: UpdateViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.isNavigationBarContrastEnforced = false

        splashScreen.setKeepOnScreenCondition {
            viewModel.uiState.value is MainUiState.Loading
        }

        if (BuildConfig.DEBUG) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            if (uiState is MainUiState.Success) {
                val successState = uiState as MainUiState.Success

                LaunchedEffect(Unit) {
                    if (BuildConfig.ENABLE_UPDATER && successState.autoUpdateEnabled) {
                        updateViewModel.checkForUpdates()
                    }
                }

                val themeMode = successState.themeMode
                val amoledMode = successState.amoledMode
                val isSetupCompleted = successState.isSetupCompleted
                val hapticEnabled = successState.hapticFeedbackEnabled
                val vibrationIntensity = successState.vibrationIntensity
                val blurEffectEnabled = successState.blurEffectEnabled
                val navBarCornerRadius = successState.navBarCornerRadius.dp
                val navMode = successState.navMode
                val navType = successState.navType
                val darkTheme = when (themeMode) {
                    ThemeMode.LIGHT -> false
                    ThemeMode.DARK -> true
                    ThemeMode.SYSTEM -> isSystemInDarkTheme()
                }

                RvSystemMonitorTheme(
                    darkTheme,
                    amoledMode,
                    hapticEnabled,
                    vibrationIntensity,
                    blurEffectEnabled,
                    navBarCornerRadius,
                    navMode,
                    navType,
                ) {
                    if (BuildConfig.ENABLE_UPDATER) {
                        val updateUiState by updateViewModel.uiState.collectAsStateWithLifecycle()

                        UpdateDialog(
                            uiState = updateUiState,
                            onDownload = { updateViewModel.downloadAndInstall(it) },
                            onCancelDownload = { updateViewModel.cancelDownload() },
                            onDismiss = { updateViewModel.resetState() },
                            onPauseUpdates = { updateViewModel.pauseUpdates(it) },
                        )
                    }

                    AppNavigation(isSetupCompleted)
                }
            }
        }
    }
}
