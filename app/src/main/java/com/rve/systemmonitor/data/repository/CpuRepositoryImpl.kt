package com.rve.systemmonitor.data.repository

import com.rve.systemmonitor.data.di.ApplicationScope
import com.rve.systemmonitor.domain.model.CPU
import com.rve.systemmonitor.domain.model.CoreDetail
import com.rve.systemmonitor.domain.repository.CpuRepository
import com.rve.systemmonitor.domain.repository.SettingsRepository
import com.rve.systemmonitor.privileged.PrivilegedAccess
import com.rve.systemmonitor.utils.CpuUtils
import com.rve.systemmonitor.utils.FlowUtils
import com.rve.systemmonitor.utils.ThermalUtils
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.shareIn

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class CpuRepositoryImpl @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val privilegedAccess: PrivilegedAccess,
    @param:ApplicationScope private val externalScope: CoroutineScope,
) : CpuRepository {
    private val TAG = "CpuRepository"

    private val sharedCpuStream by lazy {
        kotlinx.coroutines.flow.combine(
            settingsRepository.cpuRefreshDelay,
            privilegedAccess.isReady,
        ) { delayMillis, ready ->
            delayMillis to ready
        }.flatMapLatest { (delayMillis, privilegedReady) ->
            val manufacturer = CpuUtils.getSocManufacturer()
            val model = CpuUtils.getSocModel()
            val cores = CpuUtils.getCoreCount()
            val hardware = CpuUtils.getHardware()
            val board = CpuUtils.getBoard()
            val architecture = CpuUtils.getArchitecture()
            val abi = CpuUtils.getAbi()

            val staticInfo = CpuUtils.getStaticCoreInfo()
            val governors = CpuUtils.getAllCoreGovernors()

            val staticCoreInfo = (0 until cores).map { i ->
                CoreStaticInfo(
                    minFreqKhz = staticInfo.getOrElse(i * 2) { 0L },
                    maxFreqKhz = staticInfo.getOrElse(i * 2 + 1) { 0L },
                    governor = governors.getOrElse(i) { "N/A" },
                )
            }

            FlowUtils.pollingFlow(TAG, delayMillis) {
                val dynamicData = CpuUtils.getCpuDynamicData()
                val nativeTemp = dynamicData.getOrElse(0) { 0.0 }
                val fallbackTemp = if (nativeTemp <= 0.0 && privilegedReady) {
                    ThermalUtils.getCpuTemperature(privilegedAccess)
                } else {
                    0.0
                }
                val cpuTemperature = if (nativeTemp > 0.0) nativeTemp else fallbackTemp
                val coreDetails = ArrayList<CoreDetail>(cores)

                var isPrivilegedSuccess = false
                val cpuLoads = if (privilegedReady) {
                    val procStat = privilegedAccess.executeCommand("cat /proc/stat")
                    if (procStat.isNotEmpty() && !procStat.startsWith("ERROR:")) {
                        val loads = CpuUtils.calculateCpuLoad(procStat)
                        if (loads.isNotEmpty()) {
                            isPrivilegedSuccess = true
                            loads
                        } else {
                            DoubleArray(0)
                        }
                    } else {
                        DoubleArray(0)
                    }
                } else {
                    DoubleArray(0)
                }

                val totalLoad = cpuLoads.getOrElse(0) { if (isPrivilegedSuccess) -1.0 else 0.0 }

                for (i in 0 until cores) {
                    val currentKhz = dynamicData.getOrElse(1 + i * 2) { 0.0 }.toLong()
                    val nativeCoreTemp = dynamicData.getOrElse(2 + i * 2) { 0.0 }
                    val currentTemp = nativeCoreTemp
                    val currentLoad = cpuLoads.getOrElse(1 + i) { if (isPrivilegedSuccess) -1.0 else 0.0 }
                    val static = staticCoreInfo[i]
                    coreDetails.add(
                        CoreDetail(
                            id = i,
                            currentFreqKhz = currentKhz,
                            minFreqKhz = static.minFreqKhz,
                            maxFreqKhz = static.maxFreqKhz,
                            governor = static.governor,
                            temperature = currentTemp,
                            load = currentLoad,
                        ),
                    )
                }

                CPU(
                    manufacturer = manufacturer,
                    model = model,
                    cores = cores,
                    hardware = hardware,
                    board = board,
                    architecture = architecture,
                    abi = abi,
                    temperature = cpuTemperature,
                    load = totalLoad,
                    isLoadAvailable = isPrivilegedSuccess,
                    coreDetails = coreDetails.toImmutableList(),
                )
            }
        }.shareIn(
            scope = externalScope,
            started = SharingStarted.WhileSubscribed(5000),
            replay = 1,
        )
    }

    override fun getCpuInfo(): CPU {
        return CPU(
            manufacturer = CpuUtils.getSocManufacturer(),
            model = CpuUtils.getSocModel(),
            cores = CpuUtils.getCoreCount(),
            hardware = CpuUtils.getHardware(),
            board = CpuUtils.getBoard(),
            architecture = CpuUtils.getArchitecture(),
            abi = CpuUtils.getAbi(),
            temperature = CpuUtils.getCpuTemperature(),
        )
    }

    override fun getCpuStream(): Flow<CPU> = sharedCpuStream

    private data class CoreStaticInfo(val minFreqKhz: Long, val maxFreqKhz: Long, val governor: String)
}
