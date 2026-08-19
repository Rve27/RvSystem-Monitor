package com.rve.systemmonitor.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class GPU(
    val renderer: String = "unknown",
    val vendor: String = "unknown",
    val glesVersion: String = "unknown",
    val detailedGlesVersion: String = "unknown",
    val vulkanVersion: String = "unknown",
    val vulkanDriverVersion: String = "unknown",
    val temperature: Double = 0.0,
    val maxTextureSize: Int = 0,
    val maxCubeMapSize: Int = 0,
    val extensionsCount: Int = 0,
    val openGlExtensions: ImmutableList<String> = persistentListOf(),
    val vulkanExtensionsCount: Int = 0,
    val vulkanExtensions: ImmutableList<String> = persistentListOf(),
    val vulkanMaxImage1D: Int = 0,
    val vulkanMaxImage2D: Int = 0,
    val vulkanMaxImage3D: Int = 0,
    val vulkanMaxImageCube: Int = 0,
    val vulkanMaxImageArrayLayers: Int = 0,
    val vulkanMaxUniformBufferRange: Int = 0,
    val vulkanMaxStorageBufferRange: Int = 0,
    val vulkanMaxSamplerAnisotropy: Float = 0f,
    val vulkanMaxFramebufferColorSamples: Int = 0,
    val vulkanMaxFramebufferDepthSamples: Int = 0,
    val deviceType: String = "unknown",
    val shadingLanguageVersion: String = "unknown",
)
