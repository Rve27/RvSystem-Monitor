package com.rve.systemmonitor.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rve.systemmonitor.R
import com.rve.systemmonitor.domain.model.RAM
import com.rve.systemmonitor.domain.model.Storage
import com.rve.systemmonitor.domain.model.ZRAM
import com.rve.systemmonitor.ui.components.chip.BadgeChip
import com.rve.systemmonitor.ui.components.dialog.InfoDialog
import com.rve.systemmonitor.ui.viewmodel.MemoryUiState
import com.rve.systemmonitor.ui.viewmodel.MemoryViewModel
import java.util.Locale
import kotlinx.coroutines.flow.emptyFlow

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MemoryScreen(isActive: Boolean, viewModel: MemoryViewModel = hiltViewModel()) {
    val initialUiState = remember { viewModel.uiState.value }
    val uiState by if (isActive) {
        viewModel.uiState.collectAsStateWithLifecycle()
    } else {
        remember { emptyFlow<MemoryUiState>() }.collectAsStateWithLifecycle(initialUiState)
    }

    LaunchedEffect(isActive) {
        if (isActive) {
            viewModel.refreshStorage()
        }
    }

    var selectedDetail by remember { mutableStateOf<Pair<String, String>?>(null) }

    if (selectedDetail != null) {
        InfoDialog(
            title = selectedDetail!!.first,
            description = selectedDetail!!.second,
            onDismiss = { selectedDetail = null },
        )
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
            MemoryCard(
                ram = uiState.ram,
                zram = uiState.zram,
            )
        }

        item {
            StorageCard(
                storage = uiState.storage,
            )
        }

        item {
            DetailedMemoryCard(
                ram = uiState.ram,
                onItemClick = { label, description ->
                    selectedDetail = label to description
                },
            )
        }
    }
}

