package com.rve.systemmonitor.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsPreferences(private val context: Context) {
    companion object {
        val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        val LANGUAGE_KEY = stringPreferencesKey("language")
        val AMOLED_MODE_KEY = booleanPreferencesKey("amoled_mode")
        val IS_SETUP_COMPLETED_KEY = booleanPreferencesKey("is_setup_completed")
        val CPU_REFRESH_DELAY_KEY = longPreferencesKey("cpu_refresh_delay")
        val MEMORY_REFRESH_DELAY_KEY = longPreferencesKey("memory_refresh_delay")
        val GPU_REFRESH_DELAY_KEY = longPreferencesKey("gpu_refresh_delay")
        val BATTERY_REFRESH_DELAY_KEY = longPreferencesKey("battery_refresh_delay")
        val BATTERY_GRAPH_HISTORY_KEY = intPreferencesKey("battery_graph_history")
        val HAPTIC_FEEDBACK_ENABLED_KEY = booleanPreferencesKey("haptic_feedback_enabled")
        val VIBRATION_INTENSITY_KEY = stringPreferencesKey("vibration_intensity")
        val AUTO_UPDATE_ENABLED_KEY = booleanPreferencesKey("auto_update_enabled")
        val USE_SHIZUKU_KEY = booleanPreferencesKey("use_shizuku")
        val UPDATES_PAUSED_UNTIL_KEY = longPreferencesKey("updates_paused_until")
        val BLUR_EFFECT_ENABLED_KEY = booleanPreferencesKey("blur_effect_enabled")
        val NAV_BAR_CORNER_RADIUS_KEY = intPreferencesKey("nav_bar_corner_radius")
        val NAV_MODE_KEY = stringPreferencesKey("nav_mode")
        val NAV_TYPE_KEY = stringPreferencesKey("nav_type")

        const val DEFAULT_NAV_BAR_CORNER_RADIUS = 32
    }

    val autoUpdateEnabledFlow: Flow<Boolean> = context.dataStore.getValueFlow(AUTO_UPDATE_ENABLED_KEY, true)
    val useShizukuFlow: Flow<Boolean> = context.dataStore.getValueFlow(USE_SHIZUKU_KEY, false)
    val updatesPausedUntilFlow: Flow<Long> = context.dataStore.getValueFlow(UPDATES_PAUSED_UNTIL_KEY, 0L)
    val blurEffectEnabledFlow: Flow<Boolean> = context.dataStore.getValueFlow(BLUR_EFFECT_ENABLED_KEY, true)
    val navBarCornerRadiusFlow: Flow<Int> =
        context.dataStore.getValueFlow(NAV_BAR_CORNER_RADIUS_KEY, DEFAULT_NAV_BAR_CORNER_RADIUS)
            .map { it.coerceIn(12, 32) }
    val navModeFlow: Flow<NavMode> =
        context.dataStore.getEnumFlow(NAV_MODE_KEY, NavMode.FLOATING) { NavMode.valueOf(it) }
    val navTypeFlow: Flow<NavType> =
        context.dataStore.getEnumFlow(NAV_TYPE_KEY, NavType.LEGACY) { NavType.valueOf(it) }
    val themeModeFlow: Flow<ThemeMode> =
        context.dataStore.getEnumFlow(THEME_MODE_KEY, ThemeMode.SYSTEM) { ThemeMode.valueOf(it) }
    val languageFlow: Flow<AppLanguage> =
        context.dataStore.getEnumFlow(LANGUAGE_KEY, AppLanguage.SYSTEM) { AppLanguage.valueOf(it) }
    val amoledModeFlow: Flow<Boolean> = context.dataStore.getValueFlow(AMOLED_MODE_KEY, false)
    val vibrationIntensityFlow: Flow<VibrationIntensity> = context.dataStore.getEnumFlow(
        VIBRATION_INTENSITY_KEY,
        VibrationIntensity.LIGHT,
    ) { VibrationIntensity.valueOf(it) }
    val hapticFeedbackEnabledFlow: Flow<Boolean> = context.dataStore.getValueFlow(HAPTIC_FEEDBACK_ENABLED_KEY, true)
    val isSetupCompletedFlow: Flow<Boolean> = context.dataStore.getValueFlow(IS_SETUP_COMPLETED_KEY, false)
    val cpuRefreshDelayFlow: Flow<Long> = context.dataStore.getValueFlow(CPU_REFRESH_DELAY_KEY, 3000L)
    val memoryRefreshDelayFlow: Flow<Long> = context.dataStore.getValueFlow(MEMORY_REFRESH_DELAY_KEY, 3000L)
    val gpuRefreshDelayFlow: Flow<Long> = context.dataStore.getValueFlow(GPU_REFRESH_DELAY_KEY, 3000L)
    val batteryRefreshDelayFlow: Flow<Long> = context.dataStore.getValueFlow(BATTERY_REFRESH_DELAY_KEY, 1000L)
    val batteryGraphHistorySecondsFlow: Flow<Int> = context.dataStore.getValueFlow(BATTERY_GRAPH_HISTORY_KEY, 60)

    suspend fun saveThemeMode(mode: ThemeMode) = context.dataStore.setEnum(THEME_MODE_KEY, mode)
    suspend fun saveLanguage(language: AppLanguage) = context.dataStore.setEnum(LANGUAGE_KEY, language)
    suspend fun saveAmoledMode(enabled: Boolean) = context.dataStore.setValue(AMOLED_MODE_KEY, enabled)
    suspend fun saveSetupCompleted(completed: Boolean) = context.dataStore.setValue(IS_SETUP_COMPLETED_KEY, completed)
    suspend fun saveCpuRefreshDelay(delayMillis: Long) = context.dataStore.setValue(CPU_REFRESH_DELAY_KEY, delayMillis)
    suspend fun saveMemoryRefreshDelay(delayMillis: Long) =
        context.dataStore.setValue(MEMORY_REFRESH_DELAY_KEY, delayMillis)

    suspend fun saveGpuRefreshDelay(delayMillis: Long) = context.dataStore.setValue(GPU_REFRESH_DELAY_KEY, delayMillis)
    suspend fun saveBatteryRefreshDelay(delayMillis: Long) =
        context.dataStore.setValue(BATTERY_REFRESH_DELAY_KEY, delayMillis)

    suspend fun saveBatteryGraphHistorySeconds(seconds: Int) =
        context.dataStore.setValue(BATTERY_GRAPH_HISTORY_KEY, seconds)

    suspend fun saveHapticFeedbackEnabled(enabled: Boolean) =
        context.dataStore.setValue(HAPTIC_FEEDBACK_ENABLED_KEY, enabled)

    suspend fun saveVibrationIntensity(intensity: VibrationIntensity) =
        context.dataStore.setEnum(VIBRATION_INTENSITY_KEY, intensity)

    suspend fun saveAutoUpdateEnabled(enabled: Boolean) = context.dataStore.setValue(AUTO_UPDATE_ENABLED_KEY, enabled)
    suspend fun saveUseShizuku(enabled: Boolean) = context.dataStore.setValue(USE_SHIZUKU_KEY, enabled)
    suspend fun saveUpdatesPausedUntil(timestamp: Long) =
        context.dataStore.setValue(UPDATES_PAUSED_UNTIL_KEY, timestamp)

    suspend fun saveBlurEffectEnabled(enabled: Boolean) = context.dataStore.setValue(BLUR_EFFECT_ENABLED_KEY, enabled)
    suspend fun saveNavBarCornerRadius(radius: Int) = context.dataStore.setValue(NAV_BAR_CORNER_RADIUS_KEY, radius.coerceIn(12, 32))
    suspend fun saveNavMode(mode: NavMode) = context.dataStore.setEnum(NAV_MODE_KEY, mode)
    suspend fun saveNavType(type: NavType) = context.dataStore.setEnum(NAV_TYPE_KEY, type)
}
