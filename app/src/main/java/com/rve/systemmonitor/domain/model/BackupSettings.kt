package com.rve.systemmonitor.domain.model

import com.rve.systemmonitor.utils.AppLanguage
import com.rve.systemmonitor.utils.NavMode
import com.rve.systemmonitor.utils.NavType
import com.rve.systemmonitor.utils.SettingsPreferences
import com.rve.systemmonitor.utils.ThemeMode
import com.rve.systemmonitor.utils.VibrationIntensity
import kotlinx.serialization.Serializable

@Serializable
data class BackupSettings(
    val app: AppSettings = AppSettings(),
    val monitoring: MonitoringSettings = MonitoringSettings(),
    val overlay: OverlaySettings = OverlaySettings(),
)

@Serializable
data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val language: AppLanguage = AppLanguage.SYSTEM,
    val amoledMode: Boolean = false,
    val isSetupCompleted: Boolean = false,
    val hapticFeedbackEnabled: Boolean = true,
    val vibrationIntensity: VibrationIntensity = VibrationIntensity.LIGHT,
    val autoUpdateEnabled: Boolean = true,
    val useShizuku: Boolean = false,
    val updatesPausedUntil: Long = 0L,
    val blurEffectEnabled: Boolean = true,
    val navBarCornerRadius: Int = SettingsPreferences.DEFAULT_NAV_BAR_CORNER_RADIUS,
    val navMode: NavMode = NavMode.FLOATING,
    val navType: NavType = NavType.LEGACY,
)

@Serializable
data class MonitoringSettings(
    val cpuRefreshDelay: Long = 3000L,
    val memoryRefreshDelay: Long = 3000L,
    val gpuRefreshDelay: Long = 3000L,
    val batteryRefreshDelay: Long = 1000L,
    val batteryGraphHistorySeconds: Int = 60,
)

@Serializable
data class OverlaySettings(
    val isOverlayEnabled: Boolean = false,
    val isFpsEnabled: Boolean = false,
    val isRamEnabled: Boolean = false,
    val isRamPercentageEnabled: Boolean = false,
    val isRamGbEnabled: Boolean = false,
    val isBatteryTempEnabled: Boolean = false,
    val isCpuTempEnabled: Boolean = false,
    val updateInterval: Long = 1000L,
    val textSize: Float = 14f,
    val bgOpacity: Float = 0.5f,
    val padding: Int = 16,
    val textColor: String = "#FF00FF00", // Default Green
    val isVerticalLayout: Boolean = false,
    val cornerRadius: Int = 8,
    val position: OverlayPosition = OverlayPosition.FREE,
    val x: Int = 100,
    val y: Int = 100,
)
