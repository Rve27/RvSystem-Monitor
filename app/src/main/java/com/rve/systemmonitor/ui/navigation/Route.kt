package com.rve.systemmonitor.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface Route : NavKey {
    @Serializable
    data object Main : Route
    @Serializable
    data object Settings : Route
    @Serializable
    data object AppearanceSettings : Route
    @Serializable
    data object MonitoringSettings : Route
    @Serializable
    data object OverlaySettings : Route
    @Serializable
    data object AutoToggleSettings : Route

    @Serializable
    data object AppSettings : Route

    @Serializable
    data object RustLibrary : Route

    @Serializable
    data object About : Route
    @Serializable
    data object GPU : Route
    @Serializable
    data class Setup(val isTestFlow: Boolean = false) : Route
}
