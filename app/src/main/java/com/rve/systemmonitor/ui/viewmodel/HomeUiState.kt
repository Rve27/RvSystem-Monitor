package com.rve.systemmonitor.ui.viewmodel

import androidx.compose.runtime.Immutable
import com.rve.systemmonitor.domain.model.CPU
import com.rve.systemmonitor.domain.model.Device
import com.rve.systemmonitor.domain.model.Display
import com.rve.systemmonitor.domain.model.GPU
import com.rve.systemmonitor.domain.model.OS

@Immutable
data class HomeUiState(
    val device: Device = Device(),
    val os: OS = OS(),
    val display: Display = Display(),
    val cpu: CPU = CPU(),
    val gpu: GPU = GPU(),
)
