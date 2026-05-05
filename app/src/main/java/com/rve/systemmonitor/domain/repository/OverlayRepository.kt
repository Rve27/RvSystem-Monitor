package com.rve.systemmonitor.domain.repository

import kotlinx.coroutines.flow.Flow

interface OverlayRepository {
    val isFpsEnabled: Flow<Boolean>
    val isRamEnabled: Flow<Boolean>
    val isRamPercentageEnabled: Flow<Boolean>
    val isRamGbEnabled: Flow<Boolean>
    val isBatteryTempEnabled: Flow<Boolean>
    val isCpuTempEnabled: Flow<Boolean>
    val overlayUpdateInterval: Flow<Long>
    val overlayTextSize: Flow<Float>
    val overlayBgOpacity: Flow<Float>
    val overlayPadding: Flow<Int>
    val overlayTextColor: Flow<Int>
    val isVerticalLayout: Flow<Boolean>
    val overlayCornerRadius: Flow<Int>

    suspend fun setFpsEnabled(enabled: Boolean)
    suspend fun setRamEnabled(enabled: Boolean)
    suspend fun setRamPercentageEnabled(enabled: Boolean)
    suspend fun setRamGbEnabled(enabled: Boolean)
    suspend fun setBatteryTempEnabled(enabled: Boolean)
    suspend fun setCpuTempEnabled(enabled: Boolean)
    suspend fun setOverlayUpdateInterval(delayMillis: Long)
    suspend fun setOverlayTextSize(size: Float)
    suspend fun setOverlayBgOpacity(opacity: Float)
    suspend fun setOverlayPadding(padding: Int)
    suspend fun setOverlayTextColor(color: Int)
    suspend fun setVerticalLayout(vertical: Boolean)
    suspend fun setOverlayCornerRadius(radius: Int)
}
