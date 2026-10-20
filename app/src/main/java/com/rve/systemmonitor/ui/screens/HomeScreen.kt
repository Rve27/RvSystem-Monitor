package com.rve.systemmonitor.ui.screens

import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.rve.systemmonitor.R
import com.rve.systemmonitor.ui.components.card.InfoCardData
import com.rve.systemmonitor.ui.components.card.InfoOverviewCard
import com.rve.systemmonitor.ui.components.dialog.HelpBottomSheetContent
import com.rve.systemmonitor.ui.components.layout.ScreenLazyColumn
import com.rve.systemmonitor.ui.utils.rememberLifecycleAwareState
import com.rve.systemmonitor.ui.viewmodel.HomeUiState
import com.rve.systemmonitor.ui.viewmodel.HomeViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

@Composable
fun HomeScreen(isActive: Boolean, onNavigateToCPU: () -> Unit, onNavigateToGPU: () -> Unit, viewModel: HomeViewModel = hiltViewModel()) {
    val uiState by rememberLifecycleAwareState(isActive, viewModel.uiState)
    HomeScreenContent(uiState = uiState, onNavigateToCPU = onNavigateToCPU, onNavigateToGPU = onNavigateToGPU)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreenContent(uiState: HomeUiState, onNavigateToCPU: () -> Unit, onNavigateToGPU: () -> Unit) {
    var showHelpSheet by remember { mutableStateOf(false) }
    val sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden)
    val context = LocalContext.current

    if (showHelpSheet) {
        ModalBottomSheet(
            onDismissRequest = { showHelpSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            HomeHelpContent()
        }
    }

    val infoCards = remember(uiState, context) {
        listOf(
            InfoCardData(
                title = context.getString(R.string.home_title_device),
                headline = if (uiState.device.marketName != "unknown") {
                    "${uiState.device.marketName} (${uiState.device.model})"
                } else {
                    uiState.device.model
                },
                subhead = context.getString(R.string.label_by, uiState.device.brand),
                iconRes = R.drawable.mobile_filled,
                badges = listOf(uiState.device.device).toImmutableList(),
                onHelpClick = { showHelpSheet = true },
            ),
            InfoCardData(
                title = context.getString(R.string.home_title_os),
                headline = "${context.getString(R.string.home_os_name_version, uiState.os.name, uiState.os.version)} " +
                    "(${context.getString(uiState.os.dessertNameRes)})",
                subhead = uiState.os.hyperOSVersion?.let { "HyperOS $it" } ?: "",
                iconRes = R.drawable.android_filled,
                backgroundIconOffset = 45.dp,
                badges = listOf(
                    context.getString(R.string.home_badge_api, uiState.os.sdk),
                    context.getString(R.string.home_badge_patch, uiState.os.securityPatch),
                ).toImmutableList(),
            ),
            InfoCardData(
                title = context.getString(R.string.home_title_display),
                headline = uiState.display.resolution,
                subhead = run {
                    val displaySupported = uiState.display.supportedRefreshRates.joinToString(", ") { "${it}Hz" }
                    val displaySubhead = context.getString(R.string.home_screen_size, uiState.display.screenSizeInches)
                    if (displaySupported.isNotEmpty()) "$displaySubhead\n$displaySupported" else displaySubhead
                },
                iconRes = R.drawable.mobile_3_filled,
                backgroundIconOffset = 20.dp,
                badges = buildList {
                    add(context.getString(R.string.home_badge_density, uiState.display.densityDpi))
                    if (uiState.display.isHdrSupported) {
                        add(context.getString(R.string.home_badge_hdr))
                        addAll(uiState.display.hdrTypes)
                    }
                }.toImmutableList(),
                secondaryBadgeIndices = listOf(0).toImmutableList(),
            ),
            InfoCardData(
                title = context.getString(R.string.home_title_processor),
                headline = uiState.cpu.model,
                subhead = context.getString(R.string.label_by, uiState.cpu.manufacturer),
                iconRes = R.drawable.memory_filled,
                badges = listOf(context.getString(R.string.home_cores_count, uiState.cpu.cores)).toImmutableList(),
                onClick = onNavigateToCPU,
            ),
            InfoCardData(
                title = context.getString(R.string.home_title_graphics),
                headline = uiState.gpu.renderer,
                subhead = context.getString(R.string.label_by, uiState.gpu.vendor),
                iconRes = R.drawable.view_in_ar_filled,
                badges = listOf(
                    context.getString(R.string.home_badge_opengl_es, uiState.gpu.glesVersion),
                    context.getString(R.string.home_badge_vulkan, uiState.gpu.vulkanVersion),
                ).toImmutableList(),
                onClick = onNavigateToGPU,
            ),
        ).toImmutableList()
    }

    ScreenLazyColumn {
        items(
            items = infoCards,
            key = { it.title },
        ) { cardData ->
            InfoOverviewCard(data = cardData)
        }
    }
}

@Composable
private fun HomeHelpContent() {
    val helpItems = persistentListOf(
        stringResource(R.string.home_help_device_os_title) to stringResource(R.string.home_help_device_os_desc),
        stringResource(R.string.home_help_display_title) to stringResource(R.string.home_help_display_desc),
        stringResource(R.string.home_help_processor_title) to stringResource(R.string.home_help_processor_desc),
        stringResource(R.string.home_help_graphics_title) to stringResource(R.string.home_help_graphics_desc),
    )

    HelpBottomSheetContent(helpItems = helpItems)
}
