package com.rve.systemmonitor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rve.systemmonitor.ui.navigation.AppNavigation
import com.rve.systemmonitor.ui.theme.RvSystemMonitorTheme
import com.rve.systemmonitor.utils.SettingsPreferences
import com.rve.systemmonitor.utils.ThemeMode

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val settingsPreferences = SettingsPreferences(this)

        setContent {
            val themeMode by settingsPreferences.themeModeFlow.collectAsStateWithLifecycle(null)

            if (themeMode == null) {
                return@setContent
            }

            val darkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                else -> isSystemInDarkTheme()
            }

            RvSystemMonitorTheme(darkTheme) {
                AppNavigation()
            }
        }
    }
}
