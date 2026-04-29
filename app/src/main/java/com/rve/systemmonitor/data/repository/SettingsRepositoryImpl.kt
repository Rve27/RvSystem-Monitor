package com.rve.systemmonitor.data.repository

import android.app.Application
import com.rve.systemmonitor.domain.repository.SettingsRepository
import com.rve.systemmonitor.utils.SettingsPreferences
import com.rve.systemmonitor.utils.ThemeMode
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class SettingsRepositoryImpl @Inject constructor(application: Application) : SettingsRepository {

    private val settingsPreferences = SettingsPreferences(application)

    override val themeMode: Flow<ThemeMode> = settingsPreferences.themeModeFlow

    override val isSetupCompleted: Flow<Boolean> = settingsPreferences.isSetupCompletedFlow

    override val cpuRefreshDelay: Flow<Long> = settingsPreferences.cpuRefreshDelayFlow

    override val memoryRefreshDelay: Flow<Long> = settingsPreferences.memoryRefreshDelayFlow

    override val batteryRefreshDelay: Flow<Long> = settingsPreferences.batteryRefreshDelayFlow

    override suspend fun setThemeMode(mode: ThemeMode) {
        settingsPreferences.saveThemeMode(mode)
    }

    override suspend fun setSetupCompleted(completed: Boolean) {
        settingsPreferences.saveSetupCompleted(completed)
    }

    override suspend fun setCpuRefreshDelay(delayMillis: Long) {
        settingsPreferences.saveCpuRefreshDelay(delayMillis)
    }

    override suspend fun setMemoryRefreshDelay(delayMillis: Long) {
        settingsPreferences.saveMemoryRefreshDelay(delayMillis)
    }

    override suspend fun setBatteryRefreshDelay(delayMillis: Long) {
        settingsPreferences.saveBatteryRefreshDelay(delayMillis)
    }
}
