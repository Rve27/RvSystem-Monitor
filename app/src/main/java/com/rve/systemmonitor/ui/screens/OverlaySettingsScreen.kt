package com.rve.systemmonitor.ui.screens

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rve.systemmonitor.R
import com.rve.systemmonitor.domain.model.OverlayPosition
import com.rve.systemmonitor.service.SystemOverlayService
import com.rve.systemmonitor.ui.components.ExitUntilCollapsedMediumTopAppBar
import com.rve.systemmonitor.ui.components.card.SettingsSliderCard
import com.rve.systemmonitor.ui.components.haptic.hapticClickable
import com.rve.systemmonitor.ui.components.haptic.rememberHapticOnClick
import com.rve.systemmonitor.ui.components.haptic.rememberHapticOnValueChange
import com.rve.systemmonitor.ui.viewmodel.OverlaySettingsViewModel
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun OverlaySettingsScreen(
    viewModel: OverlaySettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToAutoToggle: () -> Unit,
) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val coroutineScope = rememberCoroutineScope()
    val snapAnimationSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()

    val isOverlayEnabled by viewModel.isOverlayEnabled.collectAsStateWithLifecycle()
    val isAutoToggleEnabled by viewModel.isAutoToggleEnabled.collectAsStateWithLifecycle()
    val isFpsEnabled by viewModel.isFpsEnabled.collectAsStateWithLifecycle()
    val isRamPercentageEnabled by viewModel.isRamPercentageEnabled.collectAsStateWithLifecycle()
    val isRamGbEnabled by viewModel.isRamGbEnabled.collectAsStateWithLifecycle()
    val isBatteryTempEnabled by viewModel.isBatteryTempEnabled.collectAsStateWithLifecycle()
    val isCpuTempEnabled by viewModel.isCpuTempEnabled.collectAsStateWithLifecycle()
    val updateIntervalMillis by viewModel.overlayUpdateInterval.collectAsStateWithLifecycle()
    val textSize by viewModel.overlayTextSize.collectAsStateWithLifecycle()
    val bgOpacity by viewModel.overlayBgOpacity.collectAsStateWithLifecycle()
    val padding by viewModel.overlayPadding.collectAsStateWithLifecycle()
    val textColor by viewModel.overlayTextColor.collectAsStateWithLifecycle()
    val isVerticalLayout by viewModel.isVerticalLayout.collectAsStateWithLifecycle()
    val cornerRadius by viewModel.overlayCornerRadius.collectAsStateWithLifecycle()
    val overlayPosition by viewModel.overlayPosition.collectAsStateWithLifecycle()

    val isShizukuAvailable by viewModel.isShizukuAvailable.collectAsStateWithLifecycle()
    val hasShizukuPermission by viewModel.hasShizukuPermission.collectAsStateWithLifecycle()
    val useShizuku by viewModel.useShizuku.collectAsStateWithLifecycle()

    val isOverlayActive = isOverlayEnabled || isAutoToggleEnabled

    val appearanceAlpha by animateFloatAsState(
        targetValue = if (isOverlayActive) 1f else 0.5f,
        label = "Appearance Alpha Animation",
    )
    val cardBgAlpha by animateFloatAsState(
        targetValue = if (isOverlayActive) 0.7f else 0.35f,
        label = "Card BG Alpha Animation",
    )

    var isServiceRunning by remember {
        mutableStateOf(SystemOverlayService.isRunning)
    }

    var hasOverlayPermission by remember {
        mutableStateOf(Settings.canDrawOverlays(context))
    }

    fun updateService(
        overlayEnabled: Boolean = isOverlayEnabled,
        fps: Boolean = isFpsEnabled,
        ramPercentage: Boolean = isRamPercentageEnabled,
        ramGb: Boolean = isRamGbEnabled,
        batteryTemp: Boolean = isBatteryTempEnabled,
        cpuTemp: Boolean = isCpuTempEnabled,
        interval: Long = updateIntervalMillis,
        size: Float = textSize,
        opacity: Float = bgOpacity,
        padd: Int = padding,
        color: Int = textColor,
        vertical: Boolean = isVerticalLayout,
        radius: Int = cornerRadius,
    ) {
        val ramEnabled = ramPercentage || ramGb
        if (overlayEnabled) {
            if (Settings.canDrawOverlays(context)) {
                val intent = Intent(context, SystemOverlayService::class.java).apply {
                    putExtra("update_delay", interval)
                    putExtra("show_fps", fps)
                    putExtra("show_ram", ramEnabled)
                    putExtra("show_ram_percentage", ramPercentage)
                    putExtra("show_ram_gb", ramGb)
                    putExtra("show_battery_temp", batteryTemp)
                    putExtra("show_cpu_temp", cpuTemp)
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
                isServiceRunning = SystemOverlayService.isRunning

                if (!hasOverlayPermission && (isOverlayEnabled || isServiceRunning)) {
                    viewModel.disableAll()
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
                title = stringResource(R.string.title_floating_overlay_settings),
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
            val isShizukuServiceReady = isShizukuAvailable && hasShizukuPermission

            if (!isShizukuServiceReady) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(32.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        ),
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(20.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_shizuku),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp),
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.label_shizuku),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                val statusText = when {
                                    !isShizukuAvailable -> stringResource(R.string.shizuku_status_not_running)
                                    !hasShizukuPermission -> stringResource(R.string.shizuku_status_permission_required)
                                    else -> ""
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.error),
                                    )
                                    Text(
                                        text = statusText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Medium,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                MetricToggleCard(
                    title = stringResource(R.string.overlay_enable_title),
                    isEnabled = isOverlayEnabled,
                    hasPermission = hasOverlayPermission,
                    showPermissionWarning = true,
                    onClick = rememberHapticOnClick {
                        if (!hasOverlayPermission && !isOverlayEnabled) {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                "package:${context.packageName}".toUri(),
                            )
                            context.startActivity(intent)
                        } else {
                            val nextState = !isOverlayEnabled
                            viewModel.setOverlayEnabled(nextState)
                            updateService(overlayEnabled = nextState)
                        }
                    },
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    onClick = rememberHapticOnClick {
                        onNavigateToAutoToggle()
                    },
                ) {
                    Row(
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.app_registration),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(32.dp),
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.settings_auto_toggle_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = stringResource(R.string.settings_auto_toggle_description),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = stringResource(R.string.overlay_label_metrics),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp, start = 8.dp),
                    )

                    MetricToggleCard(
                        title = stringResource(R.string.overlay_fps),
                        description = stringResource(R.string.overlay_fps_desc),
                        icon = R.drawable.sixty_fps_select_rounded,
                        isEnabled = isFpsEnabled,
                        hasPermission = hasOverlayPermission,
                        enabled = isOverlayActive && useShizuku && isShizukuServiceReady,
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
                        title = stringResource(R.string.overlay_ram_gb),
                        description = stringResource(R.string.overlay_ram_gb_desc),
                        icon = R.drawable.memory_alt_filled,
                        isEnabled = isRamGbEnabled,
                        hasPermission = hasOverlayPermission,
                        enabled = isOverlayActive,
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
                        title = stringResource(R.string.overlay_ram_percentage),
                        description = stringResource(R.string.overlay_ram_percentage_desc),
                        icon = R.drawable.memory_alt_filled,
                        isEnabled = isRamPercentageEnabled,
                        hasPermission = hasOverlayPermission,
                        enabled = isOverlayActive,
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

                    MetricToggleCard(
                        title = stringResource(R.string.overlay_battery_temp),
                        description = stringResource(R.string.overlay_battery_temp_desc),
                        icon = R.drawable.device_thermostat_filled,
                        isEnabled = isBatteryTempEnabled,
                        hasPermission = hasOverlayPermission,
                        enabled = isOverlayActive,
                        onClick = rememberHapticOnClick {
                            if (!hasOverlayPermission && !isBatteryTempEnabled) {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    "package:${context.packageName}".toUri(),
                                )
                                context.startActivity(intent)
                            } else {
                                val nextState = !isBatteryTempEnabled
                                viewModel.setBatteryTempEnabled(nextState)
                                updateService(batteryTemp = nextState)
                            }
                        },
                    )

                    MetricToggleCard(
                        title = stringResource(R.string.overlay_cpu_temp),
                        description = stringResource(R.string.overlay_cpu_temp_desc),
                        icon = R.drawable.device_thermostat_filled,
                        isEnabled = isCpuTempEnabled,
                        hasPermission = hasOverlayPermission,
                        enabled = isOverlayActive,
                        onClick = rememberHapticOnClick {
                            if (!hasOverlayPermission && !isCpuTempEnabled) {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    "package:${context.packageName}".toUri(),
                                )
                                context.startActivity(intent)
                            } else {
                                val nextState = !isCpuTempEnabled
                                viewModel.setCpuTempEnabled(nextState)
                                updateService(cpuTemp = nextState)
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
                            text = stringResource(R.string.overlay_label_layout),
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
                                title = stringResource(R.string.overlay_layout_horizontal),
                                isSelected = !isVerticalLayout,
                                enabled = isOverlayActive,
                                onClick = {
                                    if (isOverlayActive) {
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
                                title = stringResource(R.string.overlay_layout_vertical),
                                isSelected = isVerticalLayout,
                                enabled = isOverlayActive,
                                onClick = {
                                    if (isOverlayActive) {
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

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer { alpha = appearanceAlpha },
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.overlay_position),
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
                                title = stringResource(R.string.overlay_position_free),
                                isSelected = overlayPosition == OverlayPosition.FREE,
                                enabled = isOverlayActive,
                                onClick = {
                                    if (isOverlayActive) {
                                        viewModel.setOverlayPosition(OverlayPosition.FREE)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                            ) {
                                val indicatorColor by animateColorAsState(
                                    targetValue = if (overlayPosition == OverlayPosition.FREE)
                                        MaterialTheme.colorScheme.onPrimary
                                    else
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                    label = "Free Indicator",
                                    animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
                                )
                                Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .size(24.dp, 12.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(indicatorColor),
                                    )
                                }
                            }

                            LayoutOptionCard(
                                title = stringResource(R.string.overlay_position_top_center),
                                isSelected = overlayPosition == OverlayPosition.TOP_CENTER,
                                enabled = isOverlayActive,
                                onClick = {
                                    if (isOverlayActive) {
                                        viewModel.setOverlayPosition(OverlayPosition.TOP_CENTER)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                            ) {
                                val indicatorColor by animateColorAsState(
                                    targetValue = if (overlayPosition == OverlayPosition.TOP_CENTER)
                                        MaterialTheme.colorScheme.onPrimary
                                    else
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                    label = "Top Center Indicator",
                                    animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
                                )
                                Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopCenter)
                                            .size(24.dp, 12.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(indicatorColor),
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            LayoutOptionCard(
                                title = stringResource(R.string.overlay_position_top_left),
                                isSelected = overlayPosition == OverlayPosition.TOP_LEFT,
                                enabled = isOverlayActive,
                                onClick = {
                                    if (isOverlayActive) {
                                        viewModel.setOverlayPosition(OverlayPosition.TOP_LEFT)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                            ) {
                                val indicatorColor by animateColorAsState(
                                    targetValue = if (overlayPosition == OverlayPosition.TOP_LEFT)
                                        MaterialTheme.colorScheme.onPrimary
                                    else
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                    label = "Top Left Indicator",
                                    animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
                                )
                                Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .size(24.dp, 12.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(indicatorColor),
                                    )
                                }
                            }

                            LayoutOptionCard(
                                title = stringResource(R.string.overlay_position_top_right),
                                isSelected = overlayPosition == OverlayPosition.TOP_RIGHT,
                                enabled = isOverlayActive,
                                onClick = {
                                    if (isOverlayActive) {
                                        viewModel.setOverlayPosition(OverlayPosition.TOP_RIGHT)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                            ) {
                                val indicatorColor by animateColorAsState(
                                    targetValue = if (overlayPosition == OverlayPosition.TOP_RIGHT)
                                        MaterialTheme.colorScheme.onPrimary
                                    else
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                    label = "Top Right Indicator",
                                    animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
                                )
                                Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(24.dp, 12.dp)
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
                        text = stringResource(R.string.overlay_label_configuration),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                    SettingsSliderCard(
                        title = stringResource(R.string.overlay_update_interval),
                        description = stringResource(R.string.overlay_update_interval_desc),
                        iconRes = R.drawable.acute_filled,
                        sliderState = delaySliderState,
                        currentDisplayValue = overlayCurrentValue,
                        displayValueFormatter = { value ->
                            if (value % 1f == 0f) {
                                value.toInt().toString() + "s"
                            } else {
                                String.format(Locale.US, "%.1fs", value)
                            }
                        },
                        onReset = {
                            viewModel.setOverlayUpdateInterval(1000L)
                            if (isServiceRunning) updateService(interval = 1000L)
                        },
                        isResetVisible = overlayCurrentValue != 1.0f,
                        enabled = isOverlayActive,
                        alpha = appearanceAlpha,
                    )
                }
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = stringResource(R.string.overlay_label_appearance),
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
                                text = stringResource(R.string.overlay_text_color),
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
                                    val isSelected = textColor == color.toArgb()
                                    val animatedBorderWidth by animateDpAsState(
                                        targetValue = if (isSelected) 3.dp else 0.dp,
                                        label = "Color Selection Border Width Animation",
                                        animationSpec = MaterialTheme.motionScheme.slowEffectsSpec(),
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                            .border(
                                                width = animatedBorderWidth,
                                                color = MaterialTheme.colorScheme.primary,
                                                shape = CircleShape,
                                            )
                                            .hapticClickable(enabled = isOverlayActive) {
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
                                label = stringResource(R.string.overlay_text_size),
                                value = textSize,
                                defaultValue = 14f,
                                valueRange = 10f..24f,
                                enabled = isOverlayActive,
                                onValueChange = {
                                    viewModel.setOverlayTextSize(it)
                                },
                                onReset = {
                                    viewModel.setOverlayTextSize(14f)
                                },
                                valueDisplay = "${textSize.toInt()} sp",
                            )

                            AppearanceSlider(
                                label = stringResource(R.string.overlay_bg_opacity),
                                value = bgOpacity,
                                defaultValue = 0.5f,
                                valueRange = 0f..1f,
                                enabled = isOverlayActive,
                                onValueChange = {
                                    viewModel.setOverlayBgOpacity(it)
                                },
                                onReset = {
                                    viewModel.setOverlayBgOpacity(0.5f)
                                },
                                valueDisplay = "${(bgOpacity * 100).toInt()}%",
                            )

                            AppearanceSlider(
                                label = stringResource(R.string.overlay_padding),
                                value = padding.toFloat(),
                                defaultValue = 16f,
                                valueRange = 0f..32f,
                                enabled = isOverlayActive,
                                onValueChange = {
                                    viewModel.setOverlayPadding(it.toInt())
                                },
                                onReset = {
                                    viewModel.setOverlayPadding(16)
                                },
                                valueDisplay = "$padding px",
                            )

                            AppearanceSlider(
                                label = stringResource(R.string.overlay_corner_radius),
                                value = cornerRadius.toFloat(),
                                defaultValue = 8f,
                                valueRange = 0f..64f,
                                enabled = isOverlayActive,
                                onValueChange = {
                                    viewModel.setOverlayCornerRadius(it.toInt())
                                },
                                onReset = {
                                    viewModel.setOverlayCornerRadius(8)
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
                                contentDescription = stringResource(R.string.cd_reset_to_default),
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
    val colorSpec = MaterialTheme.motionScheme.slowEffectsSpec<Color>()

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
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
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
    description: String? = null,
    icon: Int? = null,
    isEnabled: Boolean,
    hasPermission: Boolean,
    enabled: Boolean = true,
    showPermissionWarning: Boolean = false,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val cardAlpha by animateFloatAsState(
        targetValue = if (enabled && hasPermission) 1f else 0.5f,
        label = "Metric Card Alpha",
    )

    Card(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = cardAlpha },
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
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (enabled && hasPermission)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(icon),
                        contentDescription = stringResource(R.string.cd_metric_icon, title),
                        tint = if (enabled && hasPermission)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.38f),
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (enabled && hasPermission)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                )
                if (!hasPermission && showPermissionWarning) {
                    Text(
                        text = stringResource(R.string.overlay_permission_required),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                } else if (description != null) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (enabled && hasPermission)
                            MaterialTheme.colorScheme.onSurfaceVariant
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                    )
                }
            }

            Switch(
                checked = isEnabled,
                onCheckedChange = null,
                enabled = hasPermission && enabled,
                interactionSource = if (hasPermission && enabled) interactionSource else null,
                colors = SwitchDefaults.colors(
                    checkedIconColor = MaterialTheme.colorScheme.primary,
                ),
                thumbContent = {
                    Crossfade(
                        targetState = isEnabled,
                        animationSpec = MaterialTheme.motionScheme.slowEffectsSpec(),
                        label = "Switch Icon Fade",
                    ) { enabledState ->
                        Icon(
                            painter = painterResource(if (enabledState) R.drawable.check_rounded else R.drawable.close_rounded),
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize),
                        )
                    }
                },
            )
        }
    }
}
