package com.rve.systemmonitor.ui.viewmodel

import androidx.compose.runtime.Immutable
import com.rve.systemmonitor.domain.model.RAM
import com.rve.systemmonitor.domain.model.ZRAM

@Immutable
data class MemoryUiState(
    val ram: RAM = RAM(),
    val zram: ZRAM = ZRAM(),
)
