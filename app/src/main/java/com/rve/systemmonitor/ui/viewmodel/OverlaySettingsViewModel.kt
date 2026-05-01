package com.rve.systemmonitor.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rve.systemmonitor.domain.repository.OverlayRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class OverlaySettingsViewModel @Inject constructor(private val overlayRepository: OverlayRepository) : ViewModel() {

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
            initialValue = android.graphics.Color.GREEN,
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
}
