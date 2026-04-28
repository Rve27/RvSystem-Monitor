@file:OptIn(ExperimentalCoroutinesApi::class)

package com.rve.systemmonitor.data.repository

import android.util.Log
import com.rve.systemmonitor.BuildConfig
import com.rve.systemmonitor.domain.model.RAM
import com.rve.systemmonitor.domain.model.ZRAM
import com.rve.systemmonitor.domain.repository.MemoryRepository
import com.rve.systemmonitor.domain.repository.SettingsRepository
import com.rve.systemmonitor.utils.MemoryUtils
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion

@Singleton
class MemoryRepositoryImpl @Inject constructor(private val settingsRepository: SettingsRepository) : MemoryRepository {
    private val TAG = "MemoryRepository"

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
        }.flowOn(Dispatchers.IO)
    }
}