@Composable
private fun DetailedMemoryCard(ram: RAM, onItemClick: (String, String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Detailed Breakdown",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MemoryDetailItem(
                label = "Cached",
                value = formatMemoryValue(ram.cached),
                description = "Memory used for the file system cache to speed up file access. " +
                    "This memory can be reclaimed by the system if needed.",
                onItemClick = onItemClick,
                modifier = Modifier.weight(1f),
            )
            MemoryDetailItem(
                label = "Buffers",
                value = formatMemoryValue(ram.buffers),
                description = "Memory used for raw disk blocks and metadata. Usually very small on Android devices.",
                onItemClick = onItemClick,
                modifier = Modifier.weight(1f),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MemoryDetailItem(
                label = "Active",
                value = formatMemoryValue(ram.active),
                description = "Memory that is currently being used or has been used very recently. " +
                    "This memory is unlikely to be reclaimed soon.",
                onItemClick = onItemClick,
                modifier = Modifier.weight(1f),
            )
            MemoryDetailItem(
                label = "Inactive",
                value = formatMemoryValue(ram.inactive),
                description = "Memory that has not been used for a while. " +
                    "It is a prime candidate for being moved to Swap/ZRAM or reclaimed.",
                onItemClick = onItemClick,
                modifier = Modifier.weight(1f),
            )
        }

        MemoryDetailItem(
            label = "Slab",
            value = formatMemoryValue(ram.slab),
            description = "Memory used by the kernel's internal data structures and objects.",
            onItemClick = onItemClick,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun StorageCard(storage: Storage) {
    val density = LocalDensity.current
    val customStroke = remember(density) {
        with(density) {
            Stroke(
                width = 12.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }
    }

    val storageTargetProgress = remember(storage.usedPercentage) {
        if (storage.usedPercentage.isNaN()) 0f else (storage.usedPercentage.toFloat() / 100f)
    }
    val storageAnimatedProgress by animateFloatAsState(
        targetValue = storageTargetProgress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "Storage Progress Animation",
    )

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
            Icon(
                painter = painterResource(R.drawable.database_filled),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(160.dp)
                    .offset(y = 30.dp)
                    .alpha(0.20f),
            )

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    modifier = Modifier.height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    CircularWavyProgressIndicator(
                        progress = { storageAnimatedProgress },
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f),
                        stroke = customStroke,
                        trackStroke = customStroke,
                        wavelength = 25.dp,
                        modifier = Modifier
                            .fillMaxHeight()
                            .aspectRatio(1f),
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column {
                            Text(
                                text = "Internal Storage",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = "${storage.used} / ${storage.total} GB",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            BadgeChip(
                                text = "${storage.usedPercentage}%",
                                containerColor = MaterialTheme.colorScheme.primary,
                                textColor = MaterialTheme.colorScheme.onPrimary,
                            )
                            BadgeChip(
                                text = "${storage.available} GB Free",
                                containerColor = MaterialTheme.colorScheme.tertiary,
                                textColor = MaterialTheme.colorScheme.onTertiary,
                            )
                        }
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f),
                    thickness = 1.dp,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    StorageInfoItem(
                        label = "Mount Path",
                        value = storage.mountPath,
                        modifier = Modifier.weight(1.5f),
                    )
                    StorageInfoItem(
                        label = "Filesystem",
                        value = storage.fileSystemType,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun StorageInfoItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(8.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun formatMemoryValue(valueInGb: Double): String {
    return if (valueInGb < 1.0) {
        val valueInMb = valueInGb * 1024.0
        "${String.format(Locale.getDefault(), "%.2f", valueInMb)} MB"
    } else {
        "${String.format(Locale.getDefault(), "%.2f", valueInGb)} GB"
    }
}

@Composable
private fun MemoryDetailItem(
    label: String,
    value: String,
    description: String,
    onItemClick: (String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.tertiaryContainer)
            .clickable { onItemClick(label, description) }
            .padding(12.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun MemoryCard(ram: RAM, zram: ZRAM) {
    val density = LocalDensity.current
    val customStroke = remember(density) {
        with(density) {
            Stroke(
                width = 12.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }
    }

    val ramTargetProgress = remember(ram.usedPercentage) {
        if (ram.usedPercentage.isNaN()) 0f else (ram.usedPercentage.toFloat() / 100f)
    }
    val ramAnimatedProgress by animateFloatAsState(
        targetValue = ramTargetProgress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "RAM Progress Animation",
    )

    val zramTargetProgress = remember(zram.usedPercentage) {
        if (zram.usedPercentage.isNaN()) 0f else (zram.usedPercentage.toFloat() / 100f)
    }
    val zramAnimatedProgress by animateFloatAsState(
        targetValue = zramTargetProgress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "ZRAM Progress Animation",
    )

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
            Icon(
                painter = painterResource(R.drawable.memory_alt_filled),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(160.dp)
                    .offset(y = 30.dp)
                    .alpha(0.20f),
            )

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Row(
                    modifier = Modifier.height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    CircularWavyProgressIndicator(
                        progress = { ramAnimatedProgress },
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
                        stroke = customStroke,
                        trackStroke = customStroke,
                        wavelength = 25.dp,
                        modifier = Modifier
                            .fillMaxHeight()
                            .aspectRatio(1f),
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column {
                            Text(
                                text = "RAM",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = "${ram.used} / ${ram.total} GB",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            BadgeChip(
                                text = "${ram.usedPercentage}%",
                                containerColor = MaterialTheme.colorScheme.secondary,
                                textColor = MaterialTheme.colorScheme.onSecondary,
                            )
                            BadgeChip(
                                text = "${ram.available} GB Free",
                                containerColor = MaterialTheme.colorScheme.tertiary,
                                textColor = MaterialTheme.colorScheme.onTertiary,
                            )
                        }
                    }
                }

                if (zram.isActive) {
                    Row(
                        modifier = Modifier.height(IntrinsicSize.Min),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        CircularWavyProgressIndicator(
                            progress = { zramAnimatedProgress },
                            color = MaterialTheme.colorScheme.secondary,
                            trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
                            stroke = customStroke,
                            trackStroke = customStroke,
                            wavelength = 25.dp,
                            modifier = Modifier
                                .fillMaxHeight()
                                .aspectRatio(1f),
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Column {
                                Text(
                                    text = "ZRAM",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = "${zram.used} / ${zram.total} GB",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                BadgeChip(
                                    text = "${zram.usedPercentage}%",
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    textColor = MaterialTheme.colorScheme.onSecondary,
                                )
                                BadgeChip(
                                    text = "${zram.available} GB Free",
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
