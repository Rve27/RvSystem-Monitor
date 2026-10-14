package com.rve.systemmonitor.ui.viewmodel

import android.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rve.systemmonitor.domain.model.OverlayPosition
import com.rve.systemmonitor.domain.repository.OverlayRepository
import com.rve.systemmonitor.domain.repository.SettingsRepository
import com.rve.systemmonitor.privileged.PrivilegedAccess
import com.rve.systemmonitor.shizuku.ShizukuManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class OverlaySettingsViewModel @Inject constructor(
    private val overlayRepository: OverlayRepository,
    private val settingsRepository: SettingsRepository,
    private val shizukuManager: ShizukuManager,
    privilegedAccess: PrivilegedAccess,
) : ViewModel() {

    val isShizukuAvailable: StateFlow<Boolean> = shizukuManager.isShizukuAvailable
    val hasShizukuPermission: StateFlow<Boolean> = shizukuManager.hasPermission
    val isPrivilegedReady: StateFlow<Boolean> = privilegedAccess.isReady
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false,
        )

    val isOverlayEnabled: StateFlow<Boolean> = overlayRepository.isOverlayEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false,
        )

    val isFpsEnabled: StateFlow<Boolean> = overlayRepository.isFpsEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false,
        )

    val isRamEnabled: StateFlow<Boolean> = overlayRepository.isRamEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false,
        )

    val isRamPercentageEnabled: StateFlow<Boolean> = overlayRepository.isRamPercentageEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false,
        )

    val isRamGbEnabled: StateFlow<Boolean> = overlayRepository.isRamGbEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false,
        )

    val isBatteryTempEnabled: StateFlow<Boolean> = overlayRepository.isBatteryTempEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false,
        )

    val isCpuTempEnabled: StateFlow<Boolean> = overlayRepository.isCpuTempEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false,
        )

    val overlayUpdateInterval: StateFlow<Long> = overlayRepository.overlayUpdateInterval
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 1000L,
        )

    val overlayTextSize: StateFlow<Float> = overlayRepository.overlayTextSize
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 14f,
        )

    val overlayBgOpacity: StateFlow<Float> = overlayRepository.overlayBgOpacity
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.5f,
        )

    val overlayPadding: StateFlow<Int> = overlayRepository.overlayPadding
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 16,
        )

    val overlayTextColor: StateFlow<Int> = overlayRepository.overlayTextColor
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Color.GREEN,
        )

    val isVerticalLayout: StateFlow<Boolean> = overlayRepository.isVerticalLayout
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false,
        )

    val overlayCornerRadius: StateFlow<Int> = overlayRepository.overlayCornerRadius
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 8,
        )

    val overlayPosition: StateFlow<OverlayPosition> = overlayRepository.overlayPosition
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = OverlayPosition.FREE,
        )

    val isAutoToggleEnabled: StateFlow<Boolean> = overlayRepository.isAutoToggleEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false,
        )

    val autoToggleApps: StateFlow<Set<String>> = overlayRepository.autoToggleApps
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet(),
        )

    fun setOverlayEnabled(enabled: Boolean) {
        viewModelScope.launch {
            if (enabled) {
                overlayRepository.setAutoToggleEnabled(false)
            }
            overlayRepository.setOverlayEnabled(enabled)
        }
    }

    fun setFpsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            overlayRepository.setFpsEnabled(enabled)
        }
    }

    fun setRamEnabled(enabled: Boolean) {
        viewModelScope.launch {
            overlayRepository.setRamEnabled(enabled)
        }
    }

    fun setRamPercentageEnabled(enabled: Boolean) {
        viewModelScope.launch {
            overlayRepository.setRamPercentageEnabled(enabled)
        }
    }

    fun setRamGbEnabled(enabled: Boolean) {
        viewModelScope.launch {
            overlayRepository.setRamGbEnabled(enabled)
        }
    }

    fun setBatteryTempEnabled(enabled: Boolean) {
        viewModelScope.launch {
            overlayRepository.setBatteryTempEnabled(enabled)
        }
    }

    fun setCpuTempEnabled(enabled: Boolean) {
        viewModelScope.launch {
            overlayRepository.setCpuTempEnabled(enabled)
        }
    }

    fun setOverlayUpdateInterval(delayMillis: Long) {
        viewModelScope.launch {
            overlayRepository.setOverlayUpdateInterval(delayMillis)
        }
    }

    fun setOverlayTextSize(size: Float) {
        viewModelScope.launch {
            overlayRepository.setOverlayTextSize(size)
        }
    }

    fun setOverlayBgOpacity(opacity: Float) {
        viewModelScope.launch {
            overlayRepository.setOverlayBgOpacity(opacity)
        }
    }

    fun setOverlayPadding(padding: Int) {
        viewModelScope.launch {
            overlayRepository.setOverlayPadding(padding)
        }
    }

    fun setOverlayTextColor(color: Int) {
        viewModelScope.launch {
            overlayRepository.setOverlayTextColor(color)
        }
    }

    fun setVerticalLayout(vertical: Boolean) {
        viewModelScope.launch {
            overlayRepository.setVerticalLayout(vertical)
        }
    }

    fun setOverlayCornerRadius(radius: Int) {
        viewModelScope.launch {
            overlayRepository.setOverlayCornerRadius(radius)
        }
    }

    fun setOverlayPosition(position: OverlayPosition) {
        viewModelScope.launch {
            overlayRepository.setOverlayPosition(position)
        }
    }

    fun disableAll() {
        viewModelScope.launch {
            overlayRepository.setOverlayEnabled(false)
            overlayRepository.setFpsEnabled(false)
            overlayRepository.setRamEnabled(false)
            overlayRepository.setRamPercentageEnabled(false)
            overlayRepository.setRamGbEnabled(false)
            overlayRepository.setBatteryTempEnabled(false)
            overlayRepository.setCpuTempEnabled(false)
            overlayRepository.setAutoToggleEnabled(false)
        }
    }

    fun setAutoToggleEnabled(enabled: Boolean) {
        viewModelScope.launch {
            if (enabled) {
                overlayRepository.setOverlayEnabled(false)
            }
            overlayRepository.setAutoToggleEnabled(enabled)
        }
    }

    fun setAutoToggleApps(apps: Set<String>) {
        viewModelScope.launch {
            overlayRepository.setAutoToggleApps(apps)
        }
    }
}
