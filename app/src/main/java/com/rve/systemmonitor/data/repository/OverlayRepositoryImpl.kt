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
    override val overlayTextSize: Flow<Float> = overlayPreferences.overlayTextSizeFlow
    override val overlayBgOpacity: Flow<Float> = overlayPreferences.overlayBgOpacityFlow
    override val overlayPadding: Flow<Int> = overlayPreferences.overlayPaddingFlow
    override val overlayTextColor: Flow<Int> = overlayPreferences.overlayTextColorFlow
    override val isVerticalLayout: Flow<Boolean> = overlayPreferences.isVerticalLayoutFlow
    override val overlayCornerRadius: Flow<Int> = overlayPreferences.overlayCornerRadiusFlow

    override suspend fun setFpsEnabled(enabled: Boolean) {
        overlayPreferences.saveIsFpsEnabled(enabled)
    }

    override suspend fun setRamEnabled(enabled: Boolean) {
        overlayPreferences.saveIsRamEnabled(enabled)
    }

    override suspend fun setOverlayUpdateInterval(delayMillis: Long) {
        overlayPreferences.saveOverlayUpdateInterval(delayMillis)
    }

    override suspend fun setOverlayTextSize(size: Float) {
        overlayPreferences.saveOverlayTextSize(size)
    }

    override suspend fun setOverlayBgOpacity(opacity: Float) {
        overlayPreferences.saveOverlayBgOpacity(opacity)
    }

    override suspend fun setOverlayPadding(padding: Int) {
        overlayPreferences.saveOverlayPadding(padding)
    }

    override suspend fun setOverlayTextColor(color: Int) {
        overlayPreferences.saveOverlayTextColor(color)
    }

    override suspend fun setVerticalLayout(vertical: Boolean) {
        overlayPreferences.saveIsVerticalLayout(vertical)
    }

    override suspend fun setOverlayCornerRadius(radius: Int) {
        overlayPreferences.saveOverlayCornerRadius(radius)
    }
}
