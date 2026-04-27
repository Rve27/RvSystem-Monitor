package com.rve.systemmonitor.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.rve.systemmonitor.domain.repository.SystemInfoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class HomeViewModel @Inject constructor(systemInfoRepository: SystemInfoRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(
        HomeUiState(
            device = systemInfoRepository.getDeviceInfo(),
            os = systemInfoRepository.getOSInfo(),
            display = systemInfoRepository.getDisplayInfo(),
            cpu = systemInfoRepository.getCpuInfo(),
            gpu = systemInfoRepository.getGpuInfo(),
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
}
