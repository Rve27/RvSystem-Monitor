package com.rve.systemmonitor.domain.repository

import com.rve.systemmonitor.utils.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val themeMode: Flow<ThemeMode>
    val isSetupCompleted: Flow<Boolean>
    val cpuRefreshDelay: Flow<Long>
    val memoryRefreshDelay: Flow<Long>
    val batteryRefreshDelay: Flow<Long>
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setSetupCompleted(completed: Boolean)
    suspend fun setCpuRefreshDelay(delayMillis: Long)
    suspend fun setMemoryRefreshDelay(delayMillis: Long)
    suspend fun setBatteryRefreshDelay(delayMillis: Long)
}
