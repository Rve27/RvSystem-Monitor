package com.rve.systemmonitor.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rve.systemmonitor.domain.model.Storage
import com.rve.systemmonitor.domain.repository.SystemInfoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class MemoryViewModel @Inject constructor(private val systemInfoRepository: SystemInfoRepository) : ViewModel() {
    private val storageInfo = MutableStateFlow(systemInfoRepository.getStorageInfo())

    val uiState = combine(
        systemInfoRepository.getMemoryInfo(),
        storageInfo,
    ) { (ram, zram), storage ->
        MemoryUiState(
            ram = ram,
            zram = zram,
            storage = storage,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(0),
        initialValue = MemoryUiState(),
    )

    fun refreshStorage() {
        storageInfo.value = systemInfoRepository.getStorageInfo()
    }
}
