package com.rve.systemmonitor.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rve.systemmonitor.domain.model.CPU
import com.rve.systemmonitor.domain.repository.SystemInfoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class CPUViewModel @Inject constructor(systemInfoRepository: SystemInfoRepository) : ViewModel() {
    val cpuInfo = systemInfoRepository.getCpuStream()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(0),
            initialValue = CPU(),
        )
}
