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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
    val materialYou by viewModel.materialYou.collectAsStateWithLifecycle()
    val themeSeedColor by viewModel.themeSeedColor.collectAsStateWithLifecycle()

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
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                // Nested Card 1: App Theme & AMOLED Mode
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp),
                                    ) {
                                        Row(
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

                                        val themeLabels = listOf(
                                            stringResource(R.string.theme_system),
                                            stringResource(R.string.theme_light),
                                            stringResource(R.string.theme_dark)
                                        )

                                        SegmentedControl(
                                            items = themeLabels,
                                            selectedIndex = when (currentTheme) {
                                                ThemeMode.SYSTEM -> 0
                                                ThemeMode.LIGHT -> 1
                                                ThemeMode.DARK -> 2
                                            },
                                            onItemSelected = { index ->
                                                val mode = when (index) {
                                                    0 -> ThemeMode.SYSTEM
                                                    1 -> ThemeMode.LIGHT
                                                    2 -> ThemeMode.DARK
                                                    else -> ThemeMode.SYSTEM
                                                }
                                                viewModel.setThemeMode(mode)
                                            }
                                        )

                                        // Conditional AMOLED Mode inside the same card
                                        AnimatedVisibility(
                                            visible = darkTheme,
                                            enter = fadeIn() + expandVertically(),
                                            exit = fadeOut() + shrinkVertically(),
                                        ) {
                                            val amoledEnabled = darkTheme
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .hapticClickable(enabled = amoledEnabled) { viewModel.setAmoledMode(!amoledMode) },
                                                shape = RoundedCornerShape(12.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                                ),
                                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 16.dp, vertical = 16.dp),
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
                                        }
                                    }
                                }

                                // Nested Card 2: Material You
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .hapticClickable { viewModel.setMaterialYou(!materialYou) },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 16.dp),
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
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onPrimary,
                                                )
                                            }

                                            Column {
                                                Text(
                                                    text = stringResource(R.string.settings_material_you),
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                )
                                                Text(
                                                    text = stringResource(R.string.settings_material_you_description),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                )
                                            }
                                        }

                                        Switch(
                                            checked = materialYou,
                                            onCheckedChange = { viewModel.setMaterialYou(it) },
                                            colors = SwitchDefaults.colors(
                                                checkedIconColor = MaterialTheme.colorScheme.primary,
                                            ),
                                            thumbContent = {
                                                Crossfade(
                                                    targetState = materialYou,
                                                    animationSpec = MaterialTheme.motionScheme.slowEffectsSpec(),
                                                    label = "Material You Switch Icon",
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

                                // Nested Card 3: Theme Color Palette (Only visible if Material You is disabled)
                                AnimatedVisibility(
                                    visible = !materialYou,
                                    enter = fadeIn() + expandVertically(),
                                    exit = fadeOut() + shrinkVertically(),
                                ) {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                        ),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(12.dp),
                                        ) {
                                            Column {
                                                Text(
                                                    text = stringResource(R.string.settings_theme_color),
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                )
                                                Text(
                                                    text = stringResource(R.string.settings_theme_color_description),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                )
                                            }

                                            val palette = listOf(
                                                0xFFFFB68E.toInt(), // Peach (default)
                                                0xFFFFB4AB.toInt(), // Red
                                                0xFFFFD188.toInt(), // Amber
                                                0xFFA7E0A2.toInt(), // Green
                                                0xFF9ECAFF.toInt(), // Blue
                                                0xFFD0BCFF.toInt(), // Violet
                                                0xFFF8AFD2.toInt(), // Pink
                                                0xFFA7D8DA.toInt(), // Teal
                                            )

                                            androidx.compose.foundation.lazy.LazyRow(
                                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                modifier = Modifier.fillMaxWidth(),
                                            ) {
                                                items(palette.size) { index ->
                                                    val colorInt = palette[index]
                                                    SwatchDot(
                                                        seedColor = Color(colorInt),
                                                        selected = themeSeedColor == colorInt,
                                                        onClick = {
                                                            viewModel.setMaterialYou(false)
                                                            viewModel.setThemeSeedColor(colorInt)
                                                        },
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
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
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                // Nested Card 1: Haptic Feedback Switch
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .hapticClickable { viewModel.setHapticFeedbackEnabled(!hapticEnabled) },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 16.dp),
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
                                                    tint = MaterialTheme.colorScheme.onPrimary,
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
                                }

                                // Nested Card 2: Vibration Intensity Selector
                                AnimatedVisibility(
                                    visible = hapticEnabled,
                                    enter = fadeIn() + expandVertically(),
                                    exit = fadeOut() + shrinkVertically(),
                                ) {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                        ),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(12.dp),
                                        ) {
                                            Text(
                                                text = stringResource(R.string.settings_vibration_intensity),
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                            )

                                            val intensityLabels = listOf(
                                                stringResource(R.string.vibration_light),
                                                stringResource(R.string.vibration_medium),
                                                stringResource(R.string.vibration_strong)
                                            )

                                            SegmentedControl(
                                                items = intensityLabels,
                                                selectedIndex = when (vibrationIntensity) {
                                                    VibrationIntensity.LIGHT -> 0
                                                    VibrationIntensity.MEDIUM -> 1
                                                    VibrationIntensity.STRONG -> 2
                                                },
                                                onItemSelected = { index ->
                                                    val intensity = when (index) {
                                                        0 -> VibrationIntensity.LIGHT
                                                        1 -> VibrationIntensity.MEDIUM
                                                        2 -> VibrationIntensity.STRONG
                                                        else -> VibrationIntensity.MEDIUM
                                                    }
                                                    viewModel.setVibrationIntensity(intensity)
                                                }
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
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Header Text
            Text(
                text = "Navigation Bar",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            )

            // Wrapped/Nested Card for Style and Corner Radius
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
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

            // 3. Blur Effect Nested Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .hapticClickable { onBlurEffectChange(!blurEffectEnabled) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
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
                                contentDescription = null,
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
}


/** Half the nav bar height, past which the shape stops changing. */
private const val MAX_NAV_BAR_CORNER_RADIUS = 32f

@Composable
private fun SwatchDot(seedColor: Color, selected: Boolean, onClick: () -> Unit) {
    val primary = seedColor
    val secondary = Color(android.graphics.Color.HSVToColor(FloatArray(3).apply {
        android.graphics.Color.colorToHSV(primary.toArgb(), this)
        this[0] = (this[0] + 30) % 360f
        this[1] = (this[1] * 0.8f).coerceIn(0f, 1f)
    }))
    val tertiary = Color(android.graphics.Color.HSVToColor(FloatArray(3).apply {
        android.graphics.Color.colorToHSV(primary.toArgb(), this)
        this[0] = (this[0] + 120) % 360f
        this[1] = (this[1] * 0.9f).coerceIn(0f, 1f)
    }))
    val neutral = Color(android.graphics.Color.HSVToColor(FloatArray(3).apply {
        android.graphics.Color.colorToHSV(primary.toArgb(), this)
        this[1] = (this[1] * 0.2f).coerceIn(0f, 1f)
        this[2] = 0.5f
    }))

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .hapticClickable(onClick = onClick, ripple = true),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            drawArc(color = primary, startAngle = 180f, sweepAngle = 90f, useCenter = true)
            drawArc(color = secondary, startAngle = 270f, sweepAngle = 90f, useCenter = true)
            drawArc(color = tertiary, startAngle = 0f, sweepAngle = 90f, useCenter = true)
            drawArc(color = neutral, startAngle = 90f, sweepAngle = 90f, useCenter = true)
        }

        if (selected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun SegmentedControl(
    items: List<String>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f),
    activeColor: Color = MaterialTheme.colorScheme.primaryContainer,
    activeTextColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    inactiveTextColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
    cornerRadius: Int = 99,
    itemCornerRadius: Int = 99,
    verticalPadding: Int = 10
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(cornerRadius.dp))
            .background(containerColor)
            .padding(4.dp)
    ) {
        items.forEachIndexed { index, title ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(itemCornerRadius.dp))
                    .background(if (selectedIndex == index) activeColor else Color.Transparent)
                    .hapticClickable { onItemSelected(index) }
                    .padding(vertical = verticalPadding.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = if (selectedIndex == index) activeTextColor else inactiveTextColor
                )
            }
        }
    }
}
