package com.rve.systemmonitor.ui.screens

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rve.systemmonitor.R
import com.rve.systemmonitor.service.SystemOverlayService
import com.rve.systemmonitor.ui.components.ExitUntilCollapsedMediumTopAppBar
import com.rve.systemmonitor.ui.components.haptic.hapticClickable
import com.rve.systemmonitor.ui.components.haptic.rememberHapticOnClick
import com.rve.systemmonitor.ui.components.haptic.rememberHapticOnValueChange
import com.rve.systemmonitor.ui.viewmodel.OverlaySettingsViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun OverlaySettingsScreen(viewModel: OverlaySettingsViewModel = hiltViewModel(), onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val coroutineScope = rememberCoroutineScope()
    val snapAnimationSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()

    val isFpsEnabled by viewModel.isFpsEnabled.collectAsStateWithLifecycle()
    val isRamPercentageEnabled by viewModel.isRamPercentageEnabled.collectAsStateWithLifecycle()
    val isRamGbEnabled by viewModel.isRamGbEnabled.collectAsStateWithLifecycle()
    val updateIntervalMillis by viewModel.overlayUpdateInterval.collectAsStateWithLifecycle()
    val textSize by viewModel.overlayTextSize.collectAsStateWithLifecycle()
    val bgOpacity by viewModel.overlayBgOpacity.collectAsStateWithLifecycle()
    val padding by viewModel.overlayPadding.collectAsStateWithLifecycle()
    val textColor by viewModel.overlayTextColor.collectAsStateWithLifecycle()
    val isVerticalLayout by viewModel.isVerticalLayout.collectAsStateWithLifecycle()
    val cornerRadius by viewModel.overlayCornerRadius.collectAsStateWithLifecycle()

    val isAnyMetricEnabled = isFpsEnabled || isRamPercentageEnabled || isRamGbEnabled
    val appearanceAlpha by animateFloatAsState(
        targetValue = if (isAnyMetricEnabled) 1f else 0.5f,
        label = "Appearance Alpha Animation",
    )
    val cardBgAlpha by animateFloatAsState(
        targetValue = if (isAnyMetricEnabled) 0.7f else 0.35f,
        label = "Card BG Alpha Animation",
    )

    var isServiceRunning by remember {
        mutableStateOf(isServiceRunning(context, SystemOverlayService::class.java))
    }

    var hasOverlayPermission by remember {
        mutableStateOf(Settings.canDrawOverlays(context))
    }

    fun updateService(
        fps: Boolean = isFpsEnabled,
        ramPercentage: Boolean = isRamPercentageEnabled,
        ramGb: Boolean = isRamGbEnabled,
        interval: Long = updateIntervalMillis,
        size: Float = textSize,
        opacity: Float = bgOpacity,
        padd: Int = padding,
        color: Int = textColor,
        vertical: Boolean = isVerticalLayout,
        radius: Int = cornerRadius,
    ) {
        val ramEnabled = ramPercentage || ramGb
        if (fps || ramEnabled) {
            if (Settings.canDrawOverlays(context)) {
                val intent = Intent(context, SystemOverlayService::class.java).apply {
                    putExtra("update_delay", interval)
                    putExtra("show_fps", fps)
                    putExtra("show_ram", ramEnabled)
                    putExtra("show_ram_percentage", ramPercentage)
                    putExtra("show_ram_gb", ramGb)
                    putExtra("text_size", size)
                    putExtra("bg_opacity", opacity)
                    putExtra("padding", padd)
                    putExtra("text_color", color)
                    putExtra("is_vertical", vertical)
                    putExtra("corner_radius", radius)
                }
                context.startForegroundService(intent)
                isServiceRunning = true
            } else {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    "package:${context.packageName}".toUri(),
                )
                context.startActivity(intent)
            }
        } else {
            context.stopService(Intent(context, SystemOverlayService::class.java))
            isServiceRunning = false
        }
    }

    val delaySliderState = rememberSliderState(
        value = (updateIntervalMillis / 1000f).coerceIn(0.5f, 5f),
        steps = 8,
        valueRange = 0.5f..5f,
    )
    var overlayCurrentValue by rememberSaveable(updateIntervalMillis) { mutableFloatStateOf(updateIntervalMillis / 1000f) }
    var delayAnimateJob: Job? by remember { mutableStateOf(null) }

    LaunchedEffect(updateIntervalMillis) {
        if (!delaySliderState.isDragging) {
            delaySliderState.value = updateIntervalMillis / 1000f
            overlayCurrentValue = updateIntervalMillis / 1000f
        }
    }

    delaySliderState.shouldAutoSnap = false
    delaySliderState.onValueChange = rememberHapticOnValueChange { newValue ->
        overlayCurrentValue = newValue
        if (delaySliderState.isDragging) {
            delayAnimateJob?.cancel()
            delaySliderState.value = newValue
        }
    }

    delaySliderState.onValueChangeFinished = {
        delayAnimateJob = coroutineScope.launch {
            animate(
                initialValue = delaySliderState.value,
                targetValue = overlayCurrentValue,
                animationSpec = snapAnimationSpec,
            ) { value, _ ->
                delaySliderState.value = value
            }
            viewModel.setOverlayUpdateInterval((overlayCurrentValue * 1000).toLong())

            if (isServiceRunning) {
                updateService(interval = (overlayCurrentValue * 1000).toLong())
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasOverlayPermission = Settings.canDrawOverlays(context)
                isServiceRunning = isServiceRunning(context, SystemOverlayService::class.java)

                if (!hasOverlayPermission && isServiceRunning) {
                    context.stopService(Intent(context, SystemOverlayService::class.java))
                    isServiceRunning = false
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            ExitUntilCollapsedMediumTopAppBar(
                title = "Overlay Settings",
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
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "Metrics",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp, start = 8.dp),
                    )

                    MetricToggleCard(
                        title = "FPS",
                        description = "Show real-time frame rate",
                        icon = R.drawable.sixty_fps_select_rounded,
                        isEnabled = isFpsEnabled,
                        hasPermission = hasOverlayPermission,
                        onClick = rememberHapticOnClick {
                            if (!hasOverlayPermission && !isFpsEnabled) {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    "package:${context.packageName}".toUri(),
                                )
                                context.startActivity(intent)
                            } else {
                                val nextState = !isFpsEnabled
                                viewModel.setFpsEnabled(nextState)
                                updateService(fps = nextState)
                            }
                        },
                    )

                    MetricToggleCard(
                        title = "RAM Usage (GB)",
                        description = "Show real-time used and total RAM in GB",
                        icon = R.drawable.memory_alt_filled,
                        isEnabled = isRamGbEnabled,
                        hasPermission = hasOverlayPermission,
                        onClick = rememberHapticOnClick {
                            if (!hasOverlayPermission && !isRamGbEnabled) {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    "package:${context.packageName}".toUri(),
                                )
                                context.startActivity(intent)
                            } else {
                                val nextState = !isRamGbEnabled
                                viewModel.setRamGbEnabled(nextState)
                                updateService(ramGb = nextState)
                            }
                        },
                    )

                    MetricToggleCard(
                        title = "RAM Percentage",
                        description = "Show real-time percentage RAM usage",
                        icon = R.drawable.memory_alt_filled,
                        isEnabled = isRamPercentageEnabled,
                        hasPermission = hasOverlayPermission,
                        onClick = rememberHapticOnClick {
                            if (!hasOverlayPermission && !isRamPercentageEnabled) {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    "package:${context.packageName}".toUri(),
                                )
                                context.startActivity(intent)
                            } else {
                                val nextState = !isRamPercentageEnabled
                                viewModel.setRamPercentageEnabled(nextState)
                                updateService(ramPercentage = nextState)
                            }
                        },
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer { alpha = appearanceAlpha },
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = "Layout",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 8.dp),
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            LayoutOptionCard(
                                title = "Horizontal",
                                isSelected = !isVerticalLayout,
                                enabled = isAnyMetricEnabled,
                                onClick = {
                                    if (isAnyMetricEnabled) {
                                        viewModel.setVerticalLayout(false)
                                        if (isServiceRunning) updateService(vertical = false)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                            ) {
                                val indicatorColor by animateColorAsState(
                                    targetValue = if (!isVerticalLayout)
                                        MaterialTheme.colorScheme.onPrimary
                                    else
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                    label = "Horizontal Layout Indicator Color",
                                    animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
                                )
                                Row(
                                    modifier = Modifier.size(40.dp, 20.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(indicatorColor),
                                    )
                                    Box(
                                        modifier = Modifier.weight(1f)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(indicatorColor),
                                    )
                                }
                            }

                            LayoutOptionCard(
                                title = "Vertical",
                                isSelected = isVerticalLayout,
                                enabled = isAnyMetricEnabled,
                                onClick = {
                                    if (isAnyMetricEnabled) {
                                        viewModel.setVerticalLayout(true)
                                        if (isServiceRunning) updateService(vertical = true)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                            ) {
                                val indicatorColor by animateColorAsState(
                                    targetValue = if (isVerticalLayout)
                                        MaterialTheme.colorScheme.onPrimary
                                    else
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                    label = "Vertical Layout Indicator Color",
                                    animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
                                )
                                Column(
                                    modifier = Modifier.size(20.dp, 40.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Box(
                                        Modifier.weight(1f)
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(indicatorColor),
                                    )
                                    Box(
                                        Modifier.weight(1f)
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(indicatorColor),
                                    )
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
                        text = "Configuration",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp),
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = cardBgAlpha),
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(20.dp)
                                .graphicsLayer { alpha = appearanceAlpha },
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
                                        .background(
                                            if (isAnyMetricEnabled)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.acute_filled),
                                        contentDescription = "Update Interval Icon",
                                        tint = if (isAnyMetricEnabled)
                                            MaterialTheme.colorScheme.onPrimary
                                        else
                                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.38f),
                                    )
                                }

                                Column {
                                    Text(
                                        text = "Update Interval",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isAnyMetricEnabled)
                                            MaterialTheme.colorScheme.onSurface
                                        else
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                    )
                                    Text(
                                        text = "Adjust how often overlay metrics refresh",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isAnyMetricEnabled)
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
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
                                        color = if (isAnyMetricEnabled)
                                            MaterialTheme.colorScheme.onSurface
                                        else
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        val displayValue = if (overlayCurrentValue % 1f == 0f) {
                                            overlayCurrentValue.toInt().toString()
                                        } else {
                                            overlayCurrentValue.toString()
                                        }
                                        Text(
                                            text = "${displayValue}s",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isAnyMetricEnabled)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),
                                        )
                                        AnimatedVisibility(
                                            visible = isAnyMetricEnabled && overlayCurrentValue != 1.0f,
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
                                                        viewModel.setOverlayUpdateInterval(1000L)
                                                        if (isServiceRunning) updateService(interval = 1000L)
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
                                    state = delaySliderState,
                                    enabled = isAnyMetricEnabled,
                                    modifier = Modifier.fillMaxWidth(),
                                    track = {
                                        SliderDefaults.Track(
                                            sliderState = delaySliderState,
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

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = "Appearance",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp),
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = cardBgAlpha),
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(20.dp)
                                .graphicsLayer { alpha = appearanceAlpha },
                        ) {
                            Text(
                                text = "Text Color",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 16.dp),
                            )
                            val colors = listOf(
                                Color.Green,
                                Color.White,
                                Color.Red,
                                Color.Cyan,
                                Color.Yellow,
                                Color.Magenta,
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                colors.forEach { color ->
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                            .border(
                                                width = if (textColor == color.toArgb()) 3.dp else 0.dp,
                                                color = MaterialTheme.colorScheme.primary,
                                                shape = CircleShape,
                                            )
                                            .hapticClickable(enabled = isAnyMetricEnabled) {
                                                viewModel.setOverlayTextColor(color.toArgb())
                                                if (isServiceRunning) updateService(color = color.toArgb())
                                            },
                                    )
                                }
                            }
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = cardBgAlpha),
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(20.dp)
                                .graphicsLayer { alpha = appearanceAlpha },
                            verticalArrangement = Arrangement.spacedBy(20.dp),
                        ) {
                            AppearanceSlider(
                                label = "Text Size",
                                value = textSize,
                                defaultValue = 14f,
                                valueRange = 10f..24f,
                                enabled = isAnyMetricEnabled,
                                onValueChange = {
                                    viewModel.setOverlayTextSize(it)
                                    if (isServiceRunning) updateService(size = it)
                                },
                                onReset = {
                                    viewModel.setOverlayTextSize(14f)
                                    if (isServiceRunning) updateService(size = 14f)
                                },
                                valueDisplay = "${textSize.toInt()} sp",
                            )

                            AppearanceSlider(
                                label = "Background Opacity",
                                value = bgOpacity,
                                defaultValue = 0.5f,
                                valueRange = 0f..1f,
                                enabled = isAnyMetricEnabled,
                                onValueChange = {
                                    viewModel.setOverlayBgOpacity(it)
                                    if (isServiceRunning) updateService(opacity = it)
                                },
                                onReset = {
                                    viewModel.setOverlayBgOpacity(0.5f)
                                    if (isServiceRunning) updateService(opacity = 0.5f)
                                },
                                valueDisplay = "${(bgOpacity * 100).toInt()}%",
                            )

                            AppearanceSlider(
                                label = "Padding",
                                value = padding.toFloat(),
                                defaultValue = 16f,
                                valueRange = 0f..32f,
                                enabled = isAnyMetricEnabled,
                                onValueChange = {
                                    viewModel.setOverlayPadding(it.toInt())
                                    if (isServiceRunning) updateService(padd = it.toInt())
                                },
                                onReset = {
                                    viewModel.setOverlayPadding(16)
                                    if (isServiceRunning) updateService(padd = 16)
                                },
                                valueDisplay = "$padding px",
                            )

                            AppearanceSlider(
                                label = "Corner Radius",
                                value = cornerRadius.toFloat(),
                                defaultValue = 8f,
                                valueRange = 0f..64f,
                                enabled = isAnyMetricEnabled,
                                onValueChange = {
                                    viewModel.setOverlayCornerRadius(it.toInt())
                                    if (isServiceRunning) updateService(radius = it.toInt())
                                },
                                onReset = {
                                    viewModel.setOverlayCornerRadius(8)
                                    if (isServiceRunning) updateService(radius = 8)
                                },
                                valueDisplay = "$cornerRadius px",
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AppearanceSlider(
    label: String,
    value: Float,
    defaultValue: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    enabled: Boolean = true,
    onValueChange: (Float) -> Unit,
    onReset: () -> Unit,
    valueDisplay: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = valueDisplay,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),
                )
                AnimatedVisibility(
                    visible = enabled && value != defaultValue,
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
                            onClick = rememberHapticOnClick(onReset),
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
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            track = { sliderState ->
                SliderDefaults.Track(
                    sliderState = sliderState,
                    modifier = Modifier.height(36.dp),
                    trackCornerSize = 12.dp,
                )
            },
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LayoutOptionCard(
    modifier: Modifier = Modifier,
    title: String,
    isSelected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    val colorSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Color>()
    val dpSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Dp>()

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.38f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        },
        label = "Layout Option Background",
        animationSpec = colorSpec,
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) {
            if (enabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.38f)
        } else {
            if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
        },
        label = "Layout Option Content Color",
        animationSpec = colorSpec,
    )

    val animatedElevation by animateDpAsState(
        targetValue = if (isSelected && enabled) 4.dp else 0.dp,
        label = "Layout Option Elevation",
        animationSpec = dpSpec,
    )

    val animatedBorderWidth by animateDpAsState(
        targetValue = if (isSelected && enabled) 2.dp else 0.dp,
        label = "Layout Option Border Width",
        animationSpec = dpSpec,
    )

    Card(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = contentColor,
        ),
        border = BorderStroke(
            width = animatedBorderWidth,
            color = MaterialTheme.colorScheme.primary.copy(alpha = if (isSelected) 0.5f else 0f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = animatedElevation),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                content()
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp),
            )
        }
    }
}

@Composable
private fun MetricToggleCard(
    title: String,
    description: String,
    icon: Int,
    isEnabled: Boolean,
    hasPermission: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        interactionSource = interactionSource,
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
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
                    painter = painterResource(icon),
                    contentDescription = "$title Icon",
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (!hasPermission) {
                    Text(
                        text = "Requires 'Draw over other apps' permission",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                } else {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Switch(
                checked = isEnabled,
                onCheckedChange = null,
                enabled = hasPermission,
                interactionSource = if (hasPermission) interactionSource else null,
                thumbContent = {
                    Crossfade(
                        targetState = isEnabled,
                        animationSpec = MaterialTheme.motionScheme.slowEffectsSpec(),
                        label = "Switch Icon Fade",
                    ) { enabled ->
                        Icon(
                            painter = painterResource(if (enabled) R.drawable.check_rounded else R.drawable.close_rounded),
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize),
                        )
                    }
                },
            )
        }
    }
}

@Suppress("DEPRECATION")
private fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
    val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    for (service in manager.getRunningServices(Int.MAX_VALUE)) {
        if (serviceClass.name == service.service.className) {
            return true
        }
    }
    return false
}
