package com.rve.systemmonitor.domain.repository

import kotlinx.coroutines.flow.Flow

interface OverlayRepository {
    val isFpsOverlayEnabled: Flow<Boolean>
    val overlayUpdateInterval: Flow<Long>
    suspend fun setFpsOverlayEnabled(enabled: Boolean)
    suspend fun setOverlayUpdateInterval(delayMillis: Long)
}
