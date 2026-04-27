package com.rve.systemmonitor.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class CPU(
    val manufacturer: String = "unknown",
    val model: String = "unknown",
    val cores: Int = 0,
    val coreDetails: List<CoreDetail> = emptyList(),
)

@Immutable
data class CoreDetail(
    val id: Int,
    val currentFreq: String = "0 MHz",
    val minFreq: String = "0 MHz",
    val maxFreq: String = "0 MHz",
    val governor: String = "unknown",
)
