package com.rve.systemmonitor.ui.viewmodel

import androidx.compose.runtime.Immutable
import com.rve.systemmonitor.ui.data.CPU
import com.rve.systemmonitor.ui.data.Device
import com.rve.systemmonitor.ui.data.Display
import com.rve.systemmonitor.ui.data.GPU
import com.rve.systemmonitor.ui.data.OS
import com.rve.systemmonitor.ui.data.RAM
import com.rve.systemmonitor.ui.data.ZRAM

@Immutable
data class HomeUiState(
    val device: Device = Device(),
    val os: OS = OS(),
    val display: Display = Display(),
    val cpu: CPU = CPU(),
    val gpu: GPU = GPU(),
    val ram: RAM = RAM(),
    val zram: ZRAM = ZRAM(),
)
