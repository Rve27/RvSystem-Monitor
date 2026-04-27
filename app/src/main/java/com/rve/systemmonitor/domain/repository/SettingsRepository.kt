package com.rve.systemmonitor.domain.repository

import com.rve.systemmonitor.utils.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val themeMode: Flow<ThemeMode>
    val cpuRefreshDelay: Flow<Long>
    val memoryRefreshDelay: Flow<Long>
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setCpuRefreshDelay(delayMillis: Long)
    suspend fun setMemoryRefreshDelay(delayMillis: Long)
}
