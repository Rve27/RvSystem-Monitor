package com.rve.systemmonitor.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rve.systemmonitor.domain.repository.SystemInfoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val systemInfoRepository: SystemInfoRepository) : ViewModel() {
    private val staticInfo = HomeUiState(
        device = systemInfoRepository.getDeviceInfo(),
        os = systemInfoRepository.getOSInfo(),
        display = systemInfoRepository.getDisplayInfo(),
        cpu = systemInfoRepository.getCpuInfo(),
        gpu = systemInfoRepository.getGpuInfo(),
    )

    val uiState = systemInfoRepository.getMemoryInfo()
        .map { (ram, zram) ->
            staticInfo.copy(
                ram = ram,
                zram = zram,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(0),
            initialValue = staticInfo,
        )
}
