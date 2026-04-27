@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.rve.systemmonitor.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composables.icons.materialsymbols.roundedfilled.R.drawable.materialsymbols_ic_memory_alt_rounded_filled
import com.rve.systemmonitor.domain.model.RAM
import com.rve.systemmonitor.domain.model.ZRAM
import com.rve.systemmonitor.ui.components.AppBars.SimpleTopAppBar
import com.rve.systemmonitor.ui.viewmodel.MemoryUiState
import com.rve.systemmonitor.ui.viewmodel.MemoryViewModel

@Composable
fun MemoryScreen(isActive: Boolean, viewModel: MemoryViewModel = hiltViewModel(), onNavigateToSettings: () -> Unit) {
    val initialUiState = remember { viewModel.uiState.value }
    val uiState by if (isActive) {
        viewModel.uiState.collectAsStateWithLifecycle()
    } else {
        remember { kotlinx.coroutines.flow.emptyFlow<MemoryUiState>() }.collectAsStateWithLifecycle(initialUiState)
    }

    Scaffold(
        topBar = {
            SimpleTopAppBar(
                title = "RvSystem Monitor",
                subtitle = "Memory",
                onNavigateToSettings = onNavigateToSettings,
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding()),
            shape = RoundedCornerShape(
                topStart = 32.dp,
                topEnd = 32.dp,
            ),
        ) {
            LazyColumn(
                contentPadding = PaddingValues(
                    top = 16.dp,
                    bottom = innerPadding.calculateBottomPadding() + 96.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(horizontal = 16.dp),
            ) {
                item {
                    MemoryCard(
                        ram = uiState.ram,
                        zram = uiState.zram,
                    )
                }
            }
        }
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
                painter = painterResource(id = materialsymbols_ic_memory_alt_rounded_filled),
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(id = materialsymbols_ic_memory_alt_rounded_filled),
                            contentDescription = "Memory Icon",
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                    }

                    Text(
                        text = "Memory",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

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

@Composable
private fun BadgeChip(text: String, containerColor: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(containerColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            fontWeight = FontWeight.Bold,
        )
    }
}
