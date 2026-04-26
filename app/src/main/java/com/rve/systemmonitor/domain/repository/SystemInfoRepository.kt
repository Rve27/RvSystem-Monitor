package com.rve.systemmonitor.domain.repository

import com.rve.systemmonitor.domain.model.CPU
import com.rve.systemmonitor.domain.model.Device
import com.rve.systemmonitor.domain.model.Display
import com.rve.systemmonitor.domain.model.GPU
import com.rve.systemmonitor.domain.model.OS
import com.rve.systemmonitor.domain.model.RAM
import com.rve.systemmonitor.domain.model.ZRAM
import kotlinx.coroutines.flow.Flow

interface SystemInfoRepository {
    fun getDeviceInfo(): Device
    fun getOSInfo(): OS
    fun getDisplayInfo(): Display
    fun getCpuInfo(): CPU
    suspend fun getGpuInfo(): GPU
    fun getMemoryInfo(): Flow<Pair<RAM, ZRAM>>
}
