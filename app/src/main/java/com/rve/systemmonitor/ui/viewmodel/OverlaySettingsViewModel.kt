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

    val isFpsOverlayEnabled: StateFlow<Boolean> = overlayRepository.isFpsOverlayEnabled
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

    fun setFpsOverlayEnabled(enabled: Boolean) {
        viewModelScope.launch {
            overlayRepository.setFpsOverlayEnabled(enabled)
        }
    }

    fun setOverlayUpdateInterval(delayMillis: Long) {
        viewModelScope.launch {
            overlayRepository.setOverlayUpdateInterval(delayMillis)
        }
    }
}
