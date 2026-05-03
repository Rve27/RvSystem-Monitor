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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.scan
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

    val batteryHistory: StateFlow<List<BatteryDataPoint>> = batteryInfo
        .sample(1000)
        .scan(emptyList<BatteryDataPoint>()) { accumulator, info ->
            val newList = accumulator.toMutableList()
            newList.add(BatteryDataPoint(info.current, info.status))
            val maxHistory = graphHistorySeconds.value
            if (newList.size > maxHistory) {
                newList.takeLast(maxHistory)
            } else {
                newList
            }
        }
        .combine(graphHistorySeconds) { history, maxHistory ->
            if (history.size > maxHistory) history.takeLast(maxHistory) else history
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    private var _hasAnimated = false
    val hasAnimated: Boolean get() = _hasAnimated

    fun markAsAnimated() {
        _hasAnimated = true
    }
}
