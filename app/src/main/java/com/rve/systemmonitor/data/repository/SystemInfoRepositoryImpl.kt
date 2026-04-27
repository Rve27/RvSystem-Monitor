package com.rve.systemmonitor.data.repository

import android.app.Application
import android.util.Log
import com.rve.systemmonitor.BuildConfig
import com.rve.systemmonitor.domain.model.CPU
import com.rve.systemmonitor.domain.model.Device
import com.rve.systemmonitor.domain.model.Display
import com.rve.systemmonitor.domain.model.GPU
import com.rve.systemmonitor.domain.model.OS
import com.rve.systemmonitor.domain.model.RAM
import com.rve.systemmonitor.domain.model.ZRAM
import com.rve.systemmonitor.domain.repository.SettingsRepository
import com.rve.systemmonitor.domain.repository.SystemInfoRepository
import com.rve.systemmonitor.utils.CpuUtils
import com.rve.systemmonitor.utils.DeviceUtils
import com.rve.systemmonitor.utils.DisplayUtils
import com.rve.systemmonitor.utils.GpuUtils
import com.rve.systemmonitor.utils.MemoryUtils
import com.rve.systemmonitor.utils.OSUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SystemInfoRepositoryImpl @Inject constructor(
    private val application: Application,
    private val settingsRepository: SettingsRepository,
) : SystemInfoRepository {
    private val TAG = "SystemInfoRepository"

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
            hardware = CpuUtils.getHardware(),
            board = CpuUtils.getBoard(),
            architecture = CpuUtils.getArchitecture(),
        )
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    override fun getCpuStream(): Flow<CPU> = settingsRepository.cpuRefreshDelay.flatMapLatest { delayMillis ->
        flow {
            if (BuildConfig.DEBUG) Log.d(TAG, "CPU Stream Started with delay: $delayMillis")

            val manufacturer = CpuUtils.getSocManufacturer()
            val model = CpuUtils.getSocModel()
            val cores = CpuUtils.getCoreCount()
            val hardware = CpuUtils.getHardware()
            val board = CpuUtils.getBoard()
            val architecture = CpuUtils.getArchitecture()

            val staticCoreInfo = (0 until cores).map { i ->
                val min = CpuUtils.getCoreFrequency(i, "min_info")
                val max = CpuUtils.getCoreFrequency(i, "max_info")
                val governor = CpuUtils.getCoreGovernor(i)
                Triple(min, max, governor)
            }

            while (true) {
                if (BuildConfig.DEBUG) Log.d(TAG, "CPU Stream Updated")
                val coreDetails = mutableListOf<com.rve.systemmonitor.domain.model.CoreDetail>()

                for (i in 0 until cores) {
                    coreDetails.add(
                        com.rve.systemmonitor.domain.model.CoreDetail(
                            id = i,
                            currentFreq = CpuUtils.getCoreFrequency(i, "cur"),
                            minFreq = staticCoreInfo[i].first,
                            maxFreq = staticCoreInfo[i].second,
                            governor = staticCoreInfo[i].third,
                        ),
                    )
                }

                emit(
                    CPU(
                        manufacturer = manufacturer,
                        model = model,
                        cores = cores,
                        hardware = hardware,
                        board = board,
                        architecture = architecture,
                        coreDetails = coreDetails,
                    ),
                )
                delay(delayMillis)
            }
        }.onCompletion {
            if (BuildConfig.DEBUG) Log.d(TAG, "CPU Stream Stopped")
        }
    }

    override fun getGpuInfo(): GPU {
        val (renderer, vendor) = GpuUtils.getGpuDetails()
        return GPU(
            renderer = renderer,
            vendor = vendor,
            glesVersion = GpuUtils.getGlesVersion(application),
        )
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    override fun getMemoryInfo(): Flow<Pair<RAM, ZRAM>> = settingsRepository.memoryRefreshDelay.flatMapLatest { delayMillis ->
        flow {
            if (BuildConfig.DEBUG) Log.d(TAG, "Memory Stream Started with delay: $delayMillis")
            while (true) {
                if (BuildConfig.DEBUG) Log.d(TAG, "Memory Stream Updated")
                val ram = MemoryUtils.getRamData()
                val zram = MemoryUtils.getZramData()
                emit(ram to zram)
                delay(delayMillis)
            }
        }.onCompletion {
            if (BuildConfig.DEBUG) Log.d(TAG, "Memory Stream Stopped")
        }
    }
}
