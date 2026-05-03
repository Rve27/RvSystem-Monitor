package com.rve.systemmonitor.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rve.systemmonitor.R
import com.rve.systemmonitor.domain.model.Battery
import com.rve.systemmonitor.domain.model.BatteryDataPoint
import com.rve.systemmonitor.ui.components.chip.BadgeChip
import com.rve.systemmonitor.ui.components.haptic.rememberHapticOnClick
import com.rve.systemmonitor.ui.components.item.HelpItem
import com.rve.systemmonitor.ui.components.item.InfoItem
import com.rve.systemmonitor.ui.navigation.TRANSITION_DURATION
import com.rve.systemmonitor.ui.viewmodel.BatteryViewModel
import kotlin.math.abs
import kotlinx.coroutines.flow.emptyFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatteryScreen(isActive: Boolean, viewModel: BatteryViewModel = hiltViewModel()) {
    val initialBatteryInfo = remember { viewModel.batteryInfo.value }
    val batteryInfo by if (isActive) {
        viewModel.batteryInfo.collectAsStateWithLifecycle()
    } else {
        remember { emptyFlow<Battery>() }.collectAsStateWithLifecycle(initialBatteryInfo)
    }

    val batteryHistory by viewModel.batteryHistory.collectAsStateWithLifecycle()
    val hasAlreadyAnimated = remember { viewModel.hasAnimated }

    var showHelpSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    if (showHelpSheet) {
        ModalBottomSheet(
            onDismissRequest = { showHelpSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            BatteryHelpContent()
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(
            top = 16.dp,
            bottom = 112.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        item {
            BatteryOverviewCard(batteryInfo)
        }

        item {
            ChargingSpeedCard(
                battery = batteryInfo,
                history = batteryHistory,
                hasAnimated = hasAlreadyAnimated,
                onAnimated = { viewModel.markAsAnimated() },
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
private fun ChargingSpeedCard(battery: Battery, history: List<BatteryDataPoint>, hasAnimated: Boolean, onAnimated: () -> Unit) {
    val currentMA = abs(battery.current)
    val isCharging = battery.status == "Charging"
    val isDischarging = battery.status == "Discharging"

    val speedLabel = when {
        isCharging -> "Charging Speed"
        isDischarging -> "Discharging Speed"
        else -> "Current Speed"
    }

    val accentColor = MaterialTheme.colorScheme.primary

    val infiniteTransition = rememberInfiniteTransition(label = "PulseTransition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (battery.wattage >= 25.0) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "PulseScale",
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp).animateContentSize(),
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
                    Text(
                        text = if (battery.current != 0) "$currentMA mA" else "0 mA",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                    )
                }

                BadgeChip(
                    text = String.format(LocalLocale.current.platformLocale, "%.2f W", battery.wattage),
                    containerColor = MaterialTheme.colorScheme.primary,
                    textColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.scale(pulseScale),
                )
            }

            val actualMax = if (history.isNotEmpty()) history.maxOf { abs(it.mA) }.toFloat() else 0f
            val renderMax = actualMax.coerceAtLeast(1000f)
            val minValInHistory = if (history.isNotEmpty()) history.minOf { abs(it.mA) }.toFloat() else 0f

            val enterTransition = if (hasAnimated) EnterTransition.None else fadeIn(animationSpec = tween(1000))

            AnimatedVisibility(
                visible = history.size >= 2,
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

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        if (history.size > 1) {
                            val minVal = 0f
                            val range = renderMax - minVal
                            val stepX = size.width / (history.size - 1)

                            fun getY(value: Int): Float {
                                return size.height - ((abs(value).toFloat() - minVal) / range * size.height)
                            }

                            val path = Path()
                            path.moveTo(0f, getY(history[0].mA))

                            for (i in 0 until history.size - 1) {
                                val x1 = i * stepX
                                val y1 = getY(history[i].mA)
                                val x2 = (i + 1) * stepX
                                val y2 = getY(history[i + 1].mA)

                                val controlPoint1X = x1 + (x2 - x1) / 2
                                val controlPoint1Y = y1
                                val controlPoint2X = x1 + (x2 - x1) / 2
                                val controlPoint2Y = y2

                                path.cubicTo(
                                    controlPoint1X,
                                    controlPoint1Y,
                                    controlPoint2X,
                                    controlPoint2Y,
                                    x2,
                                    y2,
                                )
                            }

                            drawPath(
                                path = path,
                                color = accentColor,
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
                            )

                            val fillPath = Path().apply {
                                addPath(path)
                                lineTo(size.width, size.height)
                                lineTo(0f, size.height)
                                close()
                            }
                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(accentColor.copy(alpha = 0.4f), Color.Transparent),
                                    endY = size.height,
                                ),
                            )
                        }
                    }

                    if (history.size >= 2) {
                        Text(
                            text = "MAX: ${actualMax.toInt()} mA",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(top = 4.dp, start = 4.dp),
                        )
                        Text(
                            text = "MIN: ${minValInHistory.toInt()} mA",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(bottom = 4.dp, start = 4.dp),
                        )

                        history.forEachIndexed { index, point ->
                            if (index > 0 && point.status != history[index - 1].status) {
                                val xRatio = index.toFloat() / (history.size - 1)
                                val yRatio = (abs(point.mA).toFloat() / renderMax).coerceIn(0f, 1f)

                                val statusLabel = when (point.status) {
                                    "Charging",
                                    -> "CHARGING"

                                    "Discharging",
                                    -> "DISCHARGING"

                                    else -> point.status.uppercase()
                                }
                                val statusColor = if (point.status ==
                                    "Charging"
                                ) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error

                                with(LocalDensity.current) {
                                    val xOffset = (width * xRatio).toDp()
                                    val yOffset = (height * (1f - yRatio)).toDp()

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
}

@Composable
private fun BatteryOverviewCard(battery: Battery) {
    val isCharging = battery.status == "Charging"

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

    val displayStatus = remember(battery.status, battery.wattage) {
        if (isCharging) {
            when {
                battery.wattage >= 25.0 -> "Hyper Charging"
                battery.wattage >= 15.0 -> "Fast Charging"
                else -> "Charging"
            }
        } else {
            battery.status
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
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

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
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
                                slideInVertically(
                                    initialOffsetY = { -it },
                                    animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing),
                                ) + scaleIn(
                                    animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing),
                                )
                                ).togetherWith(
                                slideOutVertically(
                                    targetOffsetY = { -it },
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
                    val healthText = if (battery.healthPercentage > 0) {
                        "${battery.health} (${battery.healthPercentage}%)"
                    } else {
                        battery.health
                    }
                    BadgeChip(
                        text = healthText,
                        containerColor = MaterialTheme.colorScheme.primary,
                        textColor = MaterialTheme.colorScheme.onPrimary,
                    )
                    BadgeChip(
                        text = battery.technology,
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        textColor = MaterialTheme.colorScheme.onTertiary,
                    )
                }
            }
        }
    }
}

@Composable
private fun BatteryDetailsCard(battery: Battery, onHelpClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Battery Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                IconButton(
                    onClick = rememberHapticOnClick(onHelpClick),
                    modifier = Modifier.size(24.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.help_filled),
                        contentDescription = "Help",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                InfoItem(
                    label = "Voltage",
                    value = "${battery.voltage} mV",
                    modifier = Modifier.weight(1f),
                )
                InfoItem(
                    label = "Temperature",
                    value = "${battery.temperature} °C",
                    modifier = Modifier.weight(1f),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Power Source",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    AnimatedContent(
                        targetState = battery.powerSource,
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
                    ) { powerSource ->
                        Text(
                            text = powerSource,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                InfoItem(
                    label = "Cycle Count",
                    value = if (battery.cycleCount >= 0) "${battery.cycleCount}" else "Unknown",
                    modifier = Modifier.weight(1f),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                InfoItem(
                    label = "Remaining Capacity",
                    value = if (battery.remainingCapacity > 0) "${battery.remainingCapacity.toInt()} mAh" else "Unknown",
                    modifier = Modifier.weight(1f),
                )
                InfoItem(
                    label = "Maximum Capacity",
                    value = if (battery.maxCapacity > 0) "${battery.maxCapacity.toInt()} mAh" else "Unknown",
                    modifier = Modifier.weight(1f),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                InfoItem(
                    label = "Design Capacity",
                    value = if (battery.capacity > 0) "${battery.capacity.toInt()} mAh" else "Unknown",
                    modifier = Modifier.weight(1f),
                )
                InfoItem(
                    label = "Uptime",
                    value = formatUptime(battery.uptime),
                    modifier = Modifier.weight(1f),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                InfoItem(
                    label = "Deep Sleep",
                    value = formatUptime(battery.deepSleep),
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.weight(1f))
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
    val helpItems = listOf(
        "Voltage & Temperature" to "Sourced from real-time system broadcasts via the Android BatteryManager API.",
        "Power Source & Status" to "Detected from the current charging state (AC, USB, or Wireless) via system intents.",
        "Charging Speed" to "Estimated based on real-time wattage: Fast Charging (15W+) and Hyper Charging (25W+).",
        "Wattage (Power)" to "Calculated in real-time by multiplying Voltage (V) and Current (A).",
        "Current (mA)" to "Direct hardware reading from the battery's charge counter property.",
        "Capacity (Design/Max/Remaining)" to "Extracted from Android PowerProfile and battery charge counter calculations. " +
            "Maximum Capacity and Health Percentage are estimates and may not be 100% accurate.",
        "Cycle Count" to "Native Android 14+ property indicating total charge cycles completed.",
        "Uptime & Deep Sleep" to "Uptime is the total time since boot. Deep Sleep is the time the CPU was in a low-power state.",
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Data Sources",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(helpItems) { (title, description) ->
                HelpItem(
                    title = title,
                    description = description,
                )
            }
        }
    }
}
