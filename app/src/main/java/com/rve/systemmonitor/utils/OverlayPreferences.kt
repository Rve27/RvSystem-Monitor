package com.rve.systemmonitor.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.overlayDataStore: DataStore<Preferences> by preferencesDataStore(name = "overlay_settings")

class OverlayPreferences(private val context: Context) {
    companion object {
        val IS_FPS_OVERLAY_ENABLED_KEY = booleanPreferencesKey("is_fps_overlay_enabled")
        val OVERLAY_UPDATE_INTERVAL_KEY = longPreferencesKey("overlay_update_interval")
    }

    val isFpsOverlayEnabledFlow: Flow<Boolean> = context.overlayDataStore.data
        .map { preferences ->
            preferences[IS_FPS_OVERLAY_ENABLED_KEY] ?: false
        }

    val overlayUpdateIntervalFlow: Flow<Long> = context.overlayDataStore.data
        .map { preferences ->
            preferences[OVERLAY_UPDATE_INTERVAL_KEY] ?: 1000L
        }

    suspend fun saveIsFpsOverlayEnabled(enabled: Boolean) {
        context.overlayDataStore.edit { preferences ->
            preferences[IS_FPS_OVERLAY_ENABLED_KEY] = enabled
        }
    }

    suspend fun saveOverlayUpdateInterval(delayMillis: Long) {
        context.overlayDataStore.edit { preferences ->
            preferences[OVERLAY_UPDATE_INTERVAL_KEY] = delayMillis
        }
    }
}
