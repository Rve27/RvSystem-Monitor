@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.rve.systemmonitor.ui.theme

import android.app.Activity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun RvSystemMonitorTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val targetColorScheme = if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    val colorScheme = animateColorScheme(targetColorScheme)

    val view = LocalView.current
    SideEffect {
        val window = (view.context as Activity).window
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
    }

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        motionScheme = MotionScheme.expressive(),
        typography = appTypography,
        content = content,
    )
}

@Composable
private fun animateColorScheme(targetColorScheme: ColorScheme): ColorScheme {
    val animationSpec = tween<Color>(durationMillis = 350)

    @Composable
    fun animateColor(target: Color) = animateColorAsState(target, animationSpec, label = "color").value

    return targetColorScheme.copy(
        primary = animateColor(targetColorScheme.primary),
        onPrimary = animateColor(targetColorScheme.onPrimary),
        primaryContainer = animateColor(targetColorScheme.primaryContainer),
        onPrimaryContainer = animateColor(targetColorScheme.onPrimaryContainer),
        inversePrimary = animateColor(targetColorScheme.inversePrimary),
        secondary = animateColor(targetColorScheme.secondary),
        onSecondary = animateColor(targetColorScheme.onSecondary),
        secondaryContainer = animateColor(targetColorScheme.secondaryContainer),
        onSecondaryContainer = animateColor(targetColorScheme.onSecondaryContainer),
        tertiary = animateColor(targetColorScheme.tertiary),
        onTertiary = animateColor(targetColorScheme.onTertiary),
        tertiaryContainer = animateColor(targetColorScheme.tertiaryContainer),
        onTertiaryContainer = animateColor(targetColorScheme.onTertiaryContainer),
        background = animateColor(targetColorScheme.background),
        onBackground = animateColor(targetColorScheme.onBackground),
        surface = animateColor(targetColorScheme.surface),
        onSurface = animateColor(targetColorScheme.onSurface),
        surfaceVariant = animateColor(targetColorScheme.surfaceVariant),
        onSurfaceVariant = animateColor(targetColorScheme.onSurfaceVariant),
        surfaceTint = animateColor(targetColorScheme.surfaceTint),
        inverseSurface = animateColor(targetColorScheme.inverseSurface),
        inverseOnSurface = animateColor(targetColorScheme.inverseOnSurface),
        error = animateColor(targetColorScheme.error),
        onError = animateColor(targetColorScheme.onError),
        errorContainer = animateColor(targetColorScheme.errorContainer),
        onErrorContainer = animateColor(targetColorScheme.onErrorContainer),
        outline = animateColor(targetColorScheme.outline),
        outlineVariant = animateColor(targetColorScheme.outlineVariant),
        scrim = animateColor(targetColorScheme.scrim),
        surfaceBright = animateColor(targetColorScheme.surfaceBright),
        surfaceContainer = animateColor(targetColorScheme.surfaceContainer),
        surfaceContainerHigh = animateColor(targetColorScheme.surfaceContainerHigh),
        surfaceContainerHighest = animateColor(targetColorScheme.surfaceContainerHighest),
        surfaceContainerLow = animateColor(targetColorScheme.surfaceContainerLow),
        surfaceContainerLowest = animateColor(targetColorScheme.surfaceContainerLowest),
        surfaceDim = animateColor(targetColorScheme.surfaceDim),
    )
}
