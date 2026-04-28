@file:OptIn(ExperimentalCoroutinesApi::class)

package com.rve.systemmonitor.data.repository

import android.app.Application
import com.rve.systemmonitor.domain.model.Battery
import com.rve.systemmonitor.domain.repository.BatteryRepository
import com.rve.systemmonitor.domain.repository.SettingsRepository
import com.rve.systemmonitor.utils.BatteryUtils
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

@Singleton
class BatteryRepositoryImpl @Inject constructor(
    private val application: Application,
    private val settingsRepository: SettingsRepository,
) : BatteryRepository {

    private val staticBatteryInfo by lazy {
        val intent = BatteryUtils.getBatteryIntent(application)
        if (intent != null) {
            val design = BatteryUtils.getCapacity(application)
            val actual = BatteryUtils.getActualCapacity(application)
            BatteryInfo(
                health = BatteryUtils.getHealth(intent),
                technology = BatteryUtils.getTechnology(intent),
                designCapacity = design,
                maxCapacity = actual,
                healthPercentage = BatteryUtils.getHealthPercentage(actual, design)
            )
        } else {
            BatteryInfo("Unknown", "Unknown", -1.0, -1.0, -1)
        }
    }

    private data class BatteryInfo(
        val health: String,
        val technology: String,
        val designCapacity: Double,
        val maxCapacity: Double,
        val healthPercentage: Int
    )

    override fun getBatteryInfo(): Battery {
        val intent = BatteryUtils.getBatteryIntent(application)
        return if (intent != null) {
            Battery(
                level = BatteryUtils.getLevel(intent),
                health = staticBatteryInfo.health,
                status = BatteryUtils.getStatus(intent),
                technology = staticBatteryInfo.technology,
                voltage = BatteryUtils.getVoltage(intent),
                temperature = BatteryUtils.getTemperature(intent),
                capacity = staticBatteryInfo.designCapacity,
                maxCapacity = staticBatteryInfo.maxCapacity,
                healthPercentage = staticBatteryInfo.healthPercentage,
                current = BatteryUtils.getCurrent(application),
            )
        } else {
            Battery()
        }
    }

    override fun getBatteryStream(): Flow<Battery> {
        val broadcastFlow = BatteryUtils.getBatteryFlow(application)

        val pollingFlow = settingsRepository.batteryRefreshDelay.flatMapLatest { delayMillis ->
            flow {
                while (true) {
                    emit(BatteryUtils.getCurrent(application))
                    delay(delayMillis)
                }
            }
        }

        return combine(broadcastFlow, pollingFlow) { intent, currentNow ->
            Battery(
                level = BatteryUtils.getLevel(intent),
                health = staticBatteryInfo.health,
                status = BatteryUtils.getStatus(intent),
                technology = staticBatteryInfo.technology,
                voltage = BatteryUtils.getVoltage(intent),
                temperature = BatteryUtils.getTemperature(intent),
                capacity = staticBatteryInfo.designCapacity,
                maxCapacity = staticBatteryInfo.maxCapacity,
                healthPercentage = staticBatteryInfo.healthPercentage,
                current = currentNow,
            )
        }.flowOn(Dispatchers.IO)
    }
}
