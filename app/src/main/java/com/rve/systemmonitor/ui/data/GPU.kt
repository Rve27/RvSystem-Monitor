package com.rve.systemmonitor.ui.data

data class GPU(
    val renderer: String = "unknown",
    val vendor: String = "unknown",
    val glesVersion: String = "unknown"
)