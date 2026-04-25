package com.rve.systemmonitor.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rve.systemmonitor.ui.data.CPU
import com.rve.systemmonitor.ui.data.Device
import com.rve.systemmonitor.ui.data.Display
import com.rve.systemmonitor.ui.data.GPU
import com.rve.systemmonitor.ui.data.OS
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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var memoryJob: Job? = null

    init {
        updateDeviceInfo()
        updateOSInfo()
        updateDisplayInfo()
        updateCpuInfo()
        updateGpuInfo()
        updateMemoryInfo()
    }

    private fun updateDeviceInfo() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    device = Device(
                        manufacturer = DeviceUtils.getManufacturer(),
                        model = DeviceUtils.getModel(),
                        device = DeviceUtils.getDevice(),
                    ),
                )
            }
        }
    }

    private fun updateOSInfo() {
        viewModelScope.launch {
            val currentSdk = OSUtils.getSdkInt()

            _uiState.update {
                it.copy(
                    os = OS(
                        version = OSUtils.getAndroidVersion(),
                        sdk = currentSdk,
                        dessertName = OSUtils.getDessertName(currentSdk),
                        securityPatch = OSUtils.getSecurityPatch(),
                    ),
                )
            }
        }
    }

    private fun updateDisplayInfo() {
        viewModelScope.launch {
            val context = getApplication<Application>()
            _uiState.update {
                it.copy(
                    display = Display(
                        resolution = DisplayUtils.getResolution(context),
                        refreshRate = DisplayUtils.getRefreshRate(context),
                        densityDpi = DisplayUtils.getDensityDpi(context),
                        screenSizeInches = DisplayUtils.getScreenSizeInches(context),
                    ),
                )
            }
        }
    }

    private fun updateCpuInfo() {
        _uiState.update {
            it.copy(
                cpu = CPU(
                    manufacturer = CpuUtils.getSocManufacturer(),
                    model = CpuUtils.getSocModel(),
                    cores = CpuUtils.getCoreCount(),
                ),
            )
        }
    }

    private fun updateGpuInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            val (renderer, vendor) = GpuUtils.getGpuDetails()

            _uiState.update {
                it.copy(
                    gpu = GPU(
                        renderer = renderer,
                        vendor = vendor,
                        glesVersion = GpuUtils.getGlesVersion(context),
                    ),
                )
            }
        }
    }

    private fun updateMemoryInfo() {
        memoryJob?.cancel()

        memoryJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                val newRamData = MemoryUtils.getRamData()
                val newZramData = MemoryUtils.getZramData()
                _uiState.update {
                    it.copy(
                        ram = newRamData,
                        zram = newZramData,
                    )
                }
                delay(2000L)
            }
        }
    }

    private fun stopMemoryJob() {
        memoryJob?.cancel()
        memoryJob = null
    }

    override fun onCleared() {
        stopMemoryJob()
    }
}
