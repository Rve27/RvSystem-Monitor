package com.rve.systemmonitor.utils

import android.app.ActivityManager
import android.content.Context
import android.util.Log
import com.rve.systemmonitor.ui.data.RAM
import com.rve.systemmonitor.ui.data.ZRAM
import java.io.File
import java.util.Locale

object MemoryUtils {
    private const val TAG = "MemoryUtils"
    private const val GB_FACTOR = 1000000000.0

    private fun getSystemMemoryInfo(context: Context): ActivityManager.MemoryInfo {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo
    }

    private fun formatToTwoDecimals(value: Double): Double {
        return String.format(Locale.US, "%.2f", value).toDouble()
    }

    fun getRamData(context: Context): RAM = runCatching {
        val memInfo = getSystemMemoryInfo(context)

        val totalBytes = memInfo.totalMem
        val availableBytes = memInfo.availMem
        val usedBytes = totalBytes - availableBytes

        val totalGB = formatToTwoDecimals(totalBytes / GB_FACTOR)
        val availableGB = formatToTwoDecimals(availableBytes / GB_FACTOR)
        val usedGB = formatToTwoDecimals(usedBytes / GB_FACTOR)

        val percentage = if (totalBytes > 0) {
            formatToTwoDecimals((usedBytes.toDouble() / totalBytes.toDouble()) * 100.0)
        } else {
            0.0
        }

        RAM(
            total = totalGB,
            available = availableGB,
            used = usedGB,
            usedPercentage = percentage,
        )
    }.getOrElse {
        Log.e(TAG, "getRamData: ${it.message}", it)
        RAM()
    }

    fun getZramData(): ZRAM {
        var swapTotal = 0L
        var swapFree = 0L

        runCatching {
            File("/proc/meminfo").useLines { lines ->
                for (line in lines) {
                    if (line.startsWith("SwapTotal:")) {
                        swapTotal = Regex("\\d+").find(line)?.value?.toLong()?.times(1024L) ?: 0L
                    } else if (line.startsWith("SwapFree:")) {
                        swapFree = Regex("\\d+").find(line)?.value?.toLong()?.times(1024L) ?: 0L
                    }
                }
            }
        }.onFailure {
            Log.e(TAG, "getZramData: ${it.message}", it)
        }

        val totalGB = formatToTwoDecimals(swapTotal / GB_FACTOR)
        val freeGB = formatToTwoDecimals(swapFree / GB_FACTOR)
        val usedGB = formatToTwoDecimals((swapTotal - swapFree) / GB_FACTOR)

        val percentage = if (swapTotal > 0) {
            formatToTwoDecimals(((swapTotal - swapFree).toDouble() / swapTotal.toDouble()) * 100.0)
        } else {
            0.0
        }

        return ZRAM(
            isActive = swapTotal > 0L,
            total = totalGB,
            available = freeGB,
            used = usedGB,
            usedPercentage = percentage,
        )
    }
}
