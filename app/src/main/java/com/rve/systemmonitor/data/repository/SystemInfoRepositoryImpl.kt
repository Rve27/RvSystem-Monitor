package com.rve.systemmonitor.data.repository

import android.app.Application
import com.rve.systemmonitor.domain.model.CPU
import com.rve.systemmonitor.domain.model.Device
import com.rve.systemmonitor.domain.model.Display
import com.rve.systemmonitor.domain.model.GPU
import com.rve.systemmonitor.domain.model.OS
import com.rve.systemmonitor.domain.model.RAM
import com.rve.systemmonitor.domain.model.ZRAM
import com.rve.systemmonitor.domain.repository.SystemInfoRepository
import com.rve.systemmonitor.utils.CpuUtils
import com.rve.systemmonitor.utils.DeviceUtils
import com.rve.systemmonitor.utils.DisplayUtils
import com.rve.systemmonitor.utils.GpuUtils
import com.rve.systemmonitor.utils.MemoryUtils
import com.rve.systemmonitor.utils.OSUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SystemInfoRepositoryImpl @Inject constructor(
    private val application: Application
) : SystemInfoRepository {

    override fun getDeviceInfo(): Device {
        return Device(
            manufacturer = DeviceUtils.getManufacturer(),
            model = DeviceUtils.getModel(),
            device = DeviceUtils.getDevice(),
        )
    }

    override fun getOSInfo(): OS {
        val currentSdk = OSUtils.getSdkInt()
        return OS(
            version = OSUtils.getAndroidVersion(),
            sdk = currentSdk,
            dessertName = OSUtils.getDessertName(currentSdk),
            securityPatch = OSUtils.getSecurityPatch(),
        )
    }

    override fun getDisplayInfo(): Display {
        return Display(
            resolution = DisplayUtils.getResolution(application),
            refreshRate = DisplayUtils.getRefreshRate(application),
            densityDpi = DisplayUtils.getDensityDpi(application),
            screenSizeInches = DisplayUtils.getScreenSizeInches(application),
        )
    }

    override fun getCpuInfo(): CPU {
        return CPU(
            manufacturer = CpuUtils.getSocManufacturer(),
            model = CpuUtils.getSocModel(),
            cores = CpuUtils.getCoreCount(),
        )
    }

    override suspend fun getGpuInfo(): GPU {
        val (renderer, vendor) = GpuUtils.getGpuDetails()
        return GPU(
            renderer = renderer,
            vendor = vendor,
            glesVersion = GpuUtils.getGlesVersion(application),
        )
    }

    override fun getMemoryInfo(): Flow<Pair<RAM, ZRAM>> = flow {
        while (true) {
            val ram = MemoryUtils.getRamData()
            val zram = MemoryUtils.getZramData()
            emit(ram to zram)
            delay(2000L)
        }
    }
}
