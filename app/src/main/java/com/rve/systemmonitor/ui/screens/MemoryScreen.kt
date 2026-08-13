package com.rve.systemmonitor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.rve.systemmonitor.R
import com.rve.systemmonitor.domain.model.RAM
import com.rve.systemmonitor.domain.model.Storage
import com.rve.systemmonitor.domain.model.ZRAM
import com.rve.systemmonitor.ui.components.card.OverviewCard
import com.rve.systemmonitor.ui.components.dialog.InfoDialog
import com.rve.systemmonitor.ui.components.layout.ScreenLazyColumn
import com.rve.systemmonitor.ui.components.row.MemoryStorageProgressRow
import com.rve.systemmonitor.ui.components.row.TwoColumnInfoRow
import com.rve.systemmonitor.ui.utils.rememberLifecycleAwareState
import com.rve.systemmonitor.ui.viewmodel.MemoryUiState
import com.rve.systemmonitor.ui.viewmodel.MemoryViewModel
import java.util.Locale

@Composable
fun MemoryScreen(isActive: Boolean, viewModel: MemoryViewModel = hiltViewModel()) {
    val uiState by rememberLifecycleAwareState(isActive, viewModel.uiState)

    LaunchedEffect(isActive) {
        if (isActive) {
            viewModel.refreshStorage()
        }
    }

    MemoryScreenContent(
        uiState = uiState,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MemoryScreenContent(uiState: MemoryUiState) {
    var selectedDetail by remember { mutableStateOf<Pair<String, String>?>(null) }

    if (selectedDetail != null) {
        InfoDialog(
            title = selectedDetail!!.first,
            description = selectedDetail!!.second,
            onDismiss = { selectedDetail = null },
        )
    }

    ScreenLazyColumn {
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
    val cached = remember(ram.cached) { formatMemoryValue(ram.cached) }
    val buffers = remember(ram.buffers) { formatMemoryValue(ram.buffers) }
    val active = remember(ram.active) { formatMemoryValue(ram.active) }
    val inactive = remember(ram.inactive) { formatMemoryValue(ram.inactive) }
    val slab = remember(ram.slab) { formatMemoryValue(ram.slab) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.memory_detailed_breakdown),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        TwoColumnInfoRow(spacing = 12.dp) {
            MemoryDetailItem(
                label = stringResource(R.string.memory_label_cached),
                value = cached,
                description = stringResource(R.string.memory_cached_description),
                onItemClick = onItemClick,
                modifier = Modifier.weight(1f),
            )
            MemoryDetailItem(
                label = stringResource(R.string.memory_label_buffers),
                value = buffers,
                description = stringResource(R.string.memory_buffers_description),
                onItemClick = onItemClick,
                modifier = Modifier.weight(1f),
            )
        }

        TwoColumnInfoRow(spacing = 12.dp) {
            MemoryDetailItem(
                label = stringResource(R.string.memory_label_active),
                value = active,
                description = stringResource(R.string.memory_active_description),
                onItemClick = onItemClick,
                modifier = Modifier.weight(1f),
            )
            MemoryDetailItem(
                label = stringResource(R.string.memory_label_inactive),
                value = inactive,
                description = stringResource(R.string.memory_inactive_description),
                onItemClick = onItemClick,
                modifier = Modifier.weight(1f),
            )
        }

        MemoryDetailItem(
            label = stringResource(R.string.memory_label_slab),
            value = slab,
            description = stringResource(R.string.memory_slab_description),
            onItemClick = onItemClick,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun StorageCard(storage: Storage) {
    OverviewCard(
        iconResId = R.drawable.database_filled,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            MemoryStorageProgressRow(
                label = stringResource(R.string.memory_internal_storage),
                usedValue = storage.used.toString(),
                totalValue = storage.total.toString(),
                usedPercentage = if (storage.usedPercentage.isNaN()) 0f else storage.usedPercentage.toFloat(),
                freeValue = storage.available.toString(),
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.4f),
                thickness = 1.dp,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                StorageInfoItem(
                    label = stringResource(R.string.memory_label_mount_path),
                    value = storage.mountPath,
                    modifier = Modifier.weight(1.5f),
                )
                StorageInfoItem(
                    label = stringResource(R.string.memory_label_filesystem),
                    value = storage.fileSystemType,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun StorageInfoItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f))
            .padding(8.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
    Card(
        onClick = { onItemClick(label, description) },
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun MemoryCard(ram: RAM, zram: ZRAM) {
    OverviewCard(
        iconResId = R.drawable.memory_alt_filled,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            MemoryStorageProgressRow(
                label = stringResource(R.string.memory_label_ram),
                usedValue = ram.used.toString(),
                totalValue = ram.total.toString(),
                usedPercentage = if (ram.usedPercentage.isNaN()) 0f else ram.usedPercentage.toFloat(),
                freeValue = ram.available.toString(),
            )

            if (zram.isActive) {
                MemoryStorageProgressRow(
                    label = stringResource(R.string.memory_label_zram),
                    usedValue = zram.used.toString(),
                    totalValue = zram.total.toString(),
                    usedPercentage = if (zram.usedPercentage.isNaN()) 0f else zram.usedPercentage.toFloat(),
                    freeValue = zram.available.toString(),
                    progressColor = MaterialTheme.colorScheme.tertiary,
                )
            }
        }
    }
}
