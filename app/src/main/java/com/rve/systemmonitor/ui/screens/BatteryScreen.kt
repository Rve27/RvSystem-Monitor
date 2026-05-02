package com.rve.systemmonitor.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rve.systemmonitor.R
import com.rve.systemmonitor.domain.model.Battery
import com.rve.systemmonitor.ui.components.chip.BadgeChip
import com.rve.systemmonitor.ui.components.item.HelpItem
import com.rve.systemmonitor.ui.components.item.InfoItem
import com.rve.systemmonitor.ui.components.haptic.rememberHapticOnClick
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
            BatteryDetailsCard(
                battery = batteryInfo,
                onHelpClick = { showHelpSheet = true },
            )
        }
    }
}

@Composable
private fun BatteryOverviewCard(battery: Battery) {
    val batteryIcon = remember(battery.level) {
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
            Crossfade(
                targetState = batteryIcon,
                label = "Battery Icon Fade",
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(y = 30.dp),
            ) { iconRes ->
                Icon(
                    painter = painterResource(id = iconRes),
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
                        targetState = battery.status,
                        transitionSpec = {
                            (
                                slideInHorizontally(
                                    animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing),
                                ) + scaleIn(
                                    animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing),
                                )
                                ).togetherWith(
                                slideOutHorizontally(
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
                InfoItem(
                    label = "Power Source",
                    value = battery.powerSource,
                    modifier = Modifier.weight(1f),
                )
                val currentMA = abs(battery.current)
                val isCharging = battery.status == "Charging"
                val isDischarging = battery.status == "Discharging"

                val speedLabel = when {
                    isCharging -> "Charging Speed"
                    isDischarging -> "Discharging Speed"
                    else -> "Current Speed"
                }

                InfoItem(
                    label = speedLabel,
                    value = if (battery.current != 0) "$currentMA mA" else "0 mA",
                    modifier = Modifier.weight(1f),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                InfoItem(
                    label = "Wattage",
                    value = String.format(LocalLocale.current.platformLocale, "%.2f W", battery.wattage),
                    modifier = Modifier.weight(1f),
                )
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
