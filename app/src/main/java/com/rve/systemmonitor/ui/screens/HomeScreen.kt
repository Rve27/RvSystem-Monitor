package com.rve.systemmonitor.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rve.systemmonitor.R
import com.rve.systemmonitor.ui.components.card.InfoCardData
import com.rve.systemmonitor.ui.components.card.InfoOverviewCard
import com.rve.systemmonitor.ui.components.item.HelpItem
import com.rve.systemmonitor.ui.viewmodel.HomeUiState
import com.rve.systemmonitor.ui.viewmodel.HomeViewModel
import kotlinx.coroutines.flow.emptyFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(isActive: Boolean, viewModel: HomeViewModel = hiltViewModel()) {
    val initialUiState = remember { viewModel.uiState.value }
    val uiState by if (isActive) {
        viewModel.uiState.collectAsStateWithLifecycle()
    } else {
        remember { emptyFlow<HomeUiState>() }.collectAsStateWithLifecycle(initialUiState)
    }

    var showHelpSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    if (showHelpSheet) {
        ModalBottomSheet(
            onDismissRequest = { showHelpSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            HomeHelpContent()
        }
    }

    val infoCards = remember(uiState) {
        listOf(
            InfoCardData(
                title = "Device",
                headline = uiState.device.model,
                subhead = "by ${uiState.device.manufacturer}",
                iconRes = R.drawable.mobile_filled,
                badges = listOf(uiState.device.device),
                onHelpClick = { showHelpSheet = true },
            ),
            InfoCardData(
                title = "Operating System",
                headline = "${uiState.os.name} ${uiState.os.version}",
                subhead = uiState.os.dessertName,
                iconRes = R.drawable.android_filled,
                backgroundIconOffset = 45.dp,
                badges = listOf("API ${uiState.os.sdk}", "Patch: ${uiState.os.securityPatch}"),
            ),
            InfoCardData(
                title = "Display",
                headline = uiState.display.resolution,
                subhead = "${uiState.display.screenSizeInches}\" Screen Size",
                iconRes = R.drawable.mobile_3_filled,
                backgroundIconOffset = 20.dp,
                badges = buildList {
                    add("${uiState.display.refreshRate}Hz")
                    add("${uiState.display.densityDpi} dpi")
                    if (uiState.display.isHdrSupported) {
                        add("HDR")
                        addAll(uiState.display.hdrTypes)
                    }
                },
                secondaryBadgeIndices = listOf(0, 1),
            ),
            InfoCardData(
                title = "Processor",
                headline = uiState.cpu.model,
                subhead = "by ${uiState.cpu.manufacturer}",
                iconRes = R.drawable.memory_filled,
                badges = listOf("${uiState.cpu.cores} Cores"),
            ),
            InfoCardData(
                title = "Graphics",
                headline = uiState.gpu.renderer,
                subhead = "by ${uiState.gpu.vendor}",
                iconRes = R.drawable.view_in_ar_filled,
                badges = listOf(
                    "OpenGL ES ${uiState.gpu.glesVersion}",
                    "Vulkan ${uiState.gpu.vulkanVersion}",
                ),
            ),
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
    val helpItems = listOf(
        "Device & Operating System" to "Information such as model, manufacturer, and Android version is extracted from " +
            "the system's Build properties and secure patch levels.",
        "Display" to "Screen resolution, refresh rate, and density metrics are obtained via the " +
            "Android WindowManager and Display APIs.",
        "Processor (CPU)" to "Detailed hardware info, including core count and architecture, is parsed from " +
            "Linux kernel files (/proc/cpuinfo) using the high-performance Rust backend.",
        "Graphics (GPU)" to "The graphics renderer, vendor, and OpenGL ES version are retrieved directly " +
            "from the device's GPU through the EGL context.",
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
