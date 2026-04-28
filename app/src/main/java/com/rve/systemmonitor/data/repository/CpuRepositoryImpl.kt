@file:OptIn(ExperimentalCoroutinesApi::class)

package com.rve.systemmonitor.data.repository

import android.util.Log
import com.rve.systemmonitor.BuildConfig
import com.rve.systemmonitor.domain.model.CPU
import com.rve.systemmonitor.domain.model.CoreDetail
import com.rve.systemmonitor.domain.repository.CpuRepository
import com.rve.systemmonitor.domain.repository.SettingsRepository
import com.rve.systemmonitor.utils.CpuUtils
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
class CpuRepositoryImpl @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : CpuRepository {
    private val TAG = "CpuRepository"

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
                val coreDetails = mutableListOf<CoreDetail>()

                for (i in 0 until cores) {
                    coreDetails.add(
                        CoreDetail(
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
        }.flowOn(Dispatchers.IO)
    }
}
