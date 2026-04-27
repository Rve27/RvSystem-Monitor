package com.rve.systemmonitor.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rve.systemmonitor.domain.repository.SystemInfoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class MemoryViewModel @Inject constructor(systemInfoRepository: SystemInfoRepository) : ViewModel() {
    val uiState = systemInfoRepository.getMemoryInfo()
        .map { (ram, zram) ->
            MemoryUiState(
                ram = ram,
                zram = zram,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(0),
            initialValue = MemoryUiState(),
        )
}
