package com.rve.systemmonitor.domain.repository

import com.rve.systemmonitor.domain.model.CPU
import kotlinx.coroutines.flow.Flow

interface CpuRepository {
    fun getCpuInfo(): CPU
    fun getCpuStream(): Flow<CPU>
}
