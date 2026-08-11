@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.rve.systemmonitor.ui.theme

import android.app.Activity
import androidx.compose.animation.animateColorAsState
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.rve.systemmonitor.ui.components.haptic.LocalHapticEnabled
import com.rve.systemmonitor.ui.components.haptic.LocalVibrationIntensity
import com.rve.systemmonitor.utils.NavMode
import com.rve.systemmonitor.utils.NavType
import com.rve.systemmonitor.utils.SettingsPreferences
import com.rve.systemmonitor.utils.VibrationIntensity

val LocalBlurEffectEnabled = compositionLocalOf { true }
val LocalNavBarCornerRadius = compositionLocalOf { SettingsPreferences.DEFAULT_NAV_BAR_CORNER_RADIUS.dp }
val LocalNavMode = compositionLocalOf { NavMode.FLOATING }
val LocalNavType = compositionLocalOf { NavType.LEGACY }

@Composable
fun RvSystemMonitorTheme(
    darkTheme: Boolean,
    amoledMode: Boolean = false,
    hapticEnabled: Boolean = true,
    vibrationIntensity: VibrationIntensity = VibrationIntensity.LIGHT,
    blurEffectEnabled: Boolean = true,
    navBarCornerRadius: Dp = SettingsPreferences.DEFAULT_NAV_BAR_CORNER_RADIUS.dp,
    navMode: NavMode = NavMode.FLOATING,
    navType: NavType = NavType.LEGACY,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val targetColorScheme = if (darkTheme) {
        val baseColorScheme = dynamicDarkColorScheme(context)
        if (amoledMode) {
            baseColorScheme.copy(
                background = Color.Black,
                surface = Color.Black,
            )
        } else {
            baseColorScheme
        }
    } else {
        dynamicLightColorScheme(context)
    }
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
        content = {
            CompositionLocalProvider(
                LocalHapticEnabled provides hapticEnabled,
                LocalVibrationIntensity provides vibrationIntensity,
                LocalBlurEffectEnabled provides blurEffectEnabled,
                LocalNavBarCornerRadius provides navBarCornerRadius,
                LocalNavMode provides navMode,
                LocalNavType provides navType,
            ) {
                content()
            }
        },
    )
}

@Composable
private fun animateColorScheme(targetColorScheme: ColorScheme): ColorScheme {
    @Composable
    fun animateColor(target: Color) = animateColorAsState(
        targetValue = target,
        animationSpec = MaterialTheme.motionScheme.slowEffectsSpec(),
        label = "color",
    ).value

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
