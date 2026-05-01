package com.rve.systemmonitor.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.overlayDataStore: DataStore<Preferences> by preferencesDataStore(name = "overlay_settings")

class OverlayPreferences(private val context: Context) {
    companion object {
        val IS_FPS_ENABLED_KEY = booleanPreferencesKey("is_fps_enabled")
        val IS_RAM_ENABLED_KEY = booleanPreferencesKey("is_ram_enabled")
        val OVERLAY_UPDATE_INTERVAL_KEY = longPreferencesKey("overlay_update_interval")
        val OVERLAY_TEXT_SIZE_KEY = floatPreferencesKey("overlay_text_size")
        val OVERLAY_BG_OPACITY_KEY = floatPreferencesKey("overlay_bg_opacity")
        val OVERLAY_PADDING_KEY = intPreferencesKey("overlay_padding")
        val OVERLAY_TEXT_COLOR_KEY = intPreferencesKey("overlay_text_color")
        val IS_VERTICAL_LAYOUT_KEY = booleanPreferencesKey("is_vertical_layout")
        val OVERLAY_CORNER_RADIUS_KEY = intPreferencesKey("overlay_corner_radius")
    }

    val isFpsEnabledFlow: Flow<Boolean> = context.overlayDataStore.data
        .map { preferences ->
            preferences[IS_FPS_ENABLED_KEY] ?: false
        }

    val isRamEnabledFlow: Flow<Boolean> = context.overlayDataStore.data
        .map { preferences ->
            preferences[IS_RAM_ENABLED_KEY] ?: false
        }

    val overlayUpdateIntervalFlow: Flow<Long> = context.overlayDataStore.data
        .map { preferences ->
            preferences[OVERLAY_UPDATE_INTERVAL_KEY] ?: 1000L
        }

    val overlayTextSizeFlow: Flow<Float> = context.overlayDataStore.data
        .map { preferences ->
            preferences[OVERLAY_TEXT_SIZE_KEY] ?: 14f
        }

    val overlayBgOpacityFlow: Flow<Float> = context.overlayDataStore.data
        .map { preferences ->
            preferences[OVERLAY_BG_OPACITY_KEY] ?: 0.5f
        }

    val overlayPaddingFlow: Flow<Int> = context.overlayDataStore.data
        .map { preferences ->
            preferences[OVERLAY_PADDING_KEY] ?: 16
        }

    val overlayTextColorFlow: Flow<Int> = context.overlayDataStore.data
        .map { preferences ->
            preferences[OVERLAY_TEXT_COLOR_KEY] ?: android.graphics.Color.GREEN
        }

    val isVerticalLayoutFlow: Flow<Boolean> = context.overlayDataStore.data
        .map { preferences ->
            preferences[IS_VERTICAL_LAYOUT_KEY] ?: false
        }

    val overlayCornerRadiusFlow: Flow<Int> = context.overlayDataStore.data
        .map { preferences ->
            preferences[OVERLAY_CORNER_RADIUS_KEY] ?: 8
        }

    suspend fun saveIsFpsEnabled(enabled: Boolean) {
        context.overlayDataStore.edit { preferences ->
            preferences[IS_FPS_ENABLED_KEY] = enabled
        }
    }

    suspend fun saveIsRamEnabled(enabled: Boolean) {
        context.overlayDataStore.edit { preferences ->
            preferences[IS_RAM_ENABLED_KEY] = enabled
        }
    }

    suspend fun saveOverlayUpdateInterval(delayMillis: Long) {
        context.overlayDataStore.edit { preferences ->
            preferences[OVERLAY_UPDATE_INTERVAL_KEY] = delayMillis
        }
    }

    suspend fun saveOverlayTextSize(size: Float) {
        context.overlayDataStore.edit { preferences ->
            preferences[OVERLAY_TEXT_SIZE_KEY] = size
        }
    }

    suspend fun saveOverlayBgOpacity(opacity: Float) {
        context.overlayDataStore.edit { preferences ->
            preferences[OVERLAY_BG_OPACITY_KEY] = opacity
        }
    }

    suspend fun saveOverlayPadding(padding: Int) {
        context.overlayDataStore.edit { preferences ->
            preferences[OVERLAY_PADDING_KEY] = padding
        }
    }

    suspend fun saveOverlayTextColor(color: Int) {
        context.overlayDataStore.edit { preferences ->
            preferences[OVERLAY_TEXT_COLOR_KEY] = color
        }
    }

    suspend fun saveIsVerticalLayout(vertical: Boolean) {
        context.overlayDataStore.edit { preferences ->
            preferences[IS_VERTICAL_LAYOUT_KEY] = vertical
        }
    }

    suspend fun saveOverlayCornerRadius(radius: Int) {
        context.overlayDataStore.edit { preferences ->
            preferences[OVERLAY_CORNER_RADIUS_KEY] = radius
        }
    }
}
