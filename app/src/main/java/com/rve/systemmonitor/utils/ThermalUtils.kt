package com.rve.systemmonitor.utils

import android.util.Log
import com.rve.systemmonitor.BuildConfig
import com.rve.systemmonitor.shizuku.ShizukuManager
import java.util.Locale

object ThermalUtils {
    private const val TAG = "ThermalUtils"
    private const val CACHE_TTL_MS = 5_000L
    private val HAL_TEMPERATURE_LINE =
        Regex("^\\s*Temperature\\{mValue=([-+]?\\d+(?:\\.\\d+)?), mType=\\d+, mName=([A-Za-z0-9_-]+),")
    private val SIMPLE_TEMPERATURE_LINE = Regex("^\\s+([A-Za-z0-9_-]+):\\s*([-+]?\\d+(?:\\.\\d+)?)")

    @Volatile private var cachedTemps: Map<String, Double> = emptyMap()
    @Volatile private var cachedAt: Long = 0L

    suspend fun getCpuTemperature(shizukuManager: ShizukuManager): Double =
        pickCpuTemp(getTemps(shizukuManager))

    private suspend fun getTemps(shizukuManager: ShizukuManager): Map<String, Double> {
        val now = System.currentTimeMillis()
        if (now - cachedAt < CACHE_TTL_MS) return cachedTemps
        val output = shizukuManager.executeCommand("dumpsys thermalservice")
        if (BuildConfig.DEBUG) Log.d(TAG, "dumpsys thermalservice:\n$output")
        cachedTemps = parseHaldTemps(output)
        if (BuildConfig.DEBUG) Log.d(TAG, "parsed temps: $cachedTemps")
        cachedAt = now
        return cachedTemps
    }

    fun parseHaldTemps(output: String): Map<String, Double> {
        val result = HashMap<String, Double>()
        val lines = output.lineSequence()
            .dropWhile { !it.contains("Current temperatures from HAL") }
            .drop(1)
        for (line in lines) {
            val hal = HAL_TEMPERATURE_LINE.find(line)
            val name: String
            val value: Double
            if (hal != null) {
                name = hal.groupValues[2]
                value = hal.groupValues[1].toDouble()
            } else {
                val simple = SIMPLE_TEMPERATURE_LINE.find(line) ?: break
                name = simple.groupValues[1]
                value = simple.groupValues[2].toDouble()
            }
            if (value > 0.0) {
                result[name.lowercase(Locale.US)] = value
            }
        }
        return result
    }

    fun pickCpuTemp(temps: Map<String, Double>): Double {
        val priority = listOf("cpu", "cpu0", "cores", "soc", "mtktscpu", "ap_ntc", "apc")
        for (key in priority) {
            temps[key]?.let { return it }
        }
        return temps.entries.firstOrNull { (key, _) ->
            "cpu" in key && "gpu" !in key && "battery" !in key && "npu" !in key
        }?.value ?: 0.0
    }
}
