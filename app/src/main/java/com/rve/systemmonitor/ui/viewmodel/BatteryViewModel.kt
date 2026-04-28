package com.rve.systemmonitor.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rve.systemmonitor.domain.model.Battery
import com.rve.systemmonitor.domain.repository.BatteryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class BatteryViewModel @Inject constructor(batteryRepository: BatteryRepository) : ViewModel() {
    val batteryInfo: StateFlow<Battery> = batteryRepository.getBatteryStream()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = batteryRepository.getBatteryInfo(),
        )
}
