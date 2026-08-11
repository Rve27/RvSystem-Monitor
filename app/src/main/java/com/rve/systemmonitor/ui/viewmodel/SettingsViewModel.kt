package com.rve.systemmonitor.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rve.systemmonitor.domain.repository.SettingsRepository
import com.rve.systemmonitor.shizuku.ShizukuManager
import com.rve.systemmonitor.utils.AppLanguage
import com.rve.systemmonitor.utils.NavMode
import com.rve.systemmonitor.utils.NavType
import com.rve.systemmonitor.utils.SettingsPreferences
import com.rve.systemmonitor.utils.ThemeMode
import com.rve.systemmonitor.utils.VibrationIntensity
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val shizukuManager: ShizukuManager,
) : ViewModel() {

    val isShizukuAvailable: StateFlow<Boolean> = shizukuManager.isShizukuAvailable
    val hasShizukuPermission: StateFlow<Boolean> = shizukuManager.hasPermission

    fun requestShizukuPermission() {
        shizukuManager.requestPermission()
    }

    fun refreshShizukuState() {
        shizukuManager.refreshState()
    }

    val themeMode: StateFlow<ThemeMode> = settingsRepository.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeMode.SYSTEM,
        )

    val language: StateFlow<AppLanguage> = settingsRepository.language
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppLanguage.SYSTEM,
        )

    val amoledMode: StateFlow<Boolean> = settingsRepository.amoledMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false,
        )

    val hapticFeedbackEnabled: StateFlow<Boolean> = settingsRepository.hapticFeedbackEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true,
        )

    val vibrationIntensity: StateFlow<VibrationIntensity> = settingsRepository.vibrationIntensity
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = VibrationIntensity.LIGHT,
        )

    val blurEffectEnabled: StateFlow<Boolean> = settingsRepository.blurEffectEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true,
        )

    val navBarCornerRadius: StateFlow<Int> = settingsRepository.navBarCornerRadius
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsPreferences.DEFAULT_NAV_BAR_CORNER_RADIUS,
        )

    val navMode: StateFlow<NavMode> = settingsRepository.navMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NavMode.FLOATING,
        )

    val navType: StateFlow<NavType> = settingsRepository.navType
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NavType.LEGACY,
        )

    val cpuRefreshDelay: StateFlow<Long> = settingsRepository.cpuRefreshDelay
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 3000L,
        )

    val memoryRefreshDelay: StateFlow<Long> = settingsRepository.memoryRefreshDelay
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 3000L,
        )

    val gpuRefreshDelay: StateFlow<Long> = settingsRepository.gpuRefreshDelay
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 3000L,
        )

    val batteryRefreshDelay: StateFlow<Long> = settingsRepository.batteryRefreshDelay
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 1000L,
        )

    val batteryGraphHistorySeconds: StateFlow<Int> = settingsRepository.batteryGraphHistorySeconds
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 60,
        )

    val autoUpdateEnabled: StateFlow<Boolean> = settingsRepository.autoUpdateEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true,
        )

    val useShizuku: StateFlow<Boolean> = settingsRepository.useShizuku
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false,
        )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
        }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch {
            settingsRepository.setLanguage(language)
        }
    }

    fun setAmoledMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAmoledMode(enabled)
        }
    }

    fun setHapticFeedbackEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setHapticFeedbackEnabled(enabled)
        }
    }

    fun setVibrationIntensity(intensity: VibrationIntensity) {
        viewModelScope.launch {
            settingsRepository.setVibrationIntensity(intensity)
        }
    }

    fun setBlurEffectEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setBlurEffectEnabled(enabled)
        }
    }

    fun setNavBarCornerRadius(radius: Int) {
        viewModelScope.launch {
            settingsRepository.setNavBarCornerRadius(radius)
        }
    }

    fun setNavMode(mode: NavMode) {
        viewModelScope.launch {
            settingsRepository.setNavMode(mode)
            settingsRepository.setNavType(if (mode == NavMode.STANDARD) NavType.MODERN else NavType.LEGACY)
        }
    }

    fun setNavType(type: NavType) {
        viewModelScope.launch {
            settingsRepository.setNavType(type)
        }
    }

    fun setCpuRefreshDelay(delayMillis: Long) {
        viewModelScope.launch {
            settingsRepository.setCpuRefreshDelay(delayMillis)
        }
    }

    fun setMemoryRefreshDelay(delayMillis: Long) {
        viewModelScope.launch {
            settingsRepository.setMemoryRefreshDelay(delayMillis)
        }
    }

    fun setGpuRefreshDelay(delayMillis: Long) {
        viewModelScope.launch {
            settingsRepository.setGpuRefreshDelay(delayMillis)
        }
    }

    fun setBatteryRefreshDelay(delayMillis: Long) {
        viewModelScope.launch {
            settingsRepository.setBatteryRefreshDelay(delayMillis)
        }
    }

    fun setBatteryGraphHistorySeconds(seconds: Int) {
        viewModelScope.launch {
            settingsRepository.setBatteryGraphHistorySeconds(seconds)
        }
    }

    fun setAutoUpdateEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAutoUpdateEnabled(enabled)
        }
    }

    fun setUseShizuku(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setUseShizuku(enabled)
        }
    }

    fun exportSettingsToFile(context: Context, uri: Uri, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = withContext(Dispatchers.IO) {
                runCatching {
                    val json = settingsRepository.exportSettings()
                    context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(json) }
                    true
                }.getOrDefault(false)
            }
            onResult(success)
        }
    }

    fun importSettingsFromFile(context: Context, uri: Uri, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = withContext(Dispatchers.IO) {
                runCatching {
                    val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                    if (json != null) {
                        settingsRepository.importSettings(json)
                        true
                    } else {
                        false
                    }
                }.getOrDefault(false)
            }
            onResult(success)
        }
    }
}
