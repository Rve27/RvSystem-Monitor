package com.rve.systemmonitor.domain.model

import androidx.compose.runtime.Immutable
import java.util.Locale
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class CPU(
    val manufacturer: String = "unknown",
    val model: String = "unknown",
    val cores: Int = 0,
    val hardware: String = "unknown",
    val board: String = "unknown",
    val architecture: String = "unknown",
    val abi: String = "unknown",
    val temperature: Double = 0.0,
    val load: Double = 0.0,
    val isLoadAvailable: Boolean = false,
    val coreDetails: ImmutableList<CoreDetail> = persistentListOf(),
)

@Immutable
data class CoreDetail(
    val id: Int,
    val currentFreqKhz: Long = 0,
    val minFreqKhz: Long = 0,
    val maxFreqKhz: Long = 0,
    val governor: String = "unknown",
    val temperature: Double = 0.0,
    val load: Double = 0.0,
) {
    // Computed once upon initialization to prevent allocations during Compose recomposition
    val currentFreq: String = formatFrequency(currentFreqKhz)
    val minFreq: String = formatFrequency(minFreqKhz)
    val maxFreq: String = formatFrequency(maxFreqKhz)

    private fun formatFrequency(freqKhz: Long): String {
        return String.format(Locale.US, "%.2f GHz", freqKhz / 1_000_000.0)
    }
}
