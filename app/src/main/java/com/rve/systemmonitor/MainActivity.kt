package com.rve.systemmonitor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rve.systemmonitor.domain.repository.SettingsRepository
import com.rve.systemmonitor.ui.navigation.AppNavigation
import com.rve.systemmonitor.ui.theme.RvSystemMonitorTheme
import com.rve.systemmonitor.utils.ThemeMode
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val themeMode by settingsRepository.themeMode.collectAsStateWithLifecycle(ThemeMode.SYSTEM)

            val darkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            RvSystemMonitorTheme(darkTheme) {
                AppNavigation()
            }
        }
    }
}
