package com.rve.systemmonitor.ui.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.graphics.TransformOrigin

const val TRANSITION_DURATION = 350
private val TRANSITION_EASING = FastOutSlowInEasing

fun enterTransition() = slideInHorizontally(
    animationSpec = tween(TRANSITION_DURATION, easing = TRANSITION_EASING),
    initialOffsetX = { it },
)

fun exitTransition() = slideOutHorizontally(
    animationSpec = tween(TRANSITION_DURATION, easing = TRANSITION_EASING),
    targetOffsetX = { -it / 3 },
) + fadeOut(
    animationSpec = tween(TRANSITION_DURATION, easing = TRANSITION_EASING),
)

fun popEnterTransition() = slideInHorizontally(
    animationSpec = tween(TRANSITION_DURATION, easing = TRANSITION_EASING),
    initialOffsetX = { -it / 3 },
) + scaleIn(
    animationSpec = tween(TRANSITION_DURATION, easing = TRANSITION_EASING),
    initialScale = 0.9f,
)

fun popExitTransition() = slideOutHorizontally(
    animationSpec = tween(TRANSITION_DURATION, easing = TRANSITION_EASING),
    targetOffsetX = { it },
) + scaleOut(
    animationSpec = tween(TRANSITION_DURATION, easing = TRANSITION_EASING),
    targetScale = 0.75f,
    transformOrigin = TransformOrigin(0.5f, 0.5f),
)
