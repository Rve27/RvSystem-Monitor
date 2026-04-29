package com.rve.systemmonitor.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rve.systemmonitor.domain.repository.HardwareRepository
import com.rve.systemmonitor.domain.repository.MemoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class MemoryViewModel @Inject constructor(memoryRepository: MemoryRepository, private val hardwareRepository: HardwareRepository) :
    ViewModel() {
    private val storageInfo = MutableStateFlow(hardwareRepository.getStorageInfo())

    val uiState = combine(
        memoryRepository.getMemoryInfo(),
        storageInfo,
    ) { (ram, zram), storage ->
        MemoryUiState(
            ram = ram,
            zram = zram,
            storage = storage,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MemoryUiState(),
    )

    fun refreshStorage() {
        storageInfo.value = hardwareRepository.getStorageInfo()
    }
}
