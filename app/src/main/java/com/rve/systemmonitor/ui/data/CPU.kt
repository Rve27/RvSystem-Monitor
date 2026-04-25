package com.rve.systemmonitor.ui.data

import androidx.compose.runtime.Immutable

@Immutable
data class CPU(val manufacturer: String = "unknown", val model: String = "unknown", val cores: Int = 0)
