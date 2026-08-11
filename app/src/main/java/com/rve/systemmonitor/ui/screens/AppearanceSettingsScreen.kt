package com.rve.systemmonitor.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rve.systemmonitor.ui.navigation.MAX_NAV_BAR_CORNER_RADIUS
import com.rve.systemmonitor.ui.navigation.NavBarPreview
import com.rve.systemmonitor.ui.navigation.NavPreviewCard
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rve.systemmonitor.R
import com.rve.systemmonitor.ui.components.ExitUntilCollapsedMediumTopAppBar
import com.rve.systemmonitor.ui.components.haptic.hapticClickable
import com.rve.systemmonitor.ui.components.haptic.rememberHapticOnClick
import com.rve.systemmonitor.ui.components.shape.LShape
import com.rve.systemmonitor.ui.viewmodel.SettingsViewModel
import com.rve.systemmonitor.utils.NavMode
import com.rve.systemmonitor.utils.NavType
import com.rve.systemmonitor.utils.ThemeMode
import com.rve.systemmonitor.utils.VibrationIntensity
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppearanceSettingsScreen(viewModel: SettingsViewModel = hiltViewModel(), onNavigateBack: () -> Unit) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val currentTheme by viewModel.themeMode.collectAsStateWithLifecycle()
    val amoledMode by viewModel.amoledMode.collectAsStateWithLifecycle()
    val blurEffectEnabled by viewModel.blurEffectEnabled.collectAsStateWithLifecycle()
    val navBarCornerRadius by viewModel.navBarCornerRadius.collectAsStateWithLifecycle()
    val navMode by viewModel.navMode.collectAsStateWithLifecycle()
    val navType by viewModel.navType.collectAsStateWithLifecycle()
    val hapticEnabled by viewModel.hapticFeedbackEnabled.collectAsStateWithLifecycle()
    val vibrationIntensity by viewModel.vibrationIntensity.collectAsStateWithLifecycle()

    val darkTheme = when (currentTheme) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            ExitUntilCollapsedMediumTopAppBar(
                title = stringResource(R.string.title_appearance),
                onNavigateBack = onNavigateBack,
                scrollBehavior = scrollBehavior,
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 16.dp,
                bottom = innerPadding.calculateBottomPadding() + 16.dp,
                start = 16.dp,
                end = 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            item {
                AppearanceHero(
                    hapticEnabled = hapticEnabled,
                    vibrationIntensity = vibrationIntensity,
                    currentTheme = currentTheme,
                    amoledMode = amoledMode,
                )
            }
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = stringResource(R.string.label_visual_style),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp, start = 8.dp),
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 8.dp, bottomEnd = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 20.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.primary),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.brightness_medium_filled),
                                            contentDescription = stringResource(R.string.cd_theme_icon),
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = stringResource(R.string.settings_app_theme),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                        )
                                        Text(
                                            text = stringResource(R.string.settings_app_theme_description),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }

                                val themeOptions = listOf(
                                    ThemeMode.LIGHT to R.string.theme_light,
                                    ThemeMode.SYSTEM to R.string.theme_system,
                                    ThemeMode.DARK to R.string.theme_dark,
                                )

                                SingleChoiceSegmentedButtonRow(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp),
                                ) {
                                    themeOptions.forEachIndexed { index, (mode, labelRes) ->
                                        SegmentedButton(
                                            shape = SegmentedButtonDefaults.itemShape(
                                                index = index,
                                                count = themeOptions.size,
                                            ),
                                            onClick = rememberHapticOnClick { viewModel.setThemeMode(mode) },
                                            selected = currentTheme == mode,
                                        ) {
                                            Text(
                                                text = stringResource(labelRes),
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = if (currentTheme == mode) FontWeight.Bold else FontWeight.Normal,
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        val amoledEnabled = darkTheme
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .hapticClickable(enabled = amoledEnabled) { viewModel.setAmoledMode(!amoledMode) }
                                    .padding(horizontal = 20.dp, vertical = 20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (amoledEnabled) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),
                                            ),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.night_mode_filled),
                                            contentDescription = stringResource(R.string.cd_amoled_icon),
                                            tint = if (amoledEnabled) MaterialTheme.colorScheme.onPrimary
                                            else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.38f),
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = stringResource(R.string.settings_amoled_mode),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (amoledEnabled) MaterialTheme.colorScheme.onSurface
                                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                        )
                                        Text(
                                            text = if (amoledEnabled) stringResource(R.string.settings_amoled_description_enabled)
                                            else stringResource(R.string.settings_amoled_description_disabled),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (amoledEnabled) MaterialTheme.colorScheme.onSurfaceVariant
                                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                                        )
                                    }
                                }

                                Switch(
                                    enabled = amoledEnabled,
                                    checked = amoledMode && amoledEnabled,
                                    onCheckedChange = { viewModel.setAmoledMode(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedIconColor = MaterialTheme.colorScheme.primary,
                                    ),
                                    thumbContent = {
                                        Crossfade(
                                            targetState = amoledMode && amoledEnabled,
                                            animationSpec = MaterialTheme.motionScheme.slowEffectsSpec(),
                                            label = "Amoled Switch Icon",
                                        ) { enabled ->
                                            Icon(
                                                painter = painterResource(
                                                    if (enabled) R.drawable.check_rounded else R.drawable.close_rounded,
                                                ),
                                                contentDescription = null,
                                                modifier = Modifier.size(SwitchDefaults.IconSize),
                                            )
                                        }
                                    },
                                )
                            }
                        }

                        NavBarStyleSection(
                            blurEffectEnabled = blurEffectEnabled,
                            onBlurEffectChange = { viewModel.setBlurEffectEnabled(it) },
                            navMode = navMode,
                            radius = navBarCornerRadius,
                            onNavModeChange = { viewModel.setNavMode(it) },
                            onRadiusChange = { viewModel.setNavBarCornerRadius(it) },
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 12.dp),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .hapticClickable { viewModel.setHapticFeedbackEnabled(!hapticEnabled) }
                                        .padding(horizontal = 20.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        modifier = Modifier.weight(1f),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(MaterialTheme.colorScheme.primary),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.mobile_vibrate_filled),
                                                contentDescription = stringResource(R.string.cd_haptic_icon),
                                                tint = MaterialTheme.colorScheme.onSecondary,
                                            )
                                        }

                                        Column {
                                            Text(
                                                text = stringResource(R.string.settings_haptic_feedback),
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                            )
                                            Text(
                                                text = stringResource(R.string.settings_haptic_description),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                    }

                                    Switch(
                                        checked = hapticEnabled,
                                        onCheckedChange = { viewModel.setHapticFeedbackEnabled(it) },
                                        colors = SwitchDefaults.colors(
                                            checkedIconColor = MaterialTheme.colorScheme.primary,
                                        ),
                                        thumbContent = {
                                            Crossfade(
                                                targetState = hapticEnabled,
                                                animationSpec = MaterialTheme.motionScheme.slowEffectsSpec(),
                                                label = "Haptic Switch Icon",
                                            ) { enabled ->
                                                Icon(
                                                    painter = painterResource(
                                                        if (enabled) R.drawable.check_rounded else R.drawable.close_rounded,
                                                    ),
                                                    contentDescription = null,
                                                    modifier = Modifier.size(SwitchDefaults.IconSize),
                                                )
                                            }
                                        },
                                    )
                                }

                                AnimatedVisibility(
                                    visible = hapticEnabled,
                                    enter = expandVertically(
                                        animationSpec = MaterialTheme.motionScheme.slowSpatialSpec(),
                                    ) + fadeIn(
                                        animationSpec = MaterialTheme.motionScheme.slowEffectsSpec(),
                                    ),
                                    exit = shrinkVertically(
                                        animationSpec = MaterialTheme.motionScheme.slowSpatialSpec(),
                                    ) + fadeOut(
                                        animationSpec = MaterialTheme.motionScheme.slowEffectsSpec(),
                                    ),
                                ) {
                                    Column(
                                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 8.dp, top = 8.dp),
                                    ) {
                                        Text(
                                            text = stringResource(R.string.settings_vibration_intensity),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(bottom = 8.dp),
                                        )

                                        val intensityOptions = listOf(
                                            VibrationIntensity.LIGHT to R.string.vibration_light,
                                            VibrationIntensity.MEDIUM to R.string.vibration_medium,
                                            VibrationIntensity.STRONG to R.string.vibration_strong,
                                        )

                                        SingleChoiceSegmentedButtonRow(
                                            modifier = Modifier.fillMaxWidth(),
                                        ) {
                                            intensityOptions.forEachIndexed { index, (intensity, labelRes) ->
                                                SegmentedButton(
                                                    shape = SegmentedButtonDefaults.itemShape(
                                                        index = index,
                                                        count = intensityOptions.size,
                                                    ),
                                                    onClick = rememberHapticOnClick {
                                                        viewModel.setVibrationIntensity(
                                                            intensity,
                                                        )
                                                    },
                                                    selected = vibrationIntensity == intensity,
                                                ) {
                                                    Text(
                                                        text = stringResource(labelRes),
                                                        style = MaterialTheme.typography.labelLarge,
                                                        fontWeight = if (vibrationIntensity ==
                                                            intensity
                                                        ) FontWeight.Bold else FontWeight.Normal,
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun Modifier.shimmerEffect(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "Shimmer Transition")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "Shimmer Offset",
    )

    val shimmerColors = listOf(
        Color.White.copy(alpha = 0.0f),
        Color.White.copy(alpha = 0.3f),
        Color.White.copy(alpha = 0.0f),
    )

    background(
        brush = Brush.linearGradient(
            colors = shimmerColors,
            start = Offset.Zero,
            end = Offset(x = translateAnim, y = translateAnim),
        ),
    )
}

@Composable
private fun AppearanceHero(hapticEnabled: Boolean, vibrationIntensity: VibrationIntensity, currentTheme: ThemeMode, amoledMode: Boolean) {
    val isDark = when (currentTheme) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val iconRes = remember(isDark, amoledMode) {
        if (isDark) {
            if (amoledMode) R.drawable.night_mode_filled else R.drawable.dark_mode
        } else {
            R.drawable.light_mode
        }
    }

    val shakeOffset = remember { Animatable(0f) }
    val blurRadius = abs(shakeOffset.value).dp / 2f

    LaunchedEffect(hapticEnabled, vibrationIntensity) {
        if (hapticEnabled) {
            val amplitude = when (vibrationIntensity) {
                VibrationIntensity.LIGHT -> 2f
                VibrationIntensity.MEDIUM -> 4f
                VibrationIntensity.STRONG -> 8f
            }
            val duration = when (vibrationIntensity) {
                VibrationIntensity.LIGHT -> 40
                VibrationIntensity.MEDIUM -> 30
                VibrationIntensity.STRONG -> 20
            }

            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = duration * 8
                    -amplitude at duration using LinearEasing
                    amplitude at duration * 2 using LinearEasing
                    -amplitude at duration * 3 using LinearEasing
                    amplitude at duration * 4 using LinearEasing
                    -amplitude at duration * 5 using LinearEasing
                    amplitude at duration * 6 using LinearEasing
                    -amplitude at duration * 7 using LinearEasing
                    0f at duration * 8 using LinearEasing
                },
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize(),
            shape = LShape(cornerRadius = 32.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(0.73f),
                ) {
                    Text(
                        text = stringResource(R.string.title_appearance),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        text = stringResource(R.string.appearance_hero_subtitle),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp)
                            .graphicsLayer {
                                translationX = shakeOffset.value
                            }
                            .blur(radiusX = blurRadius, radiusY = 0.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(12.dp),
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)),
                            ) {
                                Box(modifier = Modifier.fillMaxSize().shimmerEffect())
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.7f)
                                        .height(6.6.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)),
                                ) {
                                    Box(modifier = Modifier.fillMaxSize().shimmerEffect())
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.5f)
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)),
                                ) {
                                    Box(modifier = Modifier.fillMaxSize().shimmerEffect())
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp)
                            .graphicsLayer {
                                translationX = shakeOffset.value
                            }
                            .blur(radiusX = blurRadius, radiusY = 0.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.tertiary)
                            .padding(12.dp),
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.onTertiary),
                            ) {
                                Box(modifier = Modifier.fillMaxSize().shimmerEffect())
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.7f)
                                        .height(6.6.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.6f)),
                                ) {
                                    Box(modifier = Modifier.fillMaxSize().shimmerEffect())
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.5f)
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.3f)),
                                ) {
                                    Box(modifier = Modifier.fillMaxSize().shimmerEffect())
                                }
                            }
                        }
                    }
                }
            }
        }

        val spatialSpec = MaterialTheme.motionScheme.slowSpatialSpec<Float>()
        val effectsSpec = MaterialTheme.motionScheme.slowEffectsSpec<Float>()
        val spatialSpecInt = MaterialTheme.motionScheme.slowSpatialSpec<IntOffset>()

        AnimatedContent(
            targetState = iconRes,
            transitionSpec = {
                (
                    fadeIn(animationSpec = effectsSpec) +
                        scaleIn(
                            initialScale = 0f,
                            transformOrigin = TransformOrigin(0.5f, 1f),
                            animationSpec = spatialSpec,
                        ) +
                        slideInVertically(
                            initialOffsetY = { it },
                            animationSpec = spatialSpecInt,
                        )
                    )
                    .togetherWith(
                        fadeOut(animationSpec = effectsSpec) +
                            scaleOut(
                                targetScale = 0f,
                                transformOrigin = TransformOrigin(0.5f, 1f),
                                animationSpec = spatialSpec,
                            ) +
                            slideOutVertically(
                                targetOffsetY = { it },
                                animationSpec = spatialSpecInt,
                            ),
                    )
            },
            label = "Icon Transition",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 16.dp, top = 16.dp)
                .size(48.dp),
        ) { targetIcon ->
            Icon(
                painter = painterResource(targetIcon),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun NavBarStyleSection(
    blurEffectEnabled: Boolean,
    onBlurEffectChange: (Boolean) -> Unit,
    navMode: NavMode,
    radius: Int,
    onNavModeChange: (NavMode) -> Unit,
    onRadiusChange: (Int) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header Text
            Text(
                text = "Navigation Bar",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 20.dp),
            )

            // Wrapped/Nested Card for Style and Corner Radius
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // 1. Bottom Navigation Style
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = stringResource(R.string.settings_nav_bar_style),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            NavPreviewCard(
                                label = stringResource(R.string.settings_nav_style_standard),
                                selected = navMode == NavMode.STANDARD,
                                onClick = { onNavModeChange(NavMode.STANDARD) },
                                modifier = Modifier.weight(1f),
                            ) {
                                NavBarPreview(navMode = NavMode.STANDARD, navType = NavType.MODERN, radius = radius)
                            }
                            NavPreviewCard(
                                label = stringResource(R.string.settings_nav_style_floating),
                                selected = navMode == NavMode.FLOATING,
                                onClick = { onNavModeChange(NavMode.FLOATING) },
                                modifier = Modifier.weight(1f),
                            ) {
                                NavBarPreview(navMode = NavMode.FLOATING, navType = NavType.LEGACY, radius = radius)
                            }
                        }
                    }

                    // Divider inside nested card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f))
                    )

                    // 2. Corner Radius
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = stringResource(R.string.settings_nav_bar_corner_radius),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = stringResource(R.string.settings_nav_bar_corner_radius_description),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Slider(
                                value = radius.toFloat().coerceIn(12f, MAX_NAV_BAR_CORNER_RADIUS),
                                onValueChange = { onRadiusChange(it.roundToInt()) },
                                valueRange = 12f..MAX_NAV_BAR_CORNER_RADIUS,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                text = stringResource(R.string.settings_nav_bar_corner_radius_value, radius.coerceAtLeast(12)),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(start = 8.dp),
                            )
                        }
                    }
                }
            }

            // Divider outside nested card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .padding(horizontal = 20.dp)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f))
            )

            // 3. Blur Effect Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .hapticClickable { onBlurEffectChange(!blurEffectEnabled) }
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.layers_filled),
                            contentDescription = stringResource(R.string.settings_blur_effect),
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                    }

                    Column {
                        Text(
                            text = stringResource(R.string.settings_blur_effect),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = stringResource(R.string.settings_blur_description),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Switch(
                    checked = blurEffectEnabled,
                    onCheckedChange = onBlurEffectChange,
                    colors = SwitchDefaults.colors(
                        checkedIconColor = MaterialTheme.colorScheme.primary,
                    ),
                    thumbContent = {
                        Crossfade(
                            targetState = blurEffectEnabled,
                            animationSpec = MaterialTheme.motionScheme.slowEffectsSpec(),
                            label = "Blur Switch Icon",
                        ) { enabled ->
                            Icon(
                                painter = painterResource(
                                    if (enabled) R.drawable.check_rounded else R.drawable.close_rounded,
                                ),
                                contentDescription = null,
                                modifier = Modifier.size(SwitchDefaults.IconSize),
                            )
                        }
                    },
                )
            }
        }
    }
}


/** Half the nav bar height, past which the shape stops changing. */
private const val MAX_NAV_BAR_CORNER_RADIUS = 32f
