package com.rve.systemmonitor.ui.data

import androidx.compose.runtime.Immutable

@Immutable
data class Device(val manufacturer: String = "unknown", val model: String = "unknown", val device: String = "unknown")
