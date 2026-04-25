package com.rve.systemmonitor.ui.data

import androidx.compose.runtime.Immutable

@Immutable
data class OS(
    val name: String = "Android",
    val version: String = "unknown",
    val sdk: Int = 0,
    val dessertName: String = "unknown",
    val securityPatch: String = "unknown",
)
