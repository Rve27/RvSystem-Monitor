package com.rve.systemmonitor.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import androidx.navigation3.runtime.NavKey
import com.rve.systemmonitor.ui.navigation.isMainRootRoute
import com.rve.systemmonitor.ui.theme.LocalBlurEffectEnabled

@Composable
fun ScreenWrapper(backStack: List<NavKey>, myRoute: NavKey, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current

    // Lifecycle State
    val initialCurrentState = lifecycleOwner.lifecycle.currentStateAsState().value
    var isResumed by remember { mutableStateOf(initialCurrentState.isAtLeast(Lifecycle.State.RESUMED)) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isResumed = true
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                isResumed = false
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val isMainRootScreen = isMainRootRoute(myRoute)
    val hasVisibleNonMainRootScreen = backStack.any { route -> !isMainRootRoute(route) }
    val shouldRunDepthEffects = !isMainRootScreen || hasVisibleNonMainRootScreen

    // Dim/Blur Logic
    val previousRoute = backStack.getOrNull(backStack.lastIndex - 1)
    val shouldDim = myRoute == previousRoute

    val blurEffectEnabled = LocalBlurEffectEnabled.current
    val disableBlurAllOver = !blurEffectEnabled

    // Declarative Animations
    val targetRadius = if (shouldRunDepthEffects && !isResumed) 32f else 0f
    val fallbackCornerRadius = remember { Animatable(targetRadius) }
    LaunchedEffect(targetRadius) {
        fallbackCornerRadius.animateTo(targetRadius, animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing))
    }
    val animatedCornerRadius = fallbackCornerRadius.value

    // Dim: If strictly behind Top -> 0.4f (or 0.75f if blur is disabled). Else -> 0f.
    val targetDim = if (shouldRunDepthEffects && shouldDim) {
        if (disableBlurAllOver) 0.75f else 0.4f
    } else {
        0f
    }
    val fallbackDimAlpha = remember { Animatable(targetDim) }
    LaunchedEffect(targetDim) {
        fallbackDimAlpha.animateTo(targetDim, animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing))
    }
    val animatedDimAlpha = fallbackDimAlpha.value

    // Blur: If strictly behind Top -> 24dp. Else -> 0dp. Disabled if disableBlurAllOver is true.
    val targetBlur = if (shouldRunDepthEffects && shouldDim && !disableBlurAllOver) 24f else 0f
    val fallbackBlurRadius = remember { Animatable(targetBlur) }
    LaunchedEffect(targetBlur) {
        fallbackBlurRadius.animateTo(targetBlur, animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing))
    }
    val animatedBlurRadius = fallbackBlurRadius.value.dp

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                compositingStrategy = if (shouldRunDepthEffects) {
                    CompositingStrategy.Offscreen
                } else {
                    CompositingStrategy.Auto
                }
                if (shouldRunDepthEffects && animatedCornerRadius > 0.5f) {
                    this.shape = RoundedCornerShape(animatedCornerRadius.dp)
                    this.clip = true
                } else {
                    this.clip = false
                }
            }
            .blur(radius = if (shouldRunDepthEffects) animatedBlurRadius else 0.dp)
            .background(MaterialTheme.colorScheme.background),
    ) {
        content()

        // Dim Layer Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = animatedDimAlpha }
                .background(Color.Black),
        )
    }
}
