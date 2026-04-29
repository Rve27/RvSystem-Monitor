package com.rve.systemmonitor.utils

import android.util.Log
import com.rve.systemmonitor.domain.model.RAM
import com.rve.systemmonitor.domain.model.ZRAM

object MemoryUtils {
    private const val TAG = "MemoryUtils"

    init {
        try {
            System.loadLibrary("rvsystem_monitor")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load Rust library: ${e.message}")
        }
    }

    @JvmStatic
    private external fun getMemoryDataNative(): DoubleArray

    fun getMemoryInfo(): Pair<RAM, ZRAM> = runCatching {
        val data = getMemoryDataNative()

        val ram = RAM(
            total = data[0],
            available = data[1],
            used = data[2],
            usedPercentage = data[3],
            cached = data[4],
            buffers = data[5],
            active = data[6],
            inactive = data[7],
            slab = data[8],
        )

        val zram = ZRAM(
            isActive = data[9] > 0.0,
            total = data[10],
            available = data[11],
            used = data[12],
            usedPercentage = data[13],
        )

        ram to zram
    }.getOrElse {
        Log.e(TAG, "getMemoryInfo: ${it.message}", it)
        RAM() to ZRAM()
    }

    @JvmStatic
    private external fun getRamDataNative(): DoubleArray

    @JvmStatic
    private external fun getZramDataNative(): DoubleArray

    fun getRamData(): RAM = runCatching {
        val data = getRamDataNative()

        RAM(
            total = data[0],
            available = data[1],
            used = data[2],
            usedPercentage = data[3],
            cached = data[4],
            buffers = data[5],
            active = data[6],
            inactive = data[7],
            slab = data[8],
        )
    }.getOrElse {
        Log.e(TAG, "getRamData: ${it.message}", it)
        RAM()
    }

    fun getZramData(): ZRAM = runCatching {
        val data = getZramDataNative()

        ZRAM(
            isActive = data[0] > 0.0,
            total = data[1],
            available = data[2],
            used = data[3],
            usedPercentage = data[4],
        )
    }.getOrElse {
        Log.e(TAG, "getZramData: ${it.message}", it)
        ZRAM()
    }
}
