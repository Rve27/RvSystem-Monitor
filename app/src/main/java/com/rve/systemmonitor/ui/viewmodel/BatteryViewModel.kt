package com.rve.systemmonitor.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rve.systemmonitor.domain.model.Battery
import com.rve.systemmonitor.domain.model.BatteryDataPoint
import com.rve.systemmonitor.domain.repository.BatteryRepository
import com.rve.systemmonitor.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.stateIn

@OptIn(FlowPreview::class)
@HiltViewModel
class BatteryViewModel @Inject constructor(batteryRepository: BatteryRepository, settingsRepository: SettingsRepository) : ViewModel() {
    val batteryInfo: StateFlow<Battery> = batteryRepository.getBatteryStream()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = batteryRepository.getBatteryInfo(),
        )

    val graphHistorySeconds: StateFlow<Int> = settingsRepository.batteryGraphHistorySeconds
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 60,
        )

    private val _batteryHistory = MutableStateFlow<List<BatteryDataPoint>>(emptyList())
    val batteryHistory: StateFlow<List<BatteryDataPoint>> = _batteryHistory.asStateFlow()

    private var _hasAnimated = false
    val hasAnimated: Boolean get() = _hasAnimated

    fun markAsAnimated() {
        _hasAnimated = true
    }

    init {
        batteryInfo
            .sample(1000)
            .onEach { info ->
                val currentList = _batteryHistory.value.toMutableList()
                currentList.add(BatteryDataPoint(info.current, info.status))
                val maxHistory = graphHistorySeconds.value
                if (currentList.size > maxHistory) {
                    _batteryHistory.value = currentList.takeLast(maxHistory)
                } else {
                    _batteryHistory.value = currentList
                }
            }.launchIn(viewModelScope)
    }
}
