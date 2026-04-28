package com.rve.systemmonitor.domain.repository

import com.rve.systemmonitor.domain.model.Device
import com.rve.systemmonitor.domain.model.Display
import com.rve.systemmonitor.domain.model.GPU
import com.rve.systemmonitor.domain.model.OS
import com.rve.systemmonitor.domain.model.Storage

interface HardwareRepository {
    fun getDeviceInfo(): Device
    fun getOSInfo(): OS
    fun getDisplayInfo(): Display
    fun getGpuInfo(): GPU
    fun getStorageInfo(): Storage
}
