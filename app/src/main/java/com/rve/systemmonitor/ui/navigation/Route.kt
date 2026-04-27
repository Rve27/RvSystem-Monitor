package com.rve.systemmonitor.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable
    data object Main : Route
    @Serializable
    data object Settings : Route
}
