package com.rve.systemmonitor.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rve.systemmonitor.ui.data.CPU
import com.rve.systemmonitor.ui.data.Device
import com.rve.systemmonitor.ui.data.Display
import com.rve.systemmonitor.ui.data.GPU
import com.rve.systemmonitor.ui.data.OS
import com.rve.systemmonitor.ui.data.RAM
import com.rve.systemmonitor.ui.data.ZRAM
import com.rve.systemmonitor.utils.CpuUtils
import com.rve.systemmonitor.utils.DeviceUtils
import com.rve.systemmonitor.utils.DisplayUtils
import com.rve.systemmonitor.utils.GpuUtils
import com.rve.systemmonitor.utils.MemoryUtils
import com.rve.systemmonitor.utils.OSUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val _device = MutableStateFlow(Device())
    val device: StateFlow<Device> = _device

    private val _os = MutableStateFlow(OS())
    val os: StateFlow<OS> = _os

    private val _display = MutableStateFlow(Display())
    val display: StateFlow<Display> = _display

    private val _cpu = MutableStateFlow(CPU())
    val cpu: StateFlow<CPU> = _cpu

    private val _gpu = MutableStateFlow(GPU())
    val gpu: StateFlow<GPU> = _gpu

    private val _ram = MutableStateFlow(RAM())
    val ram: StateFlow<RAM> = _ram

    private val _zram = MutableStateFlow(ZRAM())
    val zram: StateFlow<ZRAM> = _zram

    private var memoryJob: Job? = null

    init {
        updateDeviceInfo()
        updateOSInfo()
        updateDisplayInfo()
        updateCpuInfo()
        updateGpuInfo()
        updateMemoryInfo()
    }

    fun updateDeviceInfo() {
        viewModelScope.launch {
            _device.update {
                Device(
                    manufacturer = DeviceUtils.getManufacturer(),
                    model = DeviceUtils.getModel(),
                    device = DeviceUtils.getDevice(),
                )
            }
        }
    }

    fun updateOSInfo() {
        viewModelScope.launch {
            val currentSdk = OSUtils.getSdkInt()

            _os.update {
                OS(
                    version = OSUtils.getAndroidVersion(),
                    sdk = currentSdk,
                    dessertName = OSUtils.getDessertName(currentSdk),
                    securityPatch = OSUtils.getSecurityPatch(),
                )
            }
        }
    }

    fun updateDisplayInfo() {
        viewModelScope.launch {
            val context = getApplication<Application>()
            _display.update {
                Display(
                    resolution = DisplayUtils.getResolution(context),
                    refreshRate = DisplayUtils.getRefreshRate(context),
                    densityDpi = DisplayUtils.getDensityDpi(context),
                    screenSizeInches = DisplayUtils.getScreenSizeInches(context),
                )
            }
        }
    }

    fun updateCpuInfo() {
        _cpu.update {
            CPU(
                manufacturer = CpuUtils.getSocManufacturer(),
                model = CpuUtils.getSocModel(),
                cores = CpuUtils.getCoreCount(),
            )
        }
    }

    fun updateGpuInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            val (renderer, vendor) = GpuUtils.getGpuDetails()

            _gpu.update {
                GPU(
                    renderer = renderer,
                    vendor = vendor,
                    glesVersion = GpuUtils.getGlesVersion(context)
                )
            }
        }
    }

    fun updateMemoryInfo() {
        memoryJob?.cancel()

        memoryJob = viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()

            while (isActive) {
                val newRamData = MemoryUtils.getRamData(context)
                val newZramData = MemoryUtils.getZramData()
                _ram.update { newRamData }
                _zram.update { newZramData }
                delay(2000L)
            }
        }
    }

    fun stopMemoryJob() {
        memoryJob?.cancel()
        memoryJob = null
    }

    override fun onCleared() {
        stopMemoryJob()
    }
}
