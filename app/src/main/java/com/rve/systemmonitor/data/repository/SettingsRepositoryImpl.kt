package com.rve.systemmonitor.data.repository

import android.app.Application
import com.rve.systemmonitor.domain.repository.SettingsRepository
import com.rve.systemmonitor.utils.SettingsPreferences
import com.rve.systemmonitor.utils.ThemeMode
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(private val application: Application) : SettingsRepository {

    private val settingsPreferences = SettingsPreferences(application)

    override val themeMode: Flow<ThemeMode> = settingsPreferences.themeModeFlow

    override suspend fun setThemeMode(mode: ThemeMode) {
        settingsPreferences.saveThemeMode(mode)
    }
}
