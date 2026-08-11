package com.rve.systemmonitor.data.repository

import android.app.Application
import android.app.LocaleManager
import android.os.LocaleList
import androidx.datastore.preferences.core.edit
import com.rve.systemmonitor.domain.model.AppSettings
import com.rve.systemmonitor.domain.model.BackupSettings
import com.rve.systemmonitor.domain.model.MonitoringSettings
import com.rve.systemmonitor.domain.model.OverlayPosition
import com.rve.systemmonitor.domain.model.OverlaySettings
import com.rve.systemmonitor.domain.repository.SettingsRepository
import com.rve.systemmonitor.utils.AppLanguage
import com.rve.systemmonitor.utils.NavMode
import com.rve.systemmonitor.utils.NavType
import com.rve.systemmonitor.utils.OverlayPreferences
import com.rve.systemmonitor.utils.SettingsPreferences
import com.rve.systemmonitor.utils.ThemeMode
import com.rve.systemmonitor.utils.VibrationIntensity
import com.rve.systemmonitor.utils.dataStore
import com.rve.systemmonitor.utils.overlayDataStore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Singleton
class SettingsRepositoryImpl @Inject constructor(private val application: Application) : SettingsRepository {

    private val settingsPreferences = SettingsPreferences(application)

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    override val themeMode: Flow<ThemeMode> = settingsPreferences.themeModeFlow

    override val language: Flow<AppLanguage> = settingsPreferences.languageFlow

    override val amoledMode: Flow<Boolean> = settingsPreferences.amoledModeFlow

    override val hapticFeedbackEnabled: Flow<Boolean> = settingsPreferences.hapticFeedbackEnabledFlow

    override val vibrationIntensity: Flow<VibrationIntensity> = settingsPreferences.vibrationIntensityFlow

    override val isSetupCompleted: Flow<Boolean> = settingsPreferences.isSetupCompletedFlow

    override val cpuRefreshDelay: Flow<Long> = settingsPreferences.cpuRefreshDelayFlow

    override val memoryRefreshDelay: Flow<Long> = settingsPreferences.memoryRefreshDelayFlow

    override val gpuRefreshDelay: Flow<Long> = settingsPreferences.gpuRefreshDelayFlow

    override val batteryRefreshDelay: Flow<Long> = settingsPreferences.batteryRefreshDelayFlow

    override val batteryGraphHistorySeconds: Flow<Int> = settingsPreferences.batteryGraphHistorySecondsFlow

    override val autoUpdateEnabled: Flow<Boolean> = settingsPreferences.autoUpdateEnabledFlow

    override val useShizuku: Flow<Boolean> = settingsPreferences.useShizukuFlow

    override val updatesPausedUntil: Flow<Long> = settingsPreferences.updatesPausedUntilFlow

    override val blurEffectEnabled: Flow<Boolean> = settingsPreferences.blurEffectEnabledFlow

    override val navBarCornerRadius: Flow<Int> = settingsPreferences.navBarCornerRadiusFlow

    override val navMode: Flow<NavMode> = settingsPreferences.navModeFlow

    override val navType: Flow<NavType> = settingsPreferences.navTypeFlow

    override suspend fun setThemeMode(mode: ThemeMode) {
        settingsPreferences.saveThemeMode(mode)
    }

    override suspend fun setLanguage(language: AppLanguage) {
        settingsPreferences.saveLanguage(language)
        application.getSystemService(LocaleManager::class.java).applicationLocales =
            language.languageTag?.let { LocaleList.forLanguageTags(it) } ?: LocaleList.getEmptyLocaleList()
    }

    override suspend fun setAmoledMode(enabled: Boolean) {
        settingsPreferences.saveAmoledMode(enabled)
    }

    override suspend fun setHapticFeedbackEnabled(enabled: Boolean) {
        settingsPreferences.saveHapticFeedbackEnabled(enabled)
    }

