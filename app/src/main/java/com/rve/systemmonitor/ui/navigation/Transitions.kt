package com.rve.systemmonitor.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.IntOffset

// Curva Emphasized oficial de Material 3 (la que usan los Pixel contemporáneos)
val M3EmphasizedEasing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
const val AOSP_TRANSITION_DURATION = 350

// Equivale a sud_slide_next_in
fun aospSharedAxisEnter(): EnterTransition {
    return slideInHorizontally(
        initialOffsetX = { it / 3 }, // Arranca desde 1/3 de la pantalla hacia la derecha
        animationSpec = tween(durationMillis = AOSP_TRANSITION_DURATION, easing = M3EmphasizedEasing),
    ) + fadeIn(
        animationSpec = tween(durationMillis = AOSP_TRANSITION_DURATION, easing = M3EmphasizedEasing),
    )
}

// Equivale a sud_slide_next_out
fun aospSharedAxisExit(): ExitTransition {
    return slideOutHorizontally(
        targetOffsetX = { -it / 3 }, // Se desplaza 1/3 de la pantalla hacia la izquierda
        animationSpec = tween(durationMillis = AOSP_TRANSITION_DURATION, easing = M3EmphasizedEasing),
    ) + scaleOut(
        targetScale = 0.92f, // Escala hacia abajo para dar efecto de profundidad sin desaparecer por completo
        animationSpec = tween(durationMillis = AOSP_TRANSITION_DURATION, easing = M3EmphasizedEasing),
    )
}

// Equivale a sud_slide_back_in (cuando volvés atrás, la pantalla previa reaparece desde la izquierda)
fun aospSharedAxisPopEnter(): EnterTransition {
    return slideInHorizontally(
        initialOffsetX = { -it / 3 }, // Reaparece desde 1/3 de la izquierda
        animationSpec = tween(durationMillis = AOSP_TRANSITION_DURATION, easing = M3EmphasizedEasing),
    ) + fadeIn(
        animationSpec = tween(durationMillis = AOSP_TRANSITION_DURATION, easing = M3EmphasizedEasing),
    )
}

// Equivale a sud_slide_back_out (la pantalla secundaria se destruye deslizándose a la derecha)
fun aospSharedAxisPopExit(): ExitTransition {
    return slideOutHorizontally(
        targetOffsetX = { it }, // Se desliza completamente fuera de la pantalla
        animationSpec = tween(
            durationMillis = AOSP_TRANSITION_DURATION,
            easing = Easing { f -> f * f * f }, // Durante el arrastre se desplaza muy poco, y al completarse se desliza por completo
        ),
    ) + scaleOut(
        targetScale = 0.85f, // Se achica más (efecto de profundidad más pronunciado)
        animationSpec = tween(durationMillis = AOSP_TRANSITION_DURATION, easing = M3EmphasizedEasing),
    )
}

// MD3 Expressive – Emphasized easing (matches Material Motion spec)
// cubic-bezier(0.2, 0, 0, 1.0) — fast start, smooth settle
private val EmphasizedEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

// Decelerate for elements entering the screen — mirror of EmphasizedAccelerateEasing
// so the entry feels as weighty/grounded as the pop-exit
private val EmphasizedDecelerateEasing = CubicBezierEasing(0.2f, 0.85f, 0.7f, 1f)

// Accelerate for elements leaving the screen
private val EmphasizedAccelerateEasing = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)

// Base duration designed for 1x animation scale — looks good at full speed,
// still smooth at 0.5x (system halves it to ~225 ms).
const val TRANSITION_DURATION = 450

// Push: Enter from Right — slides in 50% of screen width + slight scale up (mirrors popExit's weight)
// Fade uses an accelerate-style curve so alpha stays low while slide does its work,
// then catches up at the end — prevents the "fade arrives before slide" perceptual mismatch.
fun enterTransition() = slideInHorizontally(
    animationSpec = tween(TRANSITION_DURATION, easing = EmphasizedDecelerateEasing),
    initialOffsetX = { (it * 0.5f).toInt() },
) + scaleIn(
    animationSpec = tween(TRANSITION_DURATION, easing = EmphasizedDecelerateEasing),
    initialScale = 0.92f,
    transformOrigin = TransformOrigin(0.5f, 0.5f),
) + fadeIn(
    animationSpec = tween(TRANSITION_DURATION, easing = EmphasizedAccelerateEasing),
)

