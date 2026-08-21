package com.rve.systemmonitor.data.repository

import android.app.Application
import com.rve.systemmonitor.data.di.ApplicationScope
import com.rve.systemmonitor.domain.model.Device
import com.rve.systemmonitor.domain.model.Display
import com.rve.systemmonitor.domain.model.GPU
import com.rve.systemmonitor.domain.model.OS
import com.rve.systemmonitor.domain.model.Storage
import com.rve.systemmonitor.domain.repository.HardwareRepository
import com.rve.systemmonitor.domain.repository.SettingsRepository
import com.rve.systemmonitor.shizuku.ShizukuManager
import com.rve.systemmonitor.utils.DeviceUtils
import com.rve.systemmonitor.utils.DisplayUtils
import com.rve.systemmonitor.utils.FlowUtils
import com.rve.systemmonitor.utils.GpuUtils
import com.rve.systemmonitor.utils.OSUtils
import com.rve.systemmonitor.utils.StorageUtils
import com.rve.systemmonitor.utils.ThermalUtils
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.shareIn

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class HardwareRepositoryImpl @Inject constructor(
    private val application: Application,
    private val settingsRepository: SettingsRepository,
    private val shizukuManager: ShizukuManager,
    @param:ApplicationScope private val externalScope: CoroutineScope,
) : HardwareRepository {
    private val TAG = "HardwareRepository"

    private val device by lazy {
        Device(
            brand = DeviceUtils.getBrand(),
            model = DeviceUtils.getModel(),
            device = DeviceUtils.getDevice(),
            marketName = DeviceUtils.getMarketName(),
        )
    }

    private val os by lazy {
        val currentSdk = OSUtils.getSdkInt()
        OS(
            version = OSUtils.getAndroidVersion(),
            sdk = currentSdk,
            dessertName = "unknown", // We will use dessertNameRes for display
            dessertNameRes = OSUtils.getDessertNameRes(currentSdk),
            securityPatch = OSUtils.getSecurityPatch(),
            hyperOSVersion = OSUtils.getHyperOSVersion(),
        )
    }

    private val display by lazy {
        val (isHdr, hdrTypes) = DisplayUtils.getHdrCapabilities(application)
        Display(
            resolution = DisplayUtils.getResolution(application),
            supportedRefreshRates = DisplayUtils.getSupportedRefreshRates(application).toImmutableList(),
            densityDpi = DisplayUtils.getDensityDpi(application),
            screenSizeInches = DisplayUtils.getScreenSizeInches(application),
            isHdrSupported = isHdr,
            hdrTypes = hdrTypes.toImmutableList(),
        )
    }

    private val gpuStaticInfo by lazy {
        val (renderer, vendor, caps) = GpuUtils.getGpuDetails()
        val (maxTexSize, extCount) = caps
        GPU(
            renderer = renderer,
            vendor = vendor,
            glesVersion = GpuUtils.getGlesVersion(application),
            detailedGlesVersion = GpuUtils.getDetailedGlesVersion(),
            vulkanVersion = GpuUtils.getVulkanVersion(application),
            vulkanDriverVersion = GpuUtils.getVulkanDriverVersion(),
            maxTextureSize = maxTexSize,
            maxCubeMapSize = GpuUtils.getMaxCubeMapTextureSize(),
            max3DTextureSize = GpuUtils.getMax3DTextureSize(),
            maxRenderbufferSize = GpuUtils.getMaxRenderbufferSize(),
            maxMsaaSamples = GpuUtils.getMaxMsaaSamples(),
            maxVertexAttribs = GpuUtils.getMaxVertexAttribs(),
            maxVaryingVectors = GpuUtils.getMaxVaryingVectors(),
            maxVertexUniformVectors = GpuUtils.getMaxVertexUniformVectors(),
            maxFragmentUniformVectors = GpuUtils.getMaxFragmentUniformVectors(),
            maxTextureImageUnits = GpuUtils.getMaxTextureImageUnits(),
            maxVertexTextureImageUnits = GpuUtils.getMaxVertexTextureImageUnits(),
            maxCombinedTextureImageUnits = GpuUtils.getMaxCombinedTextureImageUnits(),
            maxViewportDims = GpuUtils.getMaxViewportDims(),
            maxArrayTextureLayers = GpuUtils.getMaxArrayTextureLayers(),
            maxColorAttachments = GpuUtils.getMaxColorAttachments(),
            maxVertexUniformBlocks = GpuUtils.getMaxVertexUniformBlocks(),
            maxFragmentUniformBlocks = GpuUtils.getMaxFragmentUniformBlocks(),
            maxCombinedUniformBlocks = GpuUtils.getMaxCombinedUniformBlocks(),
            maxComputeUniformBlocks = GpuUtils.getMaxComputeUniformBlocks(),
            maxGeometryUniformBlocks = GpuUtils.getMaxGeometryUniformBlocks(),
            maxTessControlUniformBlocks = GpuUtils.getMaxTessControlUniformBlocks(),
            maxTessEvaluationUniformBlocks = GpuUtils.getMaxTessEvaluationUniformBlocks(),
            subpixelBits = GpuUtils.getSubpixelBits(),
            aliasedLineWidthRange = GpuUtils.getAliasedLineWidthRange(),
            extensionsCount = extCount,
            openGlExtensions = GpuUtils.getOpenGlExtensions().toImmutableList(),
            vulkanExtensionsCount = GpuUtils.getVulkanExtensionsCount(),
            vulkanExtensions = GpuUtils.getVulkanExtensions().toImmutableList(),
            vulkanMaxImage1D = GpuUtils.getVulkanMaxImage1D(),
            vulkanMaxImage2D = GpuUtils.getVulkanMaxImage2D(),
            vulkanMaxImage3D = GpuUtils.getVulkanMaxImage3D(),
            vulkanMaxImageCube = GpuUtils.getVulkanMaxImageCube(),
            vulkanMaxImageArrayLayers = GpuUtils.getVulkanMaxImageArrayLayers(),
            vulkanMaxUniformBufferRange = GpuUtils.getVulkanMaxUniformBufferRange(),
            vulkanMaxStorageBufferRange = GpuUtils.getVulkanMaxStorageBufferRange(),
            vulkanMaxSamplerAnisotropy = GpuUtils.getVulkanMaxSamplerAnisotropy(),
            vulkanMaxFramebufferColorSamples = GpuUtils.getVulkanMaxFramebufferColorSamples(),
            vulkanMaxFramebufferDepthSamples = GpuUtils.getVulkanMaxFramebufferDepthSamples(),
            deviceType = GpuUtils.getVulkanDeviceType(),
            shadingLanguageVersion = GpuUtils.getShadingLanguageVersion(),
        )
    }

    private val sharedGpuStream by lazy {
        combine(
            settingsRepository.gpuRefreshDelay,
            settingsRepository.useShizuku,
            shizukuManager.isShizukuAvailable,
            shizukuManager.hasPermission,
        ) { delayMillis, useShizuku, isAvailable, hasPermission ->
            Quad(delayMillis, useShizuku, isAvailable, hasPermission)
        }.flatMapLatest { (delayMillis, useShizuku, isAvailable, hasPermission) ->
            FlowUtils.pollingFlow(TAG, delayMillis) {
                getGpuInfo(useShizuku, isAvailable, hasPermission)
            }
        }.shareIn(
            scope = externalScope,
            started = SharingStarted.WhileSubscribed(5000),
            replay = 1,
        )
    }

    override fun getDeviceInfo(): Device = device

    override fun getOSInfo(): OS = os

    override fun getDisplayInfo(): Display = display

    override fun getGpuInfo(): GPU = gpuStaticInfo.copy(
        temperature = GpuUtils.getGpuTemperature(),
    )

    private suspend fun getGpuInfo(useShizuku: Boolean, isAvailable: Boolean, hasPermission: Boolean): GPU {
        val nativeTemp = GpuUtils.getGpuTemperature()
        val fallbackTemp = if (nativeTemp <= 0.0 && useShizuku && isAvailable && hasPermission) {
            ThermalUtils.getGpuTemperature(shizukuManager)
        } else {
            0.0
        }
        return gpuStaticInfo.copy(
            temperature = if (nativeTemp > 0.0) nativeTemp else fallbackTemp,
        )
    }

    override fun getGpuStream(): Flow<GPU> = sharedGpuStream

    override fun getStorageInfo(): Storage = StorageUtils.getStorageData()
}

private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
