package com.rve.systemmonitor.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.rve.systemmonitor.R
import com.rve.systemmonitor.domain.model.CPU
import com.rve.systemmonitor.domain.model.CoreDetail
import com.rve.systemmonitor.ui.components.card.OverviewCard
import com.rve.systemmonitor.ui.components.card.StandardCard
import com.rve.systemmonitor.ui.components.chip.BadgeChip
import com.rve.systemmonitor.ui.components.chip.CompactInfoChip
import com.rve.systemmonitor.ui.components.item.InfoItem
import com.rve.systemmonitor.ui.components.layout.ScreenLazyColumn
import com.rve.systemmonitor.ui.components.row.TwoColumnInfoRow
import com.rve.systemmonitor.ui.utils.rememberLifecycleAwareState
import com.rve.systemmonitor.ui.viewmodel.CPUViewModel
import java.util.Locale

@Composable
fun CPUScreen(isActive: Boolean, viewModel: CPUViewModel = hiltViewModel()) {
    val cpuInfo by rememberLifecycleAwareState(isActive, viewModel.cpuInfo)
    CPUScreenContent(cpuInfo = cpuInfo)
}

@Composable
private fun CPUScreenContent(cpuInfo: CPU) {
    ScreenLazyColumn {
        item {
            CPUOverviewCard(cpuInfo)
        }

        item {
            Text(
                text = stringResource(R.string.cpu_cores_section_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
        }

        items(
            items = cpuInfo.coreDetails,
            key = { core -> core.id },
        ) { core ->
            CoreDetailCard(core, cpuInfo.isLoadAvailable)
        }
    }
}

@Composable
private fun CPUOverviewCard(cpu: CPU) {
    val peakFreqKhz = cpu.coreDetails.maxOfOrNull { it.maxFreqKhz } ?: 0L
    val peakFrequency = String.format(Locale.US, "%.2f GHz", peakFreqKhz / 1_000_000.0)

    OverviewCard(
        iconResId = R.drawable.memory,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = cpu.model,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(R.string.label_by, cpu.manufacturer),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                BadgeChip(
                    text = stringResource(R.string.cpu_cores_count, cpu.cores),
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    textColor = MaterialTheme.colorScheme.onTertiary,
                )
                BadgeChip(
                    text = stringResource(R.string.cpu_peak_frequency, peakFrequency),
                    containerColor = MaterialTheme.colorScheme.primary,
                    textColor = MaterialTheme.colorScheme.onPrimary,
                )
                AnimatedVisibility(
                    visible = cpu.isLoadAvailable && cpu.load >= 0.0,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut(),
                ) {
                    BadgeChip(
                        text = String.format(Locale.US, "%.1f%%", cpu.load),
                        containerColor = MaterialTheme.colorScheme.secondary,
                        textColor = MaterialTheme.colorScheme.onSecondary,
                    )
                }
                AnimatedVisibility(
                    visible = cpu.temperature > 0.0,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut(),
                ) {
                    BadgeChip(
                        text = String.format(Locale.US, "%.1f °C", cpu.temperature),
                        containerColor = MaterialTheme.colorScheme.secondary,
                        textColor = MaterialTheme.colorScheme.onSecondary,
                    )
                }
            }

            TwoColumnInfoRow {
                InfoItem(
                    label = stringResource(R.string.cpu_label_architecture),
                    value = cpu.architecture,
                    valueColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.weight(1f),
                )
                InfoItem(
                    label = stringResource(R.string.cpu_label_board),
                    value = cpu.board,
                    valueColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.weight(1f),
                )
            }

            TwoColumnInfoRow {
                InfoItem(
                    label = stringResource(R.string.cpu_label_hardware),
                    value = cpu.hardware,
                    valueColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.weight(1f),
                )
                InfoItem(
                    label = stringResource(R.string.cpu_label_abi),
                    value = cpu.abi,
                    valueColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CoreDetailCard(core: CoreDetail, isLoadAvailable: Boolean) {
    val progress = remember(core.currentFreqKhz, core.minFreqKhz, core.maxFreqKhz) {
        if (core.maxFreqKhz > core.minFreqKhz) {
            ((core.currentFreqKhz - core.minFreqKhz).toFloat() / (core.maxFreqKhz - core.minFreqKhz)).coerceIn(0f, 1f)
        } else {
            0f
        }
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "FreqProgress",
    )

    StandardCard(
        modifier = Modifier.padding(vertical = 4.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {

            Text(
                text = stringResource(R.string.cpu_core_label, core.id),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp,
                modifier = Modifier.weight(1f),
            )

            AnimatedVisibility(
                visible = isLoadAvailable && core.load >= 0.0,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
            ) {
                BadgeChip(
                    text = String.format(Locale.US, "%.1f%%", core.load),
                    containerColor = MaterialTheme.colorScheme.secondary,
                    textColor = MaterialTheme.colorScheme.onSecondary,
                )
            }

            AnimatedVisibility(
                visible = core.temperature > 0.0,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
            ) {
                BadgeChip(
                    text = String.format(Locale.US, "%.1f °C", core.temperature),
                    containerColor = MaterialTheme.colorScheme.primary,
                    textColor = MaterialTheme.colorScheme.onPrimary,
                )
            }

            AnimatedVisibility(
                visible = core.governor.isNotBlank() && !core.governor.equals("N/A", ignoreCase = true),
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
            ) {
                BadgeChip(
                    text = core.governor.uppercase(),
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    textColor = MaterialTheme.colorScheme.onTertiary,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = core.currentFreq,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    ),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearWavyProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TwoColumnInfoRow(spacing = 12.dp) {
            CompactInfoChip(
                label = stringResource(R.string.cpu_label_minimum),
                value = core.minFreq,
                modifier = Modifier.weight(1f),
            )
            CompactInfoChip(
                label = stringResource(R.string.cpu_label_maximum),
                value = core.maxFreq,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
