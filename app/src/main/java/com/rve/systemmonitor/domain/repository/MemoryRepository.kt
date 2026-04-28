package com.rve.systemmonitor.domain.repository

import com.rve.systemmonitor.domain.model.RAM
import com.rve.systemmonitor.domain.model.ZRAM
import kotlinx.coroutines.flow.Flow

interface MemoryRepository {
    fun getMemoryInfo(): Flow<Pair<RAM, ZRAM>>
}
