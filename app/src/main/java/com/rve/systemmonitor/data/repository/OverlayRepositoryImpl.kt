package com.rve.systemmonitor.data.repository

import android.app.Application
import com.rve.systemmonitor.domain.repository.OverlayRepository
import com.rve.systemmonitor.utils.OverlayPreferences
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class OverlayRepositoryImpl @Inject constructor(application: Application) : OverlayRepository {

    private val overlayPreferences = OverlayPreferences(application)

    override val isFpsEnabled: Flow<Boolean> = overlayPreferences.isFpsEnabledFlow
    override val isRamEnabled: Flow<Boolean> = overlayPreferences.isRamEnabledFlow

    override val overlayUpdateInterval: Flow<Long> = overlayPreferences.overlayUpdateIntervalFlow

    override suspend fun setFpsEnabled(enabled: Boolean) {
        overlayPreferences.saveIsFpsEnabled(enabled)
    }

    override suspend fun setRamEnabled(enabled: Boolean) {
        overlayPreferences.saveIsRamEnabled(enabled)
    }

    override suspend fun setOverlayUpdateInterval(delayMillis: Long) {
        overlayPreferences.saveOverlayUpdateInterval(delayMillis)
    }
}
