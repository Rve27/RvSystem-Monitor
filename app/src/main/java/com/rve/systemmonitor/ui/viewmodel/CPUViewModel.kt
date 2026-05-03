package com.rve.systemmonitor.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rve.systemmonitor.domain.model.CPU
import com.rve.systemmonitor.domain.repository.CpuRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class CPUViewModel @Inject constructor(cpuRepository: CpuRepository) : ViewModel() {
    private val cachedCpuStatic = cpuRepository.getCpuInfo()

    private val cpuStream = cpuRepository.getCpuStream()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CPU(),
        )

    private val cpuStatic = flowOf(cachedCpuStatic)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CPU(),
        )

    val cpuInfo = combine(cpuStream, cpuStatic) { stream, static ->
        stream.copy(
            manufacturer = static.manufacturer,
            model = static.model,
            cores = static.cores,
            hardware = static.hardware,
            board = static.board,
            architecture = static.architecture,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CPU(),
    )
}
