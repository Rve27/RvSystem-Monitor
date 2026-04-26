package com.rve.systemmonitor.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Device(val manufacturer: String = "unknown", val model: String = "unknown", val device: String = "unknown")
