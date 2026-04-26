package com.rve.systemmonitor.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rve.systemmonitor.domain.repository.SystemInfoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val systemInfoRepository: SystemInfoRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        updateStaticInfo()
        startMemoryUpdates()
    }

    private fun updateStaticInfo() {
        _uiState.update {
            it.copy(
                device = systemInfoRepository.getDeviceInfo(),
                os = systemInfoRepository.getOSInfo(),
                display = systemInfoRepository.getDisplayInfo(),
                cpu = systemInfoRepository.getCpuInfo(),
            )
        }

        viewModelScope.launch(Dispatchers.IO) {
            val gpuInfo = systemInfoRepository.getGpuInfo()
            _uiState.update {
                it.copy(gpu = gpuInfo)
            }
        }
    }

    private fun startMemoryUpdates() {
        viewModelScope.launch(Dispatchers.IO) {
            systemInfoRepository.getMemoryInfo().collect { (ram, zram) ->
                _uiState.update {
                    it.copy(
                        ram = ram,
                        zram = zram,
                    )
                }
            }
        }
    }
}