// Push: Exit to Left — recedes 25% (parallax, barely moves)
fun exitTransition() = slideOutHorizontally(
    animationSpec = tween(TRANSITION_DURATION, easing = EmphasizedAccelerateEasing),
    targetOffsetX = { -(it * 0.25f).toInt() },
) + fadeOut(
    animationSpec = tween(TRANSITION_DURATION / 2, easing = EmphasizedAccelerateEasing),
)

// Pop: Enter from Left — parallax slide-in 25% + subtle scale up
fun popEnterTransition() = slideInHorizontally(
    animationSpec = tween(TRANSITION_DURATION, easing = EmphasizedDecelerateEasing),
    initialOffsetX = { -(it * 0.25f).toInt() },
) + scaleIn(
    animationSpec = tween(TRANSITION_DURATION, easing = EmphasizedDecelerateEasing),
    initialScale = 0.95f,
) + fadeIn(
    animationSpec = tween(TRANSITION_DURATION / 2, easing = EmphasizedDecelerateEasing),
)

// Pop: Exit to Right — slides out 50% + slight scale down
fun popExitTransition() = slideOutHorizontally(
    animationSpec = tween(TRANSITION_DURATION, easing = EmphasizedAccelerateEasing),
    targetOffsetX = { (it * 0.5f).toInt() },
) + scaleOut(
    animationSpec = tween(TRANSITION_DURATION, easing = EmphasizedAccelerateEasing),
    targetScale = 0.92f,
    transformOrigin = TransformOrigin(0.5f, 0.5f),
) + fadeOut(
    animationSpec = tween(TRANSITION_DURATION / 2, easing = EmphasizedAccelerateEasing),
)

fun isMainRootRoute(route: Any?): Boolean = route is Route.Main

enum class MainRootDirection {
    FORWARD,
    BACKWARD,
}

private val MAIN_ROOT_TRANSITION_SPEC =
    tween<IntOffset>(durationMillis = 400, easing = EmphasizedEasing)

private val MAIN_ROOT_FADE_SPEC =
    tween<Float>(durationMillis = 400, easing = EmphasizedEasing)

fun mainRootDirection(fromRoute: Any?, toRoute: Any?): MainRootDirection? {
    val fromIndex = mainRootRouteIndex(fromRoute) ?: return null
    val toIndex = mainRootRouteIndex(toRoute) ?: return null
    if (fromIndex == toIndex) return null
    return if (toIndex > fromIndex) MainRootDirection.FORWARD else MainRootDirection.BACKWARD
}

private fun mainRootRouteIndex(route: Any?): Int? = when (route) {
    is Route.Main -> 0
    else -> null
}

fun mainRootEnterTransition(fromRoute: Any?, toRoute: Any?, fallback: EnterTransition): EnterTransition =
    when (mainRootDirection(fromRoute, toRoute)) {
        MainRootDirection.FORWARD -> {
            slideInHorizontally(
                animationSpec = MAIN_ROOT_TRANSITION_SPEC,
                initialOffsetX = { it },
            ) + fadeIn(animationSpec = MAIN_ROOT_FADE_SPEC)
        }

        MainRootDirection.BACKWARD -> {
            slideInHorizontally(
                animationSpec = MAIN_ROOT_TRANSITION_SPEC,
                initialOffsetX = { -it },
            ) + fadeIn(animationSpec = MAIN_ROOT_FADE_SPEC)
        }

        null -> fallback
    }

fun mainRootExitTransition(fromRoute: Any?, toRoute: Any?, fallback: ExitTransition): ExitTransition =
    when (mainRootDirection(fromRoute, toRoute)) {
        MainRootDirection.FORWARD -> {
            slideOutHorizontally(
                animationSpec = MAIN_ROOT_TRANSITION_SPEC,
                targetOffsetX = { -it },
            ) + fadeOut(animationSpec = MAIN_ROOT_FADE_SPEC)
        }

        MainRootDirection.BACKWARD -> {
            slideOutHorizontally(
                animationSpec = MAIN_ROOT_TRANSITION_SPEC,
                targetOffsetX = { it },
            ) + fadeOut(animationSpec = MAIN_ROOT_FADE_SPEC)
        }

        null -> fallback
    }
