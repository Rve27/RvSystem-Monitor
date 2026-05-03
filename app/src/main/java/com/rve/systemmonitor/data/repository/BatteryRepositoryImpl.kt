@file:OptIn(ExperimentalCoroutinesApi::class)

package com.rve.systemmonitor.data.repository

import android.app.Application
import android.os.SystemClock
import com.rve.systemmonitor.domain.model.Battery
import com.rve.systemmonitor.domain.repository.BatteryRepository
import com.rve.systemmonitor.domain.repository.SettingsRepository
import com.rve.systemmonitor.utils.BatteryUtils
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

@Singleton
class BatteryRepositoryImpl @Inject constructor(private val application: Application, private val settingsRepository: SettingsRepository) :
    BatteryRepository {

    private val staticBatteryInfo by lazy {
        val intent = BatteryUtils.getBatteryIntent(application)
        val deepSleep = SystemClock.elapsedRealtime() - SystemClock.uptimeMillis()
        if (intent != null) {
            val design = BatteryUtils.getCapacity(application)
            val actual = BatteryUtils.getActualCapacity(application)
            BatteryInfo(
                health = BatteryUtils.getHealth(intent),
                technology = BatteryUtils.getTechnology(intent),
                designCapacity = design,
                maxCapacity = actual,
                healthPercentage = BatteryUtils.getHealthPercentage(actual, design),
                deepSleep = deepSleep,
            )
        } else {
            BatteryInfo("Unknown", "Unknown", -1.0, -1.0, -1, deepSleep)
        }
    }

    private data class BatteryInfo(
        val health: String,
        val technology: String,
        val designCapacity: Double,
        val maxCapacity: Double,
        val healthPercentage: Int,
        val deepSleep: Long,
    )

    override fun getBatteryInfo(): Battery {
        val intent = BatteryUtils.getBatteryIntent(application)
        return if (intent != null) {
            val current = BatteryUtils.getCurrent(application)
            val voltage = BatteryUtils.getVoltage(intent)
            val level = BatteryUtils.getLevel(intent)

            Battery(
                level = level,
                health = staticBatteryInfo.health,
                status = BatteryUtils.getStatus(intent),
                technology = staticBatteryInfo.technology,
                voltage = voltage,
                temperature = BatteryUtils.getTemperature(intent),
                capacity = staticBatteryInfo.designCapacity,
                maxCapacity = staticBatteryInfo.maxCapacity,
                healthPercentage = staticBatteryInfo.healthPercentage,
                cycleCount = BatteryUtils.getCycleCount(intent),
                uptime = SystemClock.elapsedRealtime(),
                deepSleep = staticBatteryInfo.deepSleep,
                current = current,
                wattage = calculateWattage(current, voltage),
                powerSource = BatteryUtils.getPowerSource(intent),
                remainingCapacity = calculateRemainingCapacity(level, staticBatteryInfo.maxCapacity),
            )
        } else {
            Battery()
        }
    }

    override fun getBatteryStream(): Flow<Battery> {
        val broadcastFlow = BatteryUtils.getBatteryFlow(application)

        val pollingFlow = settingsRepository.batteryRefreshDelay.flatMapLatest { delayMillis ->
            flow {
                delay(400) // Initial delay to avoid startup peak
                while (true) {
                    emit(BatteryUtils.getCurrent(application))
                    delay(delayMillis)
                }
            }
        }

        return combine(broadcastFlow, pollingFlow) { intent, currentNow ->
            val voltage = BatteryUtils.getVoltage(intent)
            val level = BatteryUtils.getLevel(intent)

            Battery(
                level = level,
                health = staticBatteryInfo.health,
                status = BatteryUtils.getStatus(intent),
                technology = staticBatteryInfo.technology,
                voltage = voltage,
                temperature = BatteryUtils.getTemperature(intent),
                capacity = staticBatteryInfo.designCapacity,
                maxCapacity = staticBatteryInfo.maxCapacity,
                healthPercentage = staticBatteryInfo.healthPercentage,
                cycleCount = BatteryUtils.getCycleCount(intent),
                uptime = SystemClock.elapsedRealtime(),
                deepSleep = staticBatteryInfo.deepSleep,
                current = currentNow,
                wattage = calculateWattage(currentNow, voltage),
                powerSource = BatteryUtils.getPowerSource(intent),
                remainingCapacity = calculateRemainingCapacity(level, staticBatteryInfo.maxCapacity),
            )
        }.flowOn(Dispatchers.IO)
    }

    private fun calculateWattage(currentMA: Int, voltageMV: Int): Double {
        // P (W) = I (A) * V (V)
        // mA -> A: currentMA / 1000.0
        // mV -> V: voltageMV / 1000.0
        return (abs(currentMA) / 1000.0) * (voltageMV / 1000.0)
    }

    private fun calculateRemainingCapacity(level: Int, maxCapacity: Double): Double {
        if (level < 0 || maxCapacity <= 0) return 0.0
        return (level / 100.0) * maxCapacity
    }
}
