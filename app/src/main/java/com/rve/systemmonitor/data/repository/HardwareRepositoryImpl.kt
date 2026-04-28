package com.rve.systemmonitor.data.repository

import android.app.Application
import com.rve.systemmonitor.domain.model.Device
import com.rve.systemmonitor.domain.model.Display
import com.rve.systemmonitor.domain.model.GPU
import com.rve.systemmonitor.domain.model.OS
import com.rve.systemmonitor.domain.model.Storage
import com.rve.systemmonitor.domain.repository.HardwareRepository
import com.rve.systemmonitor.utils.DeviceUtils
import com.rve.systemmonitor.utils.DisplayUtils
import com.rve.systemmonitor.utils.GpuUtils
import com.rve.systemmonitor.utils.OSUtils
import com.rve.systemmonitor.utils.StorageUtils
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HardwareRepositoryImpl @Inject constructor(
    private val application: Application,
) : HardwareRepository {

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

    override fun getGpuInfo(): GPU {
        val (renderer, vendor) = GpuUtils.getGpuDetails()
        return GPU(
            renderer = renderer,
            vendor = vendor,
            glesVersion = GpuUtils.getGlesVersion(application),
        )
    }

    override fun getStorageInfo(): Storage {
        return StorageUtils.getStorageData()
    }
}
