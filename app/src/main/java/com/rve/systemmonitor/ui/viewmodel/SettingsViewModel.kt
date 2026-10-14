package com.rve.systemmonitor.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rve.systemmonitor.domain.repository.SettingsRepository
import com.rve.systemmonitor.privileged.PrivilegedAccess
import com.rve.systemmonitor.shizuku.ShizukuManager
import com.rve.systemmonitor.utils.AppLanguage
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
    private val privilegedAccess: PrivilegedAccess,
) : ViewModel() {

    val isShizukuAvailable: StateFlow<Boolean> = shizukuManager.isShizukuAvailable
    val hasShizukuPermission: StateFlow<Boolean> = shizukuManager.hasPermission

    val isRootGranted: StateFlow<Boolean> = privilegedAccess.isRootGranted

    fun requestRoot() {
        viewModelScope.launch { privilegedAccess.requestRoot() }
    }

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
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val navBarBlurEffectEnabled: StateFlow<Boolean> = settingsRepository.navBarBlurEffectEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val navBarIsFloating: StateFlow<Boolean> = settingsRepository.navBarIsFloating
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

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

    val useRoot: StateFlow<Boolean> = settingsRepository.useRoot
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false,
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

    fun setNavBarBlurEffectEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNavBarBlurEffectEnabled(enabled)
            if (enabled) {
                settingsRepository.setNavBarIsFloating(true)
            }
        }
    }

    fun setNavBarIsFloating(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNavBarIsFloating(enabled)
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
            if (enabled) settingsRepository.setUseRoot(false)
            settingsRepository.setUseShizuku(enabled)
        }
    }

    fun setUseRoot(enabled: Boolean) {
        viewModelScope.launch {
            if (enabled) settingsRepository.setUseShizuku(false)
            settingsRepository.setUseRoot(enabled)
            if (enabled) privilegedAccess.requestRoot()
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
