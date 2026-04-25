package com.rve.systemmonitor.ui.data

import androidx.compose.runtime.Immutable

@Immutable
data class GPU(val renderer: String = "unknown", val vendor: String = "unknown", val glesVersion: String = "unknown")