    override suspend fun setVibrationIntensity(intensity: VibrationIntensity) {
        settingsPreferences.saveVibrationIntensity(intensity)
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

    override suspend fun setGpuRefreshDelay(delayMillis: Long) {
        settingsPreferences.saveGpuRefreshDelay(delayMillis)
    }

    override suspend fun setBatteryRefreshDelay(delayMillis: Long) {
        settingsPreferences.saveBatteryRefreshDelay(delayMillis)
    }

    override suspend fun setBatteryGraphHistorySeconds(seconds: Int) {
        settingsPreferences.saveBatteryGraphHistorySeconds(seconds)
    }

    override suspend fun setAutoUpdateEnabled(enabled: Boolean) {
        settingsPreferences.saveAutoUpdateEnabled(enabled)
    }

    override suspend fun setUseShizuku(enabled: Boolean) {
        settingsPreferences.saveUseShizuku(enabled)
    }

    override suspend fun setUpdatesPausedUntil(timestampMillis: Long) {
        settingsPreferences.saveUpdatesPausedUntil(timestampMillis)
    }

    override suspend fun setBlurEffectEnabled(enabled: Boolean) {
        settingsPreferences.saveBlurEffectEnabled(enabled)
    }

    override suspend fun setNavBarCornerRadius(radius: Int) {
        settingsPreferences.saveNavBarCornerRadius(radius)
    }

    override suspend fun setNavMode(mode: NavMode) {
        settingsPreferences.saveNavMode(mode)
    }

    override suspend fun setNavType(type: NavType) {
        settingsPreferences.saveNavType(type)
    }

    override suspend fun exportSettings(): String {
        val appPrefs = application.dataStore.data.first()
        val overlayPrefs = application.overlayDataStore.data.first()

        val backup = BackupSettings(
            app = AppSettings(
                themeMode =
                    appPrefs[SettingsPreferences.THEME_MODE_KEY]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                        ?: ThemeMode.SYSTEM,
                language =
                    appPrefs[SettingsPreferences.LANGUAGE_KEY]?.let { runCatching { AppLanguage.valueOf(it) }.getOrNull() }
                        ?: AppLanguage.SYSTEM,
                amoledMode = appPrefs[SettingsPreferences.AMOLED_MODE_KEY] ?: false,
                isSetupCompleted = appPrefs[SettingsPreferences.IS_SETUP_COMPLETED_KEY] ?: false,
                hapticFeedbackEnabled = appPrefs[SettingsPreferences.HAPTIC_FEEDBACK_ENABLED_KEY] ?: true,
                vibrationIntensity =
                    appPrefs[SettingsPreferences.VIBRATION_INTENSITY_KEY]?.let {
                        runCatching { VibrationIntensity.valueOf(it) }.getOrNull()
                    }
                        ?: VibrationIntensity.LIGHT,
                autoUpdateEnabled = appPrefs[SettingsPreferences.AUTO_UPDATE_ENABLED_KEY] ?: true,
                useShizuku = appPrefs[SettingsPreferences.USE_SHIZUKU_KEY] ?: false,
                updatesPausedUntil = appPrefs[SettingsPreferences.UPDATES_PAUSED_UNTIL_KEY] ?: 0L,
                blurEffectEnabled = appPrefs[SettingsPreferences.BLUR_EFFECT_ENABLED_KEY] ?: true,
                navBarCornerRadius = appPrefs[SettingsPreferences.NAV_BAR_CORNER_RADIUS_KEY]
                    ?: SettingsPreferences.DEFAULT_NAV_BAR_CORNER_RADIUS,
                navMode = appPrefs[SettingsPreferences.NAV_MODE_KEY]?.let {
                    runCatching { NavMode.valueOf(it) }.getOrNull()
                } ?: NavMode.FLOATING,
                navType = appPrefs[SettingsPreferences.NAV_TYPE_KEY]?.let {
                    runCatching { NavType.valueOf(it) }.getOrNull()
                } ?: NavType.LEGACY,
            ),
            monitoring = MonitoringSettings(
                cpuRefreshDelay = appPrefs[SettingsPreferences.CPU_REFRESH_DELAY_KEY] ?: 3000L,
                memoryRefreshDelay = appPrefs[SettingsPreferences.MEMORY_REFRESH_DELAY_KEY] ?: 3000L,
                gpuRefreshDelay = appPrefs[SettingsPreferences.GPU_REFRESH_DELAY_KEY] ?: 3000L,
                batteryRefreshDelay = appPrefs[SettingsPreferences.BATTERY_REFRESH_DELAY_KEY] ?: 1000L,
                batteryGraphHistorySeconds = appPrefs[SettingsPreferences.BATTERY_GRAPH_HISTORY_KEY] ?: 60,
            ),
            overlay = OverlaySettings(
                isOverlayEnabled = overlayPrefs[OverlayPreferences.IS_OVERLAY_ENABLED_KEY] ?: false,
                isFpsEnabled = overlayPrefs[OverlayPreferences.IS_FPS_ENABLED_KEY] ?: false,
                isRamEnabled = overlayPrefs[OverlayPreferences.IS_RAM_ENABLED_KEY] ?: false,
                isRamPercentageEnabled = overlayPrefs[OverlayPreferences.IS_RAM_PERCENTAGE_ENABLED_KEY] ?: false,
                isRamGbEnabled = overlayPrefs[OverlayPreferences.IS_RAM_GB_ENABLED_KEY] ?: false,
                isBatteryTempEnabled = overlayPrefs[OverlayPreferences.IS_BATTERY_TEMP_ENABLED_KEY] ?: false,
                isCpuTempEnabled = overlayPrefs[OverlayPreferences.IS_CPU_TEMP_ENABLED_KEY] ?: false,
                updateInterval = overlayPrefs[OverlayPreferences.OVERLAY_UPDATE_INTERVAL_KEY] ?: 1000L,
                textSize = overlayPrefs[OverlayPreferences.OVERLAY_TEXT_SIZE_KEY] ?: 14f,
                bgOpacity = overlayPrefs[OverlayPreferences.OVERLAY_BG_OPACITY_KEY] ?: 0.5f,
                padding = overlayPrefs[OverlayPreferences.OVERLAY_PADDING_KEY] ?: 16,
                textColor = String.format("#%08X", overlayPrefs[OverlayPreferences.OVERLAY_TEXT_COLOR_KEY] ?: -16711936),
                isVerticalLayout = overlayPrefs[OverlayPreferences.IS_VERTICAL_LAYOUT_KEY] ?: false,
                cornerRadius = overlayPrefs[OverlayPreferences.OVERLAY_CORNER_RADIUS_KEY] ?: 8,
                position = overlayPrefs[OverlayPreferences.OVERLAY_POSITION_KEY]?.let {
                    runCatching { OverlayPosition.valueOf(it) }.getOrNull()
                } ?: OverlayPosition.FREE,
                x = overlayPrefs[OverlayPreferences.OVERLAY_X_KEY] ?: 100,
                y = overlayPrefs[OverlayPreferences.OVERLAY_Y_KEY] ?: 100,
            ),
        )
        return json.encodeToString(backup)
    }

    override suspend fun importSettings(json: String) {
        val backup = this.json.decodeFromString<BackupSettings>(json)

        application.dataStore.edit { prefs ->
            prefs[SettingsPreferences.THEME_MODE_KEY] = backup.app.themeMode.name
            prefs[SettingsPreferences.LANGUAGE_KEY] = backup.app.language.name
            prefs[SettingsPreferences.AMOLED_MODE_KEY] = backup.app.amoledMode
            prefs[SettingsPreferences.IS_SETUP_COMPLETED_KEY] = backup.app.isSetupCompleted
            prefs[SettingsPreferences.HAPTIC_FEEDBACK_ENABLED_KEY] = backup.app.hapticFeedbackEnabled
            prefs[SettingsPreferences.VIBRATION_INTENSITY_KEY] = backup.app.vibrationIntensity.name
            prefs[SettingsPreferences.AUTO_UPDATE_ENABLED_KEY] = backup.app.autoUpdateEnabled
            prefs[SettingsPreferences.USE_SHIZUKU_KEY] = backup.app.useShizuku
            prefs[SettingsPreferences.UPDATES_PAUSED_UNTIL_KEY] = backup.app.updatesPausedUntil
            prefs[SettingsPreferences.BLUR_EFFECT_ENABLED_KEY] = backup.app.blurEffectEnabled
            prefs[SettingsPreferences.NAV_BAR_CORNER_RADIUS_KEY] = backup.app.navBarCornerRadius
            prefs[SettingsPreferences.NAV_MODE_KEY] = backup.app.navMode.name
            prefs[SettingsPreferences.NAV_TYPE_KEY] = backup.app.navType.name

            prefs[SettingsPreferences.CPU_REFRESH_DELAY_KEY] = backup.monitoring.cpuRefreshDelay
            prefs[SettingsPreferences.MEMORY_REFRESH_DELAY_KEY] = backup.monitoring.memoryRefreshDelay
            prefs[SettingsPreferences.GPU_REFRESH_DELAY_KEY] = backup.monitoring.gpuRefreshDelay
            prefs[SettingsPreferences.BATTERY_REFRESH_DELAY_KEY] = backup.monitoring.batteryRefreshDelay
            prefs[SettingsPreferences.BATTERY_GRAPH_HISTORY_KEY] = backup.monitoring.batteryGraphHistorySeconds
        }

        application.getSystemService(LocaleManager::class.java).applicationLocales =
            backup.app.language.languageTag?.let { LocaleList.forLanguageTags(it) } ?: LocaleList.getEmptyLocaleList()

        application.overlayDataStore.edit { prefs ->
            prefs[OverlayPreferences.IS_OVERLAY_ENABLED_KEY] = backup.overlay.isOverlayEnabled
            prefs[OverlayPreferences.IS_FPS_ENABLED_KEY] = backup.overlay.isFpsEnabled
            prefs[OverlayPreferences.IS_RAM_ENABLED_KEY] = backup.overlay.isRamEnabled
            prefs[OverlayPreferences.IS_RAM_PERCENTAGE_ENABLED_KEY] = backup.overlay.isRamPercentageEnabled
            prefs[OverlayPreferences.IS_RAM_GB_ENABLED_KEY] = backup.overlay.isRamGbEnabled
            prefs[OverlayPreferences.IS_BATTERY_TEMP_ENABLED_KEY] = backup.overlay.isBatteryTempEnabled
            prefs[OverlayPreferences.IS_CPU_TEMP_ENABLED_KEY] = backup.overlay.isCpuTempEnabled
            prefs[OverlayPreferences.OVERLAY_UPDATE_INTERVAL_KEY] = backup.overlay.updateInterval
            prefs[OverlayPreferences.OVERLAY_TEXT_SIZE_KEY] = backup.overlay.textSize
            prefs[OverlayPreferences.OVERLAY_BG_OPACITY_KEY] = backup.overlay.bgOpacity
            prefs[OverlayPreferences.OVERLAY_PADDING_KEY] = backup.overlay.padding
            prefs[OverlayPreferences.OVERLAY_TEXT_COLOR_KEY] =
                runCatching { android.graphics.Color.parseColor(backup.overlay.textColor) }.getOrDefault(-16711936)
            prefs[OverlayPreferences.IS_VERTICAL_LAYOUT_KEY] = backup.overlay.isVerticalLayout
            prefs[OverlayPreferences.OVERLAY_CORNER_RADIUS_KEY] = backup.overlay.cornerRadius
            prefs[OverlayPreferences.OVERLAY_POSITION_KEY] = backup.overlay.position.name
            prefs[OverlayPreferences.OVERLAY_X_KEY] = backup.overlay.x
            prefs[OverlayPreferences.OVERLAY_Y_KEY] = backup.overlay.y
        }
    }
}
