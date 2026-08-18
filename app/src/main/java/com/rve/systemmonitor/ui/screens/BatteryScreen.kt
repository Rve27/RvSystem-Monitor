package com.rve.systemmonitor.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.rve.systemmonitor.R
import com.rve.systemmonitor.domain.model.Battery
import com.rve.systemmonitor.domain.model.BatteryDataPoint
import com.rve.systemmonitor.ui.components.card.OverviewCard
import com.rve.systemmonitor.ui.components.card.StandardCard
import com.rve.systemmonitor.ui.components.chip.BadgeChip
import com.rve.systemmonitor.ui.components.dialog.HelpBottomSheetContent
import com.rve.systemmonitor.ui.components.haptic.rememberHapticOnClick
import com.rve.systemmonitor.ui.components.item.InfoItem
import com.rve.systemmonitor.ui.components.layout.ScreenLazyColumn
import com.rve.systemmonitor.ui.components.row.TwoColumnInfoRow
import com.rve.systemmonitor.ui.navigation.TRANSITION_DURATION
import com.rve.systemmonitor.ui.utils.rememberLifecycleAwareState
import com.rve.systemmonitor.ui.viewmodel.BatteryViewModel
import kotlin.math.abs
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
fun BatteryScreen(isActive: Boolean, viewModel: BatteryViewModel = hiltViewModel()) {
    val batteryInfo by rememberLifecycleAwareState(isActive, viewModel.batteryInfo)
    val batteryHistory by rememberLifecycleAwareState(isActive, viewModel.batteryHistory)

    BatteryScreenContent(
        batteryInfo = batteryInfo,
        batteryHistory = batteryHistory,
        hasAlreadyAnimated = viewModel.hasAnimated,
        onAnimated = { viewModel.markAsAnimated() },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BatteryScreenContent(
    batteryInfo: Battery,
    batteryHistory: ImmutableList<BatteryDataPoint>,
    hasAlreadyAnimated: Boolean,
    onAnimated: () -> Unit,
) {
    var showHelpSheet by remember { mutableStateOf(false) }
    val sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden)

    if (showHelpSheet) {
        ModalBottomSheet(
            onDismissRequest = { showHelpSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            BatteryHelpContent()
        }
    }

    ScreenLazyColumn {
        item {
            BatteryOverviewCard(batteryInfo)
        }

        item {
            ChargingSpeedCard(
                battery = batteryInfo,
                history = batteryHistory,
                hasAnimated = hasAlreadyAnimated,
                onAnimated = onAnimated,
            )
        }

        item {
            BatteryDetailsCard(
                battery = batteryInfo,
                onHelpClick = { showHelpSheet = true },
            )
        }
    }
}

@Composable
private fun ChargingSpeedCard(battery: Battery, history: ImmutableList<BatteryDataPoint>, hasAnimated: Boolean, onAnimated: () -> Unit) {
    val currentMA = abs(battery.current)
    val isCharging = battery.status == "Charging"
    val isDischarging = battery.status == "Discharging"

    val speedLabel = when {
        isCharging -> stringResource(R.string.battery_charging_speed)
        isDischarging -> stringResource(R.string.battery_discharging_speed)
        else -> stringResource(R.string.battery_current_speed)
    }

    val accentColor = MaterialTheme.colorScheme.primary

    StandardCard {
        Column(
            modifier = Modifier.animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = speedLabel,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    val displaySign = if (isDischarging && battery.current != 0) "-" else ""
                    Text(
                        text = if (battery.current != 0) "$displaySign$currentMA mA" else "0 mA",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                    )
                }

                BadgeChip(
                    text = String.format(LocalLocale.current.platformLocale, "%.2f W", battery.wattage),
                    containerColor = MaterialTheme.colorScheme.primary,
                    textColor = MaterialTheme.colorScheme.onPrimary,
                )
            }

            val currentSessionHistory = remember(history, battery.status) {
                history.takeLastWhile { it.status == battery.status }
            }

            val actualMax = remember(currentSessionHistory) {
                if (currentSessionHistory.isNotEmpty()) currentSessionHistory.maxOf { abs(it.mA) }.toFloat() else 0f
            }
            val renderMax = remember(actualMax) {
                actualMax.coerceAtLeast(1000f)
            }
            val minValInHistory = remember(currentSessionHistory) {
                if (currentSessionHistory.isNotEmpty()) currentSessionHistory.minOf { abs(it.mA) }.toFloat() else 0f
            }

            val enterTransition = if (hasAnimated) EnterTransition.None else fadeIn(animationSpec = tween(1000))

            AnimatedVisibility(
                visible = currentSessionHistory.size >= 2,
                enter = enterTransition,
                exit = fadeOut(),
            ) {
                LaunchedEffect(Unit) {
                    onAnimated()
                }
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                ) {
                    val width = constraints.maxWidth.toFloat()
                    val height = constraints.maxHeight.toFloat()
                    val density = LocalDensity.current
                    val strokeWidth = with(density) { 3.dp.toPx() }

                    val (linePath, fillPath) = remember(currentSessionHistory, renderMax, width, height) {
                        if (currentSessionHistory.size < 2) return@remember Path() to Path()

                        val minVal = 0f
                        val range = if (renderMax > 0) renderMax - minVal else 1f
                        val stepX = width / (currentSessionHistory.size - 1)

                        fun getY(value: Int): Float {
                            return height - ((abs(value).toFloat() - minVal) / range * height)
                        }

                        val p = Path()
                        p.moveTo(0f, getY(currentSessionHistory[0].mA))

                        for (i in 0 until currentSessionHistory.size - 1) {
                            val x1 = i * stepX
                            val y1 = getY(currentSessionHistory[i].mA)
                            val x2 = (i + 1) * stepX
                            val y2 = getY(currentSessionHistory[i + 1].mA)

                            val controlPoint1X = x1 + (x2 - x1) / 2
                            val controlPoint1Y = y1
                            val controlPoint2X = x1 + (x2 - x1) / 2
                            val controlPoint2Y = y2

                            p.cubicTo(
                                controlPoint1X,
                                controlPoint1Y,
                                controlPoint2X,
                                controlPoint2Y,
                                x2,
                                y2,
                            )
                        }

                        val fP = Path().apply {
                            addPath(p)
                            lineTo(width, height)
                            lineTo(0f, height)
                            close()
                        }
                        p to fP
                    }

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        if (currentSessionHistory.size > 1) {
                            drawPath(
                                path = linePath,
                                color = accentColor,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                            )

                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(accentColor.copy(alpha = 0.4f), Color.Transparent),
                                    endY = size.height,
                                ),
                            )
                        }
                    }

                    if (currentSessionHistory.size >= 2) {
                        val sign = if (isDischarging) "-" else ""
                        Text(
                            text = stringResource(R.string.battery_graph_max, "$sign${actualMax.toInt()}"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(top = 4.dp, start = 4.dp),
                        )
                        Text(
                            text = stringResource(R.string.battery_graph_min, "$sign${minValInHistory.toInt()}"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(bottom = 4.dp, start = 4.dp),
                        )

                        currentSessionHistory.forEachIndexed { index, point ->
                            if (index > 0 && point.status != currentSessionHistory[index - 1].status) {
                                val xRatio = index.toFloat() / (currentSessionHistory.size - 1)
                                val yRatio = (abs(point.mA).toFloat() / renderMax).coerceIn(0f, 1f)

                                val statusLabel = when (point.status) {
                                    "Charging",
                                    -> stringResource(R.string.battery_status_charging_label)

                                    "Discharging",
                                    -> stringResource(R.string.battery_status_discharging_label)

                                    else -> point.status.uppercase()
                                }
                                val statusColor = if (point.status ==
                                    "Charging"
                                ) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error

                                val xOffset = with(density) { (width * xRatio).toDp() }
                                val yOffset = with(density) { (height * (1f - yRatio)).toDp() }

                                Text(
                                    text = statusLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 8.sp,
                                    ),
                                    color = statusColor.copy(alpha = 0.8f),
                                    modifier = Modifier
                                        .offset(
                                            x = xOffset - 20.dp,
                                            y = yOffset - 14.dp,
                                        ),
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
private fun BatteryOverviewCard(battery: Battery) {
    val isCharging = battery.status == "Charging"
    val context = LocalContext.current

    val batteryIcon = remember(battery.level, isCharging, battery.wattage) {
        if (isCharging) {
            when {
                battery.wattage >= 25.0 -> R.drawable.bolt_boost_filled
                battery.wattage >= 15.0 -> R.drawable.bolt_filled
                else -> R.drawable.mobile_charge_filled
            }
        } else {
            when {
                battery.level >= 100 -> R.drawable.battery_android_full
                battery.level >= 85 -> R.drawable.battery_android_6
                battery.level >= 70 -> R.drawable.battery_android_5
                battery.level >= 55 -> R.drawable.battery_android_4
                battery.level >= 40 -> R.drawable.battery_android_3
                battery.level >= 25 -> R.drawable.battery_android_2
                battery.level >= 10 -> R.drawable.battery_android_1
                else -> R.drawable.battery_android_0
            }
        }
    }

    val displayStatus = remember(battery.status, battery.wattage, battery.statusRes) {
        if (isCharging) {
            when {
                battery.wattage >= 25.0 -> context.getString(R.string.battery_hyper_charging)
                battery.wattage >= 15.0 -> context.getString(R.string.battery_fast_charging)
                else -> context.getString(R.string.battery_charging)
            }
        } else {
            context.getString(battery.statusRes)
        }
    }

    OverviewCard(
        backgroundIcon = {
            val chargingIcons = remember {
                listOf(R.drawable.mobile_charge_filled, R.drawable.bolt_filled, R.drawable.bolt_boost_filled)
            }

            AnimatedContent(
                targetState = batteryIcon,
                transitionSpec = {
                    val isChargingTransition = targetState in chargingIcons || initialState in chargingIcons
                    if (isChargingTransition) {
                        (
                            slideInVertically(
                                initialOffsetY = { it },
                                animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing),
                            ) + scaleIn(
                                animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing),
                            )
                            ).togetherWith(
                            slideOutVertically(
                                targetOffsetY = { it },
                                animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing),
                            ) + scaleOut(
                                animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing),
                            ),
                        )
                    } else {
                        fadeIn(
                            animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing),
                        ).togetherWith(
                            fadeOut(
                                animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing),
                            ),
                        )
                    }
                },
                label = "Battery Icon Animation",
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(y = 30.dp),
            ) { icBattery ->
                Icon(
                    painter = painterResource(id = icBattery),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(160.dp)
                        .alpha(0.20f),
                )
            }
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "${battery.level}%",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Bold,
                )
                AnimatedContent(
                    targetState = displayStatus,
                    transitionSpec = {
                        (
                            slideInHorizontally(
                                initialOffsetX = { -it },
                                animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing),
                            ) + scaleIn(
                                animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing),
                            )
                            ).togetherWith(
                            slideOutHorizontally(
                                targetOffsetX = { -it },
                                animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing),
                            ) + scaleOut(
                                animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing),
                            ),
                        )
                    },
                    label = "BatteryStatusAnimation",
                ) { status ->
                    Text(
                        text = status,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                BadgeChip(
                    text = stringResource(battery.healthRes),
                    containerColor = MaterialTheme.colorScheme.primary,
                    textColor = MaterialTheme.colorScheme.onPrimary,
                )
                BadgeChip(
                    text = battery.technology,
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    textColor = MaterialTheme.colorScheme.onTertiary,
                )
                BadgeChip(
                    text = if (battery.capacity > 0) "${battery.capacity.toInt()} mAh" else stringResource(R.string.value_unknown),
                    containerColor = MaterialTheme.colorScheme.secondary,
                    textColor = MaterialTheme.colorScheme.onSecondary,
                )
            }
        }
    }
}

