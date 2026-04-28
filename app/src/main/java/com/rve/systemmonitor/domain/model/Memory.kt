package com.rve.systemmonitor.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class RAM(
    val total: Double = 0.0,
    val available: Double = 0.0,
    val used: Double = 0.0,
    val usedPercentage: Double = 0.0,
    val cached: Double = 0.0,
    val buffers: Double = 0.0,
    val active: Double = 0.0,
    val inactive: Double = 0.0,
    val slab: Double = 0.0,
)

@Immutable
data class ZRAM(
    val isActive: Boolean = false,
    val total: Double = 0.0,
    val available: Double = 0.0,
    val used: Double = 0.0,
    val usedPercentage: Double = 0.0,
)

@Immutable
data class Storage(
    val total: Double = 0.0,
    val available: Double = 0.0,
    val used: Double = 0.0,
    val usedPercentage: Double = 0.0,
    val mountPath: String = "",
    val fileSystemType: String = "",
)

