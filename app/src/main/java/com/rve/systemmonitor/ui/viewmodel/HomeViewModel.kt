package com.rve.systemmonitor.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rve.systemmonitor.domain.repository.CpuRepository
import com.rve.systemmonitor.domain.repository.HardwareRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(private val hardwareRepository: HardwareRepository, private val cpuRepository: CpuRepository) :
    ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = HomeUiState(
                device = hardwareRepository.getDeviceInfo(),
                os = hardwareRepository.getOSInfo(),
                display = hardwareRepository.getDisplayInfo(),
                cpu = cpuRepository.getCpuInfo(),
                gpu = hardwareRepository.getGpuInfo(),
            )
        }
    }
}
