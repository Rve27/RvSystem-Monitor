package com.rve.systemmonitor.utils

import android.os.Build
import android.util.Log

object CpuUtils {
    private const val TAG = "CpuUtils"

    init {
        try {
            System.loadLibrary("rvsystem_monitor")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load Rust library: ${e.message}", e)
        }
    }

    @JvmStatic
    private external fun getAllCoreFrequenciesNative(): Array<String>

    fun getAllCoreFrequencies(): Array<String> = runCatching {
        getAllCoreFrequenciesNative()
    }.getOrElse {
        Log.e(TAG, "getAllCoreFrequencies: ${it.message}", it)
        emptyArray()
    }

    @JvmStatic
    private external fun getCoreCountNative(): Int

    @JvmStatic
    private external fun getCoreFrequencyNative(coreId: Int, type: String): String

    @JvmStatic
    private external fun getCoreGovernorNative(coreId: Int): String

    fun getSocManufacturer(): String = runCatching {
        val manufacturer = Build.SOC_MANUFACTURER
        if (manufacturer != Build.UNKNOWN) {
            manufacturer.replaceFirstChar { it.uppercase() }
        } else {
            "Unknown"
        }
    }.getOrElse {
        Log.e(TAG, "getSocManufacturer: ${it.message}", it)
        "Unknown"
    }

    fun getSocModel(): String = runCatching {
        val model = Build.SOC_MODEL
        if (model != Build.UNKNOWN) {
            model.uppercase()
        } else {
            "Unknown"
        }
    }.getOrElse {
        Log.e(TAG, "getSocModel: ${it.message}", it)
        "Unknown"
    }

    fun getHardware(): String = runCatching { Build.HARDWARE }.getOrElse { "Unknown" }

    fun getBoard(): String = runCatching { Build.BOARD }.getOrElse { "Unknown" }

    fun getArchitecture(): String = runCatching {
        Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown"
    }.getOrElse { "Unknown" }

    fun getCoreCount(): Int = runCatching {
        getCoreCountNative()
    }.getOrElse {
        Log.e(TAG, "getCoreCount: ${it.message}", it)
        0
    }

    fun getCoreFrequency(coreId: Int, type: String): String = runCatching {
        getCoreFrequencyNative(coreId, type)
    }.getOrElse { "N/A" }

    fun getCoreGovernor(coreId: Int): String = runCatching {
        getCoreGovernorNative(coreId)
    }.getOrElse { "N/A" }
}
