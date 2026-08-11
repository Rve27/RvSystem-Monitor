package com.rve.systemmonitor.domain.repository

import com.rve.systemmonitor.utils.AppLanguage
import com.rve.systemmonitor.utils.NavMode
import com.rve.systemmonitor.utils.NavType
import com.rve.systemmonitor.utils.ThemeMode
import com.rve.systemmonitor.utils.VibrationIntensity
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val themeMode: Flow<ThemeMode>
    val language: Flow<AppLanguage>
    val amoledMode: Flow<Boolean>
    val hapticFeedbackEnabled: Flow<Boolean>
    val vibrationIntensity: Flow<VibrationIntensity>
    val isSetupCompleted: Flow<Boolean>
    val cpuRefreshDelay: Flow<Long>
    val memoryRefreshDelay: Flow<Long>
    val gpuRefreshDelay: Flow<Long>
    val batteryRefreshDelay: Flow<Long>
    val batteryGraphHistorySeconds: Flow<Int>
    val autoUpdateEnabled: Flow<Boolean>
    val useShizuku: Flow<Boolean>
    val updatesPausedUntil: Flow<Long>
    val blurEffectEnabled: Flow<Boolean>
    val navBarCornerRadius: Flow<Int>
    val navMode: Flow<NavMode>
    val navType: Flow<NavType>
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setLanguage(language: AppLanguage)
    suspend fun setAmoledMode(enabled: Boolean)
    suspend fun setHapticFeedbackEnabled(enabled: Boolean)
    suspend fun setVibrationIntensity(intensity: VibrationIntensity)
    suspend fun setSetupCompleted(completed: Boolean)
    suspend fun setCpuRefreshDelay(delayMillis: Long)
    suspend fun setMemoryRefreshDelay(delayMillis: Long)
    suspend fun setGpuRefreshDelay(delayMillis: Long)
    suspend fun setBatteryRefreshDelay(delayMillis: Long)
    suspend fun setBatteryGraphHistorySeconds(seconds: Int)
    suspend fun setAutoUpdateEnabled(enabled: Boolean)
    suspend fun setUseShizuku(enabled: Boolean)
    suspend fun setUpdatesPausedUntil(timestampMillis: Long)
    suspend fun setBlurEffectEnabled(enabled: Boolean)
    suspend fun setNavBarCornerRadius(radius: Int)
    suspend fun setNavMode(mode: NavMode)
    suspend fun setNavType(type: NavType)
    suspend fun exportSettings(): String
    suspend fun importSettings(json: String)
}
