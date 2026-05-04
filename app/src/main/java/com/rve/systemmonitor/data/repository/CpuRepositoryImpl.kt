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
class CpuRepositoryImpl @Inject constructor(private val settingsRepository: SettingsRepository) : CpuRepository {
    private val TAG = "CpuRepository"

    override fun getCpuInfo(): CPU {
        return CPU(
            manufacturer = CpuUtils.getSocManufacturer(),
            model = CpuUtils.getSocModel(),
            cores = CpuUtils.getCoreCount(),
            hardware = CpuUtils.getHardware(),
            board = CpuUtils.getBoard(),
            architecture = CpuUtils.getArchitecture(),
            temperature = CpuUtils.getCpuTemperature(),
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
                val minKhz = CpuUtils.getCoreFrequencyKhz(i, "min_info")
                val maxKhz = CpuUtils.getCoreFrequencyKhz(i, "max_info")
                val governor = CpuUtils.getCoreGovernor(i)
                CoreStaticInfo(
                    minFreq = CpuUtils.formatFrequency(minKhz),
                    maxFreq = CpuUtils.formatFrequency(maxKhz),
                    minFreqKhz = minKhz,
                    maxFreqKhz = maxKhz,
                    governor = governor,
                )
            }

            while (true) {
                if (BuildConfig.DEBUG) Log.d(TAG, "CPU Stream Updated")
                val coreDetails = mutableListOf<CoreDetail>()
                val currentFrequenciesKhz = CpuUtils.getAllCoreFrequenciesKhz()
                val coreTemperatures = CpuUtils.getAllCoreTemperatures()
                val cpuTemperature = CpuUtils.getCpuTemperature()

                for (i in 0 until cores) {
                    val currentKhz = currentFrequenciesKhz.getOrElse(i) { 0L }
                    val currentTemp = coreTemperatures.getOrElse(i) { 0.0 }
                    val static = staticCoreInfo[i]
                    coreDetails.add(
                        CoreDetail(
                            id = i,
                            currentFreq = CpuUtils.formatFrequency(currentKhz),
                            minFreq = static.minFreq,
                            maxFreq = static.maxFreq,
                            currentFreqKhz = currentKhz,
                            minFreqKhz = static.minFreqKhz,
                            maxFreqKhz = static.maxFreqKhz,
                            governor = static.governor,
                            temperature = currentTemp,
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
                        temperature = cpuTemperature,
                        coreDetails = coreDetails,
                    ),
                )
                delay(delayMillis)
            }
        }.onCompletion {
            if (BuildConfig.DEBUG) Log.d(TAG, "CPU Stream Stopped")
        }.flowOn(Dispatchers.IO)
    }

    private data class CoreStaticInfo(
        val minFreq: String,
        val maxFreq: String,
        val minFreqKhz: Long,
        val maxFreqKhz: Long,
        val governor: String,
    )
}
