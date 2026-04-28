package com.rve.systemmonitor.domain.model

data class Battery(
    val level: Int = 0,
    val health: String = "Unknown",
    val status: String = "Unknown",
    val technology: String = "Unknown",
    val voltage: Int = 0,
    val temperature: Float = 0f,
    val capacity: Double = 0.0
)
