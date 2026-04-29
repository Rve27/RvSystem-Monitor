package com.rve.systemmonitor.domain.repository

import kotlinx.coroutines.flow.Flow

interface OverlayRepository {
    val isFpsEnabled: Flow<Boolean>
    val isRamEnabled: Flow<Boolean>
    val overlayUpdateInterval: Flow<Long>
    suspend fun setFpsEnabled(enabled: Boolean)
    suspend fun setRamEnabled(enabled: Boolean)
    suspend fun setOverlayUpdateInterval(delayMillis: Long)
}
