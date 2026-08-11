package com.rve.systemmonitor.utils

/** Controls whether the bottom nav bar is attached to the screen edge or floats above it. */
enum class NavMode {
    /** Full-width bar attached to the bottom edge, only top corners rounded. */
    STANDARD,
    /** Pill-shaped bar that floats above the bottom edge with all corners rounded. */
    FLOATING,
}

/** Controls the visual style of individual navigation items. */
enum class NavType {
    /** Crossfade icon swap + static label (current default). */
    LEGACY,
    /** Bouncy squircle indicator + scale animation. */
    MODERN,
}