@Composable
private fun BatteryDetailsCard(battery: Battery, onHelpClick: () -> Unit) {
    StandardCard(shape = RoundedCornerShape(16.dp), contentPadding = 16.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.battery_details_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                IconButton(
                    onClick = rememberHapticOnClick(onHelpClick),
                    modifier = Modifier.size(24.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.help_filled),
                        contentDescription = stringResource(R.string.cd_help),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            TwoColumnInfoRow {
                InfoItem(
                    label = stringResource(R.string.battery_label_voltage),
                    value = "${battery.voltage} mV",
                    modifier = Modifier.weight(1f),
                )
                InfoItem(
                    label = stringResource(R.string.battery_label_temperature),
                    value = "${battery.temperature} °C",
                    modifier = Modifier.weight(1f),
                )
            }

            TwoColumnInfoRow {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.battery_label_power_source),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    AnimatedContent(
                        targetState = battery.powerSourceRes,
                        transitionSpec = {
                            (
                                slideInHorizontally(
                                    initialOffsetX = { -it },
                                    animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing),
                                ) + scaleIn(
                                    animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing),
                                )
                                ).togetherWith(
                                slideOutHorizontally(
                                    targetOffsetX = { -it },
                                    animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing),
                                ) + scaleOut(
                                    animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing),
                                ),
                            )
                        },
                        label = "PowerSourceAnimation",
                    ) { powerSourceRes ->
                        Text(
                            text = stringResource(powerSourceRes),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                InfoItem(
                    label = stringResource(R.string.battery_label_cycle_count),
                    value = if (battery.cycleCount >= 0) "${battery.cycleCount}" else stringResource(R.string.value_unknown),
                    modifier = Modifier.weight(1f),
                )
            }

            TwoColumnInfoRow {
                InfoItem(
                    label = stringResource(R.string.battery_label_uptime),
                    value = formatUptime(battery.uptime),
                    modifier = Modifier.weight(1f),
                )
                InfoItem(
                    label = stringResource(R.string.battery_label_deep_sleep),
                    value = formatUptime(battery.deepSleep),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

private fun formatUptime(millis: Long): String {
    val totalSeconds = millis / 1000
    val days = totalSeconds / 86400
    val hours = (totalSeconds % 86400) / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return buildString {
        if (days > 0) append("${days}d ")
        if (hours > 0 || days > 0) append("${hours}h ")
        if (minutes > 0 || hours > 0 || days > 0) append("${minutes}m ")
        append("${seconds}s")
    }.trim()
}

@Composable
private fun BatteryHelpContent() {
    val helpItems = persistentListOf(
        stringResource(R.string.battery_help_voltage_temp_title) to stringResource(R.string.battery_help_voltage_temp_desc),
        stringResource(R.string.battery_help_power_source_title) to stringResource(R.string.battery_help_power_source_desc),
        stringResource(R.string.battery_help_charging_speed_title) to stringResource(R.string.battery_help_charging_speed_desc),
        stringResource(R.string.battery_help_wattage_title) to stringResource(R.string.battery_help_wattage_desc),
        stringResource(R.string.battery_help_current_title) to stringResource(R.string.battery_help_current_desc),
        stringResource(R.string.battery_help_design_capacity_title) to stringResource(R.string.battery_help_design_capacity_desc),
        stringResource(R.string.battery_help_cycle_count_title) to stringResource(R.string.battery_help_cycle_count_desc),
        stringResource(R.string.battery_help_uptime_title) to stringResource(R.string.battery_help_uptime_desc),
    )

    HelpBottomSheetContent(helpItems = helpItems)
}
