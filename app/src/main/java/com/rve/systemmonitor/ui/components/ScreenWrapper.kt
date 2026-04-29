package com.rve.systemmonitor.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.currentStateAsState
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

/**
 * A wrapper component for screen content that provides sophisticated transition effects.
 *
 * This component tracks the navigation backstack and the screen's own lifecycle to:
 * 1. Animate corner radius when the screen is being transitioned (resumed vs paused).
 * 2. Apply a dimming effect when the screen is visible but not the primary navigation target
 *    (e.g., when a dialog or another layer is partially covering it).
 * 3. Manage background and clipping logic efficiently using [graphicsLayer].
 *
 * @param navController The [NavController] used to track backstack entries and visibility.
 * @param modifier The [Modifier] to be applied to the outer container.
 * @param content The composable content to be wrapped inside this screen.
 */
@Composable
fun ScreenWrapper(navController: NavController, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current

    var isResumed by remember { mutableStateOf(false) }

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

    val currentState = lifecycleOwner.lifecycle.currentStateAsState().value
    if (currentState.isAtLeast(Lifecycle.State.RESUMED)) {
        isResumed = true
    }

    val visibleEntries by navController.visibleEntries.collectAsStateWithLifecycle()
    val myEntry = lifecycleOwner as? NavBackStackEntry
    val myIndex = visibleEntries.indexOfFirst { it.id == myEntry?.id }
    val topIndex = visibleEntries.indexOfLast {
        it.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
    }

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val isNavigationTarget = myEntry != null && currentBackStackEntry?.id == myEntry.id

    val shouldDim = remember(visibleEntries, myEntry, myIndex, topIndex, isNavigationTarget) {
        !isNavigationTarget &&
            myIndex != -1 &&
            topIndex != -1 &&
            myIndex < topIndex &&
            myEntry?.lifecycle?.currentState != Lifecycle.State.CREATED
    }

    val targetRadius = if (isResumed) 0f else 32f
    val cornerRadius by animateFloatAsState(
        targetValue = targetRadius,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "cornerRadius",
    )

    val targetDim = if (shouldDim) 0.4f else 0f
    val dimAlpha by animateFloatAsState(
        targetValue = targetDim,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "dimAlpha",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                compositingStrategy = CompositingStrategy.Offscreen
                if (cornerRadius > 0.5f) {
                    this.shape = RoundedCornerShape(cornerRadius.dp)
                    this.clip = true
                } else {
                    this.clip = false
                }
            }
            .background(MaterialTheme.colorScheme.background),
    ) {
        content()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = dimAlpha }
                .background(Color.Black),
        )
    }
}
