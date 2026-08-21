package com.rve.systemmonitor.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rve.systemmonitor.R
import com.rve.systemmonitor.domain.model.GPU
import com.rve.systemmonitor.ui.components.ExitUntilCollapsedMediumTopAppBar
import com.rve.systemmonitor.ui.components.ScreenWrapper
import com.rve.systemmonitor.ui.components.card.OverviewCard
import com.rve.systemmonitor.ui.components.card.StandardCard
import com.rve.systemmonitor.ui.components.chip.BadgeChip
import com.rve.systemmonitor.ui.components.item.InfoItem
import com.rve.systemmonitor.ui.components.layout.ScreenLazyColumn
import com.rve.systemmonitor.ui.components.row.TwoColumnInfoRow
import com.rve.systemmonitor.ui.viewmodel.GPUViewModel
import com.rve.systemmonitor.utils.GpuUtils
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GPUScreen(onNavigateBack: () -> Unit, viewModel: GPUViewModel = hiltViewModel()) {
    val gpuInfo by viewModel.gpuInfo.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        topBar = {
            ExitUntilCollapsedMediumTopAppBar(
                title = stringResource(R.string.title_graphics_info),
                onNavigateBack = onNavigateBack,
                scrollBehavior = scrollBehavior,
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            GPUScreenContent(gpuInfo = gpuInfo)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GPUScreenContent(gpuInfo: GPU) {
    var showVulkanExtensions by remember { mutableStateOf(false) }
    var showOpenGlExtensions by remember { mutableStateOf(false) }

    ScreenLazyColumn {
        item {
            OverviewCard(
                iconResId = R.drawable.view_in_ar_filled,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column {
                        Text(
                            text = gpuInfo.renderer,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = stringResource(R.string.label_by, gpuInfo.vendor),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }

                    BadgeChip(
                        text = String.format(Locale.US, "%.1f °C", gpuInfo.temperature),
                        containerColor = MaterialTheme.colorScheme.primary,
                        textColor = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        }

        item {
            StandardCard {
                Text(
                    text = stringResource(R.string.gpu_opengl_es),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp),
                )

                TwoColumnInfoRow(modifier = Modifier.padding(bottom = 16.dp)) {
                    InfoItem(
                        label = stringResource(R.string.gpu_label_version),
                        value = gpuInfo.detailedGlesVersion,
                        modifier = Modifier.weight(1f),
                    )
                    InfoItem(
                        label = stringResource(R.string.gpu_label_shader_version),
                        value = gpuInfo.shadingLanguageVersion,
                        modifier = Modifier.weight(1f),
                    )
                }

                TwoColumnInfoRow {
                    InfoItem(
                        label = stringResource(R.string.gpu_label_extensions),
                        value = "${gpuInfo.extensionsCount}",
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }

                if (gpuInfo.openGlExtensions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { showOpenGlExtensions = true },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                        shapes = ButtonDefaults.shapes(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary,
                        ),
                    ) {
                        Text("Show all extensions")
                    }
                }
            }
        }

        item {
            StandardCard {
                Text(
                    text = "OpenGL ES Limits",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp),
                )

                TwoColumnInfoRow(modifier = Modifier.padding(bottom = 16.dp)) {
                    InfoItem(
                        label = stringResource(R.string.gpu_label_max_texture_size),
                        value = if (gpuInfo.maxTextureSize > 0) {
                            stringResource(R.string.gpu_max_texture_size_format, gpuInfo.maxTextureSize)
                        } else {
                            stringResource(R.string.value_unknown)
                        },
                        modifier = Modifier.weight(1f),
                    )
                    InfoItem(
                        label = stringResource(R.string.gpu_label_max_cube_map_size),
                        value = if (gpuInfo.maxCubeMapSize > 0) {
                            stringResource(R.string.gpu_max_texture_size_format, gpuInfo.maxCubeMapSize)
                        } else {
                            stringResource(R.string.value_unknown)
                        },
                        modifier = Modifier.weight(1f),
                    )
                }

                TwoColumnInfoRow(modifier = Modifier.padding(bottom = 16.dp)) {
                    InfoItem(
                        label = stringResource(R.string.gpu_label_max_3d_texture_size),
                        value = if (gpuInfo.max3DTextureSize > 0) {
                            stringResource(R.string.gpu_max_texture_size_format, gpuInfo.max3DTextureSize)
                        } else {
                            stringResource(R.string.value_unknown)
                        },
                        modifier = Modifier.weight(1f),
                    )
                    InfoItem(
                        label = stringResource(R.string.gpu_label_max_renderbuffer_size),
                        value = if (gpuInfo.maxRenderbufferSize > 0) {
                            stringResource(R.string.gpu_max_texture_size_format, gpuInfo.maxRenderbufferSize)
                        } else {
                            stringResource(R.string.value_unknown)
                        },
                        modifier = Modifier.weight(1f),
                    )
                }

                TwoColumnInfoRow(modifier = Modifier.padding(bottom = 16.dp)) {
                    InfoItem(
                        label = stringResource(R.string.gpu_label_max_msaa_samples),
                        value = if (gpuInfo.maxMsaaSamples > 0) {
                            "${gpuInfo.maxMsaaSamples}x"
                        } else {
                            stringResource(R.string.value_unknown)
                        },
                        modifier = Modifier.weight(1f),
                    )
                    InfoItem(
                        label = stringResource(R.string.gpu_label_max_vertex_attribs),
                        value = if (gpuInfo.maxVertexAttribs > 0) {
                            "${gpuInfo.maxVertexAttribs}"
                        } else {
                            stringResource(R.string.value_unknown)
                        },
                        modifier = Modifier.weight(1f),
                    )
                }

                TwoColumnInfoRow(modifier = Modifier.padding(bottom = 16.dp)) {
                    InfoItem(
                        label = stringResource(R.string.gpu_label_max_varying_vectors),
                        value = if (gpuInfo.maxVaryingVectors > 0) {
                            "${gpuInfo.maxVaryingVectors * 4} / ${gpuInfo.maxVaryingVectors}"
                        } else {
                            stringResource(R.string.value_unknown)
                        },
                        modifier = Modifier.weight(1f),
                    )
                    InfoItem(
                        label = stringResource(R.string.gpu_label_max_vertex_uniform_vectors),
                        value = if (gpuInfo.maxVertexUniformVectors > 0) {
                            "${gpuInfo.maxVertexUniformVectors}"
                        } else {
                            stringResource(R.string.value_unknown)
                        },
                        modifier = Modifier.weight(1f),
                    )
                }

                TwoColumnInfoRow(modifier = Modifier.padding(bottom = 16.dp)) {
                    InfoItem(
                        label = stringResource(R.string.gpu_label_max_fragment_uniform_vectors),
                        value = if (gpuInfo.maxFragmentUniformVectors > 0) {
                            "${gpuInfo.maxFragmentUniformVectors}"
                        } else {
                            stringResource(R.string.value_unknown)
                        },
                        modifier = Modifier.weight(1f),
                    )
                    InfoItem(
                        label = stringResource(R.string.gpu_label_max_texture_image_units),
                        value = if (gpuInfo.maxTextureImageUnits > 0) {
                            "${gpuInfo.maxTextureImageUnits}"
                        } else {
                            stringResource(R.string.value_unknown)
                        },
                        modifier = Modifier.weight(1f),
                    )
                }

                TwoColumnInfoRow(modifier = Modifier.padding(bottom = 16.dp)) {
                    InfoItem(
                        label = stringResource(R.string.gpu_label_max_vertex_texture_image_units),
                        value = if (gpuInfo.maxVertexTextureImageUnits > 0) {
                            "${gpuInfo.maxVertexTextureImageUnits}"
                        } else {
                            stringResource(R.string.value_unknown)
                        },
                        modifier = Modifier.weight(1f),
                    )
                    InfoItem(
                        label = stringResource(R.string.gpu_label_max_combined_texture_image_units),
                        value = if (gpuInfo.maxCombinedTextureImageUnits > 0) {
                            "${gpuInfo.maxCombinedTextureImageUnits}"
                        } else {
                            stringResource(R.string.value_unknown)
                        },
                        modifier = Modifier.weight(1f),
                    )
                }

                TwoColumnInfoRow(modifier = Modifier.padding(bottom = 16.dp)) {
                    InfoItem(
                        label = stringResource(R.string.gpu_label_max_viewport_dims),
                        value = if (gpuInfo.maxViewportDims.first > 0 && gpuInfo.maxViewportDims.second > 0) {
                            "${gpuInfo.maxViewportDims.first} x ${gpuInfo.maxViewportDims.second}"
                        } else {
                            stringResource(R.string.value_unknown)
                        },
                        modifier = Modifier.weight(1f),
                    )
                    InfoItem(
                        label = stringResource(R.string.gpu_label_max_array_texture_layers),
                        value = if (gpuInfo.maxArrayTextureLayers > 0) {
                            "${gpuInfo.maxArrayTextureLayers}"
                        } else {
                            stringResource(R.string.value_unknown)
                        },
                        modifier = Modifier.weight(1f),
                    )
                }

                TwoColumnInfoRow(modifier = Modifier.padding(bottom = 16.dp)) {
                    InfoItem(
                        label = stringResource(R.string.gpu_label_max_color_attachments),
                        value = if (gpuInfo.maxColorAttachments > 0) {
                            "${gpuInfo.maxColorAttachments}"
                        } else {
                            stringResource(R.string.value_unknown)
                        },
                        modifier = Modifier.weight(1f),
                    )
                    InfoItem(
                        label = stringResource(R.string.gpu_label_max_vertex_uniform_blocks),
                        value = if (gpuInfo.maxVertexUniformBlocks > 0) {
                            "${gpuInfo.maxVertexUniformBlocks}"
                        } else {
                            stringResource(R.string.value_unknown)
                        },
                        modifier = Modifier.weight(1f),
                    )
                }

                TwoColumnInfoRow(modifier = Modifier.padding(bottom = 16.dp)) {
                    InfoItem(
                        label = stringResource(R.string.gpu_label_max_fragment_uniform_blocks),
                        value = if (gpuInfo.maxFragmentUniformBlocks > 0) {
                            "${gpuInfo.maxFragmentUniformBlocks}"
                        } else {
                            stringResource(R.string.value_unknown)
                        },
                        modifier = Modifier.weight(1f),
                    )
                    InfoItem(
                        label = stringResource(R.string.gpu_label_max_combined_uniform_blocks),
                        value = if (gpuInfo.maxCombinedUniformBlocks > 0) {
                            "${gpuInfo.maxCombinedUniformBlocks}"
                        } else {
                            stringResource(R.string.value_unknown)
                        },
                        modifier = Modifier.weight(1f),
                    )
                }

                TwoColumnInfoRow(modifier = Modifier.padding(bottom = 16.dp)) {
                    InfoItem(
                        label = stringResource(R.string.gpu_label_max_compute_uniform_blocks),
                        value = if (gpuInfo.maxComputeUniformBlocks > 0) {
                            "${gpuInfo.maxComputeUniformBlocks}"
                        } else {
                            stringResource(R.string.value_unknown)
                        },
                        modifier = Modifier.weight(1f),
                    )
                    InfoItem(
                        label = stringResource(R.string.gpu_label_max_geometry_uniform_blocks),
                        value = if (gpuInfo.maxGeometryUniformBlocks > 0) {
                            "${gpuInfo.maxGeometryUniformBlocks}"
                        } else {
                            stringResource(R.string.value_unknown)
                        },
                        modifier = Modifier.weight(1f),
                    )
                }

                TwoColumnInfoRow {
                    InfoItem(
                        label = stringResource(R.string.gpu_label_max_tess_control_uniform_blocks),
                        value = if (gpuInfo.maxTessControlUniformBlocks > 0) {
                            "${gpuInfo.maxTessControlUniformBlocks}"
                        } else {
                            stringResource(R.string.value_unknown)
                        },
                        modifier = Modifier.weight(1f),
                    )
                    InfoItem(
                        label = stringResource(R.string.gpu_label_max_tess_evaluation_uniform_blocks),
                        value = if (gpuInfo.maxTessEvaluationUniformBlocks > 0) {
                            "${gpuInfo.maxTessEvaluationUniformBlocks}"
                        } else {
                            stringResource(R.string.value_unknown)
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        item {
            StandardCard {
                Text(
                    text = stringResource(R.string.gpu_vulkan),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp),
                )

                TwoColumnInfoRow(modifier = Modifier.padding(bottom = 16.dp)) {
                    InfoItem(
                        label = stringResource(R.string.gpu_label_api_version),
                        value = gpuInfo.vulkanVersion,
                        modifier = Modifier.weight(1f),
                    )
                    InfoItem(
                        label = stringResource(R.string.gpu_label_driver_version),
                        value = gpuInfo.vulkanDriverVersion,
                        modifier = Modifier.weight(1f),
                    )
                }

                TwoColumnInfoRow(modifier = Modifier.padding(bottom = 16.dp)) {
                    InfoItem(
                        label = stringResource(R.string.gpu_label_device_type),
                        value = gpuInfo.deviceType,
                        modifier = Modifier.weight(1f),
                    )
                    InfoItem(
                        label = stringResource(R.string.gpu_label_extensions),
                        value = "${gpuInfo.vulkanExtensionsCount}",
                        modifier = Modifier.weight(1f),
                    )
                }

                if (gpuInfo.vulkanExtensions.isNotEmpty()) {
                    OutlinedButton(
                        onClick = { showVulkanExtensions = true },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                        shapes = ButtonDefaults.shapes(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary,
                        ),
                    ) {
                        Text("Show all extensions")
                    }
                }
            }
        }

        item {
            StandardCard {
                Text(
                    text = "Vulkan Limits",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp),
                )

                TwoColumnInfoRow(modifier = Modifier.padding(bottom = 16.dp)) {
                    InfoItem(
                        label = "Max 1D Image",
                        value = if (gpuInfo.vulkanMaxImage1D >
                            0
                        ) "${gpuInfo.vulkanMaxImage1D} px" else stringResource(R.string.value_unknown),
                        modifier = Modifier.weight(1f),
                    )
                    InfoItem(
                        label = "Max 2D Image",
                        value = if (gpuInfo.vulkanMaxImage2D >
                            0
                        ) "${gpuInfo.vulkanMaxImage2D} x ${gpuInfo.vulkanMaxImage2D} px" else stringResource(R.string.value_unknown),
                        modifier = Modifier.weight(1f),
                    )
                }

                TwoColumnInfoRow(modifier = Modifier.padding(bottom = 16.dp)) {
                    InfoItem(
                        label = "Max 3D Image",
                        value =
                            if (gpuInfo.vulkanMaxImage3D > 0) {
                                "${gpuInfo.vulkanMaxImage3D} x ${gpuInfo.vulkanMaxImage3D} x ${gpuInfo.vulkanMaxImage3D} px"
                            } else {
                                stringResource(R.string.value_unknown)
                            },
                        modifier = Modifier.weight(1f),
                    )
                    InfoItem(
                        label = "Max Cube Image",
                        value =
                            if (gpuInfo.vulkanMaxImageCube > 0) {
                                "${gpuInfo.vulkanMaxImageCube} x ${gpuInfo.vulkanMaxImageCube} px"
                            } else {
                                stringResource(R.string.value_unknown)
                            },
                        modifier = Modifier.weight(1f),
                    )
                }

                TwoColumnInfoRow(modifier = Modifier.padding(bottom = 16.dp)) {
                    InfoItem(
                        label = "Max Array Layers",
                        value = if (gpuInfo.vulkanMaxImageArrayLayers >
                            0
                        ) "${gpuInfo.vulkanMaxImageArrayLayers}" else stringResource(R.string.value_unknown),
                        modifier = Modifier.weight(1f),
                    )
                    InfoItem(
                        label = "Uniform Buffer Range",
                        value =
                            if (gpuInfo.vulkanMaxUniformBufferRange > 0) {
                                GpuUtils.formatBinarySize(gpuInfo.vulkanMaxUniformBufferRange.toLong())
                            } else {
                                stringResource(R.string.value_unknown)
                            },
                        modifier = Modifier.weight(1f),
                    )
                }

                TwoColumnInfoRow(modifier = Modifier.padding(bottom = 16.dp)) {
                    InfoItem(
                        label = "Storage Buffer Range",
                        value =
                            if (gpuInfo.vulkanMaxStorageBufferRange > 0) {
                                GpuUtils.formatBinarySize(gpuInfo.vulkanMaxStorageBufferRange.toLong())
                            } else {
                                stringResource(R.string.value_unknown)
                            },
                        modifier = Modifier.weight(1f),
                    )
                    InfoItem(
                        label = "Max Anisotropy",
                        value = if (gpuInfo.vulkanMaxSamplerAnisotropy >
                            0f
                        ) "${gpuInfo.vulkanMaxSamplerAnisotropy.toInt()}x" else stringResource(R.string.value_unknown),
                        modifier = Modifier.weight(1f),
                    )
                }

                TwoColumnInfoRow {
                    InfoItem(
                        label = "Color Max Samples",
                        value = if (gpuInfo.vulkanMaxFramebufferColorSamples >
                            0
                        ) "${gpuInfo.vulkanMaxFramebufferColorSamples}x" else stringResource(R.string.value_unknown),
                        modifier = Modifier.weight(1f),
                    )
                    InfoItem(
                        label = "Depth Max Samples",
                        value = if (gpuInfo.vulkanMaxFramebufferDepthSamples >
                            0
                        ) "${gpuInfo.vulkanMaxFramebufferDepthSamples}x" else stringResource(R.string.value_unknown),
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }

    if (showVulkanExtensions) {
        val sheetState = androidx.compose.material3.rememberBottomSheetState(initialValue = SheetValue.Hidden)
        ModalBottomSheet(
            onDismissRequest = { showVulkanExtensions = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp),
            ) {
                Text(
                    text = "Vulkan Extensions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp),
                )

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(gpuInfo.vulkanExtensions) { extension ->
                        Text(
                            text = extension,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }

    if (showOpenGlExtensions) {
        val sheetState = androidx.compose.material3.rememberBottomSheetState(initialValue = SheetValue.Hidden)
        ModalBottomSheet(
            onDismissRequest = { showOpenGlExtensions = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp),
            ) {
                Text(
                    text = "OpenGL Extensions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp),
                )

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(gpuInfo.openGlExtensions) { extension ->
                        Text(
                            text = extension,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
