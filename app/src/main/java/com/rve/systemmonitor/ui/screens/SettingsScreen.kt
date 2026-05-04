package com.rve.systemmonitor.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animate
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rve.systemmonitor.R
import com.rve.systemmonitor.ui.components.ExitUntilCollapsedMediumTopAppBar
import com.rve.systemmonitor.ui.components.haptic.hapticClickable
import com.rve.systemmonitor.ui.components.haptic.rememberHapticOnClick
import com.rve.systemmonitor.ui.components.haptic.rememberHapticOnValueChange
import com.rve.systemmonitor.ui.viewmodel.SettingsViewModel
import com.rve.systemmonitor.utils.ThemeMode
import com.rve.systemmonitor.utils.VibrationIntensity
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel(), onNavigateBack: () -> Unit) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val currentTheme by viewModel.themeMode.collectAsStateWithLifecycle()
    val hapticEnabled by viewModel.hapticFeedbackEnabled.collectAsStateWithLifecycle()
    val vibrationIntensity by viewModel.vibrationIntensity.collectAsStateWithLifecycle()
    val cpuDelayMillis by viewModel.cpuRefreshDelay.collectAsStateWithLifecycle()
    val memoryDelayMillis by viewModel.memoryRefreshDelay.collectAsStateWithLifecycle()
    val batteryDelayMillis by viewModel.batteryRefreshDelay.collectAsStateWithLifecycle()
    val batteryGraphHistorySeconds by viewModel.batteryGraphHistorySeconds.collectAsStateWithLifecycle()

    val coroutineScope = rememberCoroutineScope()
    val snapAnimationSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()

    val cpuSliderState = rememberSliderState(
        value = (cpuDelayMillis / 1000).toFloat(),
        steps = 3,
        valueRange = 1f..5f,
    )
    var cpuCurrentValue by rememberSaveable(cpuDelayMillis) { mutableFloatStateOf((cpuDelayMillis / 1000).toFloat()) }
    var cpuAnimateJob: Job? by remember { mutableStateOf(null) }

    val memorySliderState = rememberSliderState(
        value = (memoryDelayMillis / 1000).toFloat(),
        steps = 3,
        valueRange = 1f..5f,
    )
    var memoryCurrentValue by rememberSaveable(memoryDelayMillis) { mutableFloatStateOf((memoryDelayMillis / 1000).toFloat()) }
    var memoryAnimateJob: Job? by remember { mutableStateOf(null) }

    val batterySliderState = rememberSliderState(
        value = (batteryDelayMillis / 1000).toFloat(),
        steps = 3,
        valueRange = 1f..5f,
    )
    var batteryCurrentValue by rememberSaveable(batteryDelayMillis) { mutableFloatStateOf((batteryDelayMillis / 1000).toFloat()) }
    var batteryAnimateJob: Job? by remember { mutableStateOf(null) }

    val historySliderState = rememberSliderState(
        value = batteryGraphHistorySeconds.toFloat().coerceIn(30f, 300f),
        steps = 8,
        valueRange = 30f..300f,
    )
    var historyCurrentValue by rememberSaveable(batteryGraphHistorySeconds) {
        mutableFloatStateOf(batteryGraphHistorySeconds.toFloat().coerceIn(30f, 300f))
    }
    var historyAnimateJob: Job? by remember { mutableStateOf(null) }

    LaunchedEffect(cpuDelayMillis) {
        if (!cpuSliderState.isDragging) {
            cpuSliderState.value = (cpuDelayMillis / 1000).toFloat()
            cpuCurrentValue = (cpuDelayMillis / 1000).toFloat()
        }
    }

    LaunchedEffect(memoryDelayMillis) {
        if (!memorySliderState.isDragging) {
            memorySliderState.value = (memoryDelayMillis / 1000).toFloat()
            memoryCurrentValue = (memoryDelayMillis / 1000).toFloat()
        }
    }

    LaunchedEffect(batteryDelayMillis) {
        if (!batterySliderState.isDragging) {
            batterySliderState.value = (batteryDelayMillis / 1000).toFloat()
            batteryCurrentValue = (batteryDelayMillis / 1000).toFloat()
        }
    }

    LaunchedEffect(batteryGraphHistorySeconds) {
        if (!historySliderState.isDragging) {
            val coercedValue = batteryGraphHistorySeconds.toFloat().coerceIn(30f, 300f)
            historySliderState.value = coercedValue
            historyCurrentValue = coercedValue
        }
    }

    cpuSliderState.shouldAutoSnap = false
    cpuSliderState.onValueChange = rememberHapticOnValueChange { newValue ->
        cpuCurrentValue = newValue
        if (cpuSliderState.isDragging) {
            cpuAnimateJob?.cancel()
            cpuSliderState.value = newValue
        }
    }

    cpuSliderState.onValueChangeFinished = {
        cpuAnimateJob = coroutineScope.launch {
            animate(
                initialValue = cpuSliderState.value,
                targetValue = cpuCurrentValue,
                animationSpec = snapAnimationSpec,
            ) { value, _ ->
                cpuSliderState.value = value
            }
            viewModel.setCpuRefreshDelay(cpuCurrentValue.toLong() * 1000)
        }
    }

    memorySliderState.shouldAutoSnap = false
    memorySliderState.onValueChange = rememberHapticOnValueChange { newValue ->
        memoryCurrentValue = newValue
        if (memorySliderState.isDragging) {
            memoryAnimateJob?.cancel()
            memorySliderState.value = newValue
        }
    }

    memorySliderState.onValueChangeFinished = {
        memoryAnimateJob = coroutineScope.launch {
            animate(
                initialValue = memorySliderState.value,
                targetValue = memoryCurrentValue,
                animationSpec = snapAnimationSpec,
            ) { value, _ ->
                memorySliderState.value = value
            }
            viewModel.setMemoryRefreshDelay(memoryCurrentValue.toLong() * 1000)
        }
    }

    batterySliderState.shouldAutoSnap = false
    batterySliderState.onValueChange = rememberHapticOnValueChange { newValue ->
        batteryCurrentValue = newValue
        if (batterySliderState.isDragging) {
            batteryAnimateJob?.cancel()
            batterySliderState.value = newValue
        }
    }

    batterySliderState.onValueChangeFinished = {
        batteryAnimateJob = coroutineScope.launch {
            animate(
                initialValue = batterySliderState.value,
                targetValue = batteryCurrentValue,
                animationSpec = snapAnimationSpec,
            ) { value, _ ->
                batterySliderState.value = value
            }
            viewModel.setBatteryRefreshDelay(batteryCurrentValue.toLong() * 1000)
        }
    }

    historySliderState.shouldAutoSnap = false
    historySliderState.onValueChange = rememberHapticOnValueChange { newValue ->
        historyCurrentValue = newValue
        if (historySliderState.isDragging) {
            historyAnimateJob?.cancel()
            historySliderState.value = newValue
        }
    }

    historySliderState.onValueChangeFinished = {
        historyAnimateJob = coroutineScope.launch {
            animate(
                initialValue = historySliderState.value,
                targetValue = historyCurrentValue,
                animationSpec = snapAnimationSpec,
            ) { value, _ ->
                historySliderState.value = value
            }
            viewModel.setBatteryGraphHistorySeconds(historyCurrentValue.toInt())
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            ExitUntilCollapsedMediumTopAppBar(
                title = "Settings",
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
                Column(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "Appearance",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp, start = 8.dp),
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
                            modifier = Modifier.padding(top = 20.dp, bottom = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(0.dp),
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
                                        contentDescription = "Theme Icon",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                    )
                                }

                                Column {
                                    Text(
                                        text = "App Theme",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        text = "Choose your preferred visual style",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            val themeOptions = listOf(
                                ThemeMode.LIGHT to "Light",
                                ThemeMode.SYSTEM to "System",
                                ThemeMode.DARK to "Dark",
                            )

                            SingleChoiceSegmentedButtonRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp),
                            ) {
                                themeOptions.forEachIndexed { index, (mode, label) ->
                                    SegmentedButton(
                                        shape = SegmentedButtonDefaults.itemShape(
                                            index = index,
                                            count = themeOptions.size,
                                        ),
                                        onClick = rememberHapticOnClick { viewModel.setThemeMode(mode) },
                                        selected = currentTheme == mode,
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = if (currentTheme == mode) FontWeight.Bold else FontWeight.Normal,
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .hapticClickable { viewModel.setHapticFeedbackEnabled(!hapticEnabled) }
                                    .padding(horizontal = 20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(vertical = 8.dp),
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
                                            contentDescription = "Haptic Icon",
                                            tint = MaterialTheme.colorScheme.onSecondary,
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = "Haptic Feedback",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                        )
                                        Text(
                                            text = "Subtle vibrations on interaction",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }

                                Switch(
                                    checked = hapticEnabled,
                                    onCheckedChange = { viewModel.setHapticFeedbackEnabled(it) },
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
                                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 8.dp),
                                ) {
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "Vibration Intensity",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(bottom = 8.dp),
                                    )

                                    val intensityOptions = listOf(
                                        VibrationIntensity.LIGHT to "Light",
                                        VibrationIntensity.MEDIUM to "Medium",
                                        VibrationIntensity.STRONG to "Strong",
                                    )

                                    SingleChoiceSegmentedButtonRow(
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        intensityOptions.forEachIndexed { index, (intensity, label) ->
                                            SegmentedButton(
                                                shape = SegmentedButtonDefaults.itemShape(
                                                    index = index,
                                                    count = intensityOptions.size,
                                                ),
                                                onClick = rememberHapticOnClick { viewModel.setVibrationIntensity(intensity) },
                                                selected = vibrationIntensity == intensity,
                                            ) {
                                                Text(
                                                    text = label,
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

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = "Monitoring",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp),
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
                            modifier = Modifier.padding(20.dp),
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
                                        painter = painterResource(R.drawable.memory_filled),
                                        contentDescription = "CPU Monitoring Icon",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                    )
                                }

                                Column {
                                    Text(
                                        text = "CPU Update Interval",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        text = "Adjust how often CPU stats are refreshed",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = "Refresh Rate",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${cpuCurrentValue.toInt()}s",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                        AnimatedVisibility(
                                            visible = cpuCurrentValue != 3.0f,
                                            enter = slideInHorizontally(
                                                animationSpec = MaterialTheme.motionScheme.slowSpatialSpec(),
                                            ) { it } + expandHorizontally(
                                                animationSpec = MaterialTheme.motionScheme.slowSpatialSpec(),
                                            ) + fadeIn(
                                                animationSpec = MaterialTheme.motionScheme.slowEffectsSpec(),
                                            ),
                                            exit = slideOutHorizontally(
                                                animationSpec = MaterialTheme.motionScheme.slowSpatialSpec(),
                                            ) { it } + shrinkHorizontally(
                                                animationSpec = MaterialTheme.motionScheme.slowSpatialSpec(),
                                            ) + fadeOut(
                                                animationSpec = MaterialTheme.motionScheme.slowEffectsSpec(),
                                            ),
                                        ) {
                                            Row {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                IconButton(
                                                    onClick = rememberHapticOnClick {
                                                        viewModel.setCpuRefreshDelay(3000L)
                                                    },
                                                    modifier = Modifier.size(24.dp),
                                                ) {
                                                    Icon(
                                                        painter = painterResource(R.drawable.reset_settings_rounded),
                                                        contentDescription = "Reset to default",
                                                        modifier = Modifier.size(16.dp),
                                                        tint = MaterialTheme.colorScheme.primary,
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Slider(
                                    state = cpuSliderState,
                                    modifier = Modifier.fillMaxWidth(),
                                    track = {
                                        SliderDefaults.Track(
                                            sliderState = cpuSliderState,
                                            modifier = Modifier.height(36.dp),
                                            trackCornerSize = 12.dp,
                                        )
                                    },
                                )
                            }
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
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
                                        painter = painterResource(R.drawable.memory_alt_filled),
                                        contentDescription = "Memory Monitoring Icon",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                    )
                                }

                                Column {
                                    Text(
                                        text = "Memory Update Interval",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        text = "Adjust how often Memory stats are refreshed",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = "Refresh Rate",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${memoryCurrentValue.toInt()}s",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                        AnimatedVisibility(
                                            visible = memoryCurrentValue != 3.0f,
                                            enter = slideInHorizontally(
                                                animationSpec = MaterialTheme.motionScheme.slowSpatialSpec(),
                                            ) { it } + expandHorizontally(
                                                animationSpec = MaterialTheme.motionScheme.slowSpatialSpec(),
                                            ) + fadeIn(
                                                animationSpec = MaterialTheme.motionScheme.slowEffectsSpec(),
                                            ),
                                            exit = slideOutHorizontally(
                                                animationSpec = MaterialTheme.motionScheme.slowSpatialSpec(),
                                            ) { it } + shrinkHorizontally(
                                                animationSpec = MaterialTheme.motionScheme.slowSpatialSpec(),
                                            ) + fadeOut(
                                                animationSpec = MaterialTheme.motionScheme.slowEffectsSpec(),
                                            ),
                                        ) {
                                            Row {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                IconButton(
                                                    onClick = rememberHapticOnClick {
                                                        viewModel.setMemoryRefreshDelay(3000L)
                                                    },
                                                    modifier = Modifier.size(24.dp),
                                                ) {
                                                    Icon(
                                                        painter = painterResource(R.drawable.reset_settings_rounded),
                                                        contentDescription = "Reset to default",
                                                        modifier = Modifier.size(16.dp),
                                                        tint = MaterialTheme.colorScheme.primary,
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Slider(
                                    state = memorySliderState,
                                    modifier = Modifier.fillMaxWidth(),
                                    track = {
                                        SliderDefaults.Track(
                                            sliderState = memorySliderState,
                                            modifier = Modifier.height(36.dp),
                                            trackCornerSize = 12.dp,
                                        )
                                    },
                                )
                            }
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
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
                                        painter = painterResource(R.drawable.battery_android_full),
                                        contentDescription = "Battery Monitoring Icon",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                    )
                                }

                                Column {
                                    Text(
                                        text = "Battery Update Interval",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        text = "Adjust how often uptime and current (mA) are refreshed",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = "Refresh Rate",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${batteryCurrentValue.toInt()}s",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                        AnimatedVisibility(
                                            visible = batteryCurrentValue != 1.0f,
                                            enter = slideInHorizontally(
                                                animationSpec = MaterialTheme.motionScheme.slowSpatialSpec(),
                                            ) { it } + expandHorizontally(
                                                animationSpec = MaterialTheme.motionScheme.slowSpatialSpec(),
                                            ) + fadeIn(
                                                animationSpec = MaterialTheme.motionScheme.slowEffectsSpec(),
                                            ),
                                            exit = slideOutHorizontally(
                                                animationSpec = MaterialTheme.motionScheme.slowSpatialSpec(),
                                            ) { it } + shrinkHorizontally(
                                                animationSpec = MaterialTheme.motionScheme.slowSpatialSpec(),
                                            ) + fadeOut(
                                                animationSpec = MaterialTheme.motionScheme.slowEffectsSpec(),
                                            ),
                                        ) {
                                            Row {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                IconButton(
                                                    onClick = rememberHapticOnClick {
                                                        viewModel.setBatteryRefreshDelay(1000L)
                                                    },
                                                    modifier = Modifier.size(24.dp),
                                                ) {
                                                    Icon(
                                                        painter = painterResource(R.drawable.reset_settings_rounded),
                                                        contentDescription = "Reset to default",
                                                        modifier = Modifier.size(16.dp),
                                                        tint = MaterialTheme.colorScheme.primary,
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Slider(
                                    state = batterySliderState,
                                    modifier = Modifier.fillMaxWidth(),
                                    track = {
                                        SliderDefaults.Track(
                                            sliderState = batterySliderState,
                                            modifier = Modifier.height(36.dp),
                                            trackCornerSize = 12.dp,
                                        )
                                    },
                                )
                            }
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
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
                                        painter = painterResource(R.drawable.timeline_rounded),
                                        contentDescription = "Graph History Icon",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                    )
                                }

                                Column {
                                    Text(
                                        text = "Battery Graph History",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        text = "Set how much data to show on the graph",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = "History Duration",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    val durationText = if (historyCurrentValue >= 60) {
                                        val minutes = historyCurrentValue.toInt() / 60
                                        val seconds = historyCurrentValue.toInt() % 60
                                        if (seconds == 0) "${minutes}m" else "${minutes}m ${seconds}s"
                                    } else {
                                        "${historyCurrentValue.toInt()}s"
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = durationText,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                        AnimatedVisibility(
                                            visible = historyCurrentValue != 60.0f,
                                            enter = slideInHorizontally(
                                                animationSpec = MaterialTheme.motionScheme.slowSpatialSpec(),
                                            ) { it } + expandHorizontally(
                                                animationSpec = MaterialTheme.motionScheme.slowSpatialSpec(),
                                            ) + fadeIn(
                                                animationSpec = MaterialTheme.motionScheme.slowEffectsSpec(),
                                            ),
                                            exit = slideOutHorizontally(
                                                animationSpec = MaterialTheme.motionScheme.slowSpatialSpec(),
                                            ) { it } + shrinkHorizontally(
                                                animationSpec = MaterialTheme.motionScheme.slowSpatialSpec(),
                                            ) + fadeOut(
                                                animationSpec = MaterialTheme.motionScheme.slowEffectsSpec(),
                                            ),
                                        ) {
                                            Row {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                IconButton(
                                                    onClick = rememberHapticOnClick {
                                                        viewModel.setBatteryGraphHistorySeconds(60)
                                                    },
                                                    modifier = Modifier.size(24.dp),
                                                ) {
                                                    Icon(
                                                        painter = painterResource(R.drawable.reset_settings_rounded),
                                                        contentDescription = "Reset to default",
                                                        modifier = Modifier.size(16.dp),
                                                        tint = MaterialTheme.colorScheme.primary,
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Slider(
                                    state = historySliderState,
                                    modifier = Modifier.fillMaxWidth(),
                                    track = {
                                        SliderDefaults.Track(
                                            sliderState = historySliderState,
                                            modifier = Modifier.height(36.dp),
                                            trackCornerSize = 12.dp,
                                        )
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
