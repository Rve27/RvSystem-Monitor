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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.stateIn
@OptIn(FlowPreview::class)
@HiltViewModel
class BatteryViewModel @Inject constructor(private val batteryRepository: BatteryRepository, settingsRepository: SettingsRepository) :
    ViewModel() {
    private val cachedBatteryStatic = batteryRepository.getBatteryInfo()

    private val batteryStream = batteryRepository.getBatteryStream()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = batteryRepository.getBatteryInfo(),
        )

    private val batteryStatic = flowOf(cachedBatteryStatic)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Battery(),
        )

    val batteryInfo: StateFlow<Battery> = combine(
        batteryStream,
        batteryStatic,
    ) { stream, static ->
        stream.copy(
            health = static.health,
            technology = static.technology,
            capacity = static.capacity,
            maxCapacity = static.maxCapacity,
            healthPercentage = static.healthPercentage,
            cycleCount = static.cycleCount,
            deepSleep = static.deepSleep,
        )
    }.stateIn(
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

    private val _historyList = mutableListOf<BatteryDataPoint>()

    val batteryHistory: StateFlow<List<BatteryDataPoint>> = batteryInfo
        .sample(1000)
        .map { info ->
            _historyList.add(BatteryDataPoint(info.current, info.status))
            val maxHistory = graphHistorySeconds.value
            if (_historyList.size > maxHistory) {
                _historyList.subList(0, _historyList.size - maxHistory).clear()
            }
            _historyList.toList()
        }
        .combine(graphHistorySeconds) { history, maxHistory ->
            if (history.size > maxHistory) history.takeLast(maxHistory) else history
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = _historyList.toList(),
        )

    private var _hasAnimated = false
    val hasAnimated: Boolean get() = _hasAnimated

    fun markAsAnimated() {
        _hasAnimated = true
    }
}
