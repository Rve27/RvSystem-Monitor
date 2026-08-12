package com.rve.systemmonitor.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class Display(
    val resolution: String = "unknown",
    val densityDpi: Int = 0,
    val screenSizeInches: Double = 0.0,
    val isHdrSupported: Boolean = false,
    val hdrTypes: ImmutableList<String> = persistentListOf(),
    val supportedRefreshRates: ImmutableList<Int> = persistentListOf(),

)
