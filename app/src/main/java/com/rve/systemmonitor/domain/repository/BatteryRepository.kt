package com.rve.systemmonitor.domain.repository

import com.rve.systemmonitor.domain.model.Battery
import kotlinx.coroutines.flow.Flow

interface BatteryRepository {
    fun getBatteryInfo(): Battery
    fun getBatteryStream(): Flow<Battery>
}
