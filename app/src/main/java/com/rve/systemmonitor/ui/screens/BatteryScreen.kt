package com.rve.systemmonitor.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rve.systemmonitor.R
import com.rve.systemmonitor.domain.model.Battery
import com.rve.systemmonitor.ui.viewmodel.BatteryViewModel
import kotlin.math.abs
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun BatteryScreen(isActive: Boolean, viewModel: BatteryViewModel = hiltViewModel()) {
    val initialBatteryInfo = remember { viewModel.batteryInfo.value }
    val batteryInfo by if (isActive) {
        viewModel.batteryInfo.collectAsStateWithLifecycle()
    } else {
        remember { emptyFlow<Battery>() }.collectAsStateWithLifecycle(initialBatteryInfo)
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
            BatteryDetailsCard(batteryInfo)
        }
    }
}

@Composable
private fun BatteryOverviewCard(battery: Battery) {
    val batteryIcon = remember(battery.level) {
        when {
            battery.level >= 100 -> R.drawable.battery_android_full_24px
            battery.level >= 85 -> R.drawable.battery_android_6_24px
            battery.level >= 70 -> R.drawable.battery_android_5_24px
            battery.level >= 55 -> R.drawable.battery_android_4_24px
            battery.level >= 40 -> R.drawable.battery_android_3_24px
            battery.level >= 25 -> R.drawable.battery_android_2_24px
            battery.level >= 10 -> R.drawable.battery_android_1_24px
            else -> R.drawable.battery_android_0_24px
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
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
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = battery.status,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
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
                        containerColor = MaterialTheme.colorScheme.secondary,
                        textColor = MaterialTheme.colorScheme.onSecondary,
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
private fun BatteryDetailsCard(battery: Battery) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Battery Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

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
                    value = String.format("%.2f W", battery.wattage),
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
private fun InfoItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = LocalContentColor.current.copy(alpha = 0.7f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun BadgeChip(text: String, containerColor: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(containerColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
        )
    }
}
