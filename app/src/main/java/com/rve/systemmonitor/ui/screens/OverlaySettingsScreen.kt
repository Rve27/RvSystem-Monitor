package com.rve.systemmonitor.ui.screens

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.core.animate
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composables.icons.materialsymbols.roundedfilled.R.drawable.materialsymbols_ic_layers_rounded_filled
import com.composables.icons.materialsymbols.roundedfilled.R.drawable.materialsymbols_ic_speed_rounded_filled
import com.rve.systemmonitor.service.SystemOverlayService
import com.rve.systemmonitor.ui.components.ExitUntilCollapsedMediumTopAppBar
import com.rve.systemmonitor.ui.viewmodel.OverlaySettingsViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun OverlaySettingsScreen(viewModel: OverlaySettingsViewModel = hiltViewModel(), onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val coroutineScope = rememberCoroutineScope()
    val snapAnimationSpec = MaterialTheme.motionScheme.fastEffectsSpec<Float>()

    val fpsInteractionSource = remember { MutableInteractionSource() }
    val ramInteractionSource = remember { MutableInteractionSource() }

    val isFpsEnabled by viewModel.isFpsEnabled.collectAsStateWithLifecycle()
    val isRamEnabled by viewModel.isRamEnabled.collectAsStateWithLifecycle()
    val updateIntervalMillis by viewModel.overlayUpdateInterval.collectAsStateWithLifecycle()

    var isServiceRunning by remember {
        mutableStateOf(isServiceRunning(context, SystemOverlayService::class.java))
    }

    var hasOverlayPermission by remember {
        mutableStateOf(Settings.canDrawOverlays(context))
    }

    fun updateService(fps: Boolean, ram: Boolean) {
        if (fps || ram) {
            if (Settings.canDrawOverlays(context)) {
                val intent = Intent(context, SystemOverlayService::class.java).apply {
                    putExtra("update_delay", updateIntervalMillis)
                    putExtra("show_fps", fps)
                    putExtra("show_ram", ram)
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
        value = (updateIntervalMillis / 1000f).coerceIn(1f, 5f),
        steps = 3,
        valueRange = 1f..5f,
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
    delaySliderState.onValueChange = { newValue ->
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
            viewModel.setOverlayUpdateInterval(overlayCurrentValue.toLong() * 1000)

            if (isServiceRunning) {
                updateService(isFpsEnabled, isRamEnabled)
            }
        }
    }

    // Refresh permission status when returning to the app
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

                    // FPS Toggle
                    MetricToggleCard(
                        title = "FPS",
                        description = "Show real-time frame rate",
                        icon = materialsymbols_ic_speed_rounded_filled,
                        isEnabled = isFpsEnabled,
                        hasPermission = hasOverlayPermission,
                        interactionSource = fpsInteractionSource,
                        onClick = {
                            val nextState = !isFpsEnabled
                            viewModel.setFpsEnabled(nextState)
                            updateService(nextState, isRamEnabled)
                        },
                    )

                    // RAM Toggle
                    MetricToggleCard(
                        title = "RAM Usage",
                        description = "Show real-time memory usage",
                        icon = materialsymbols_ic_layers_rounded_filled,
                        isEnabled = isRamEnabled,
                        hasPermission = hasOverlayPermission,
                        interactionSource = ramInteractionSource,
                        onClick = {
                            val nextState = !isRamEnabled
                            viewModel.setRamEnabled(nextState)
                            updateService(isFpsEnabled, nextState)
                        },
                    )
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
                                        painter = painterResource(materialsymbols_ic_speed_rounded_filled),
                                        contentDescription = "Update Interval Icon",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                    )
                                }

                                Column {
                                    Text(
                                        text = "Update Interval",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        text = "Adjust how often overlay metrics refresh",
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
                                    Text(
                                        text = "${overlayCurrentValue.toInt()}s",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }

                                Slider(
                                    state = delaySliderState,
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
    interactionSource: MutableInteractionSource,
    onClick: () -> Unit,
) {
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
                interactionSource = interactionSource,
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
