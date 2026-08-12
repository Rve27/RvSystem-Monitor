package com.rve.systemmonitor.utils

import android.os.Build
import android.util.Log
import java.util.Locale

object CpuUtils {
    private const val TAG = "CpuUtils"

    init {
        NativeLoader.load()
    }

    @JvmStatic
    private external fun getAllCoreFrequenciesNative(): LongArray

    fun getAllCoreFrequenciesKhz(): LongArray = runCatching {
        getAllCoreFrequenciesNative()
    }.getOrElse {
        Log.e(TAG, "getAllCoreFrequenciesKhz: ${it.message}", it)
        LongArray(0)
    }

    fun getAllCoreFrequencies(): Array<String> = runCatching {
        val frequencies = getAllCoreFrequenciesKhz()
        frequencies.map { formatFrequency(it) }.toTypedArray()
    }.getOrElse {
        Log.e(TAG, "getAllCoreFrequencies: ${it.message}", it)
        emptyArray()
    }

    @JvmStatic
    private external fun getStaticCoreInfoNative(): LongArray

    @JvmStatic
    private external fun getAllCoreGovernorsNative(): Array<String>

    @JvmStatic
    private external fun getCoreCountNative(): Int

    @JvmStatic
    private external fun getCoreFrequencyNative(coreId: Int, type: String): Long

    @JvmStatic
    private external fun getCoreGovernorNative(core_id: Int): String

    @JvmStatic
    private external fun getCpuTemperatureNative(): Double

    @JvmStatic
    private external fun getAllCoreTemperaturesNative(): DoubleArray

    @JvmStatic
    private external fun getCpuDynamicDataNative(): DoubleArray

    @JvmStatic
    private external fun calculateCpuLoadNative(procStat: String): DoubleArray

    fun getCpuTemperature(): Double = runCatching {
        getCpuTemperatureNative()
    }.getOrElse { 0.0 }

    fun getAllCoreTemperatures(): DoubleArray = runCatching {
        getAllCoreTemperaturesNative()
    }.getOrElse { DoubleArray(0) }

    fun getCpuDynamicData(): DoubleArray = runCatching {
        getCpuDynamicDataNative()
    }.getOrElse { DoubleArray(0) }

    fun calculateCpuLoad(procStat: String): DoubleArray = runCatching {
        calculateCpuLoadNative(procStat)
    }.getOrElse { DoubleArray(0) }

    fun formatFrequency(freqKhz: Long): String {
        return String.format(Locale.US, "%.2f GHz", freqKhz / 1_000_000.0)
    }

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
        val cpuInfoFile = java.io.File("/proc/cpuinfo")
        if (cpuInfoFile.exists() && cpuInfoFile.canRead()) {
            val cpuInfo = cpuInfoFile.readText()
            val featuresLine = cpuInfo.lines().find { it.startsWith("Features") }

            // Check for ARMv9 specific features (SVE/SVE2 are mandatory in ARMv9-A)
            if (featuresLine != null && (featuresLine.contains("sve ") || featuresLine.contains("sve2 "))) {
                return "ARMv9-A"
            }

            // Fallback to CPU part number mapping for known cores
            val cpuPartLine = cpuInfo.lines().findLast { it.startsWith("CPU part") }
            if (cpuPartLine != null) {
                val part = cpuPartLine.split(":").lastOrNull()?.trim()?.lowercase()
                val cpuPartToArch = mapOf(
                    "0xd03" to "ARMv8-A", "0xd04" to "ARMv8-A", "0xd05" to "ARMv8.2-A",
                    "0xd07" to "ARMv8-A", "0xd08" to "ARMv8-A", "0xd09" to "ARMv8-A",
                    "0xd0a" to "ARMv8.2-A", "0xd0b" to "ARMv8.2-A", "0xd0c" to "ARMv8.2-A",
                    "0xd0d" to "ARMv8.2-A", "0xd41" to "ARMv8.2-A", "0xd42" to "ARMv8.2-A",
                    "0xd44" to "ARMv8.2-A", "0xd46" to "ARMv9-A", "0xd47" to "ARMv9-A",
                    "0xd48" to "ARMv9-A", "0xd49" to "ARMv9-A", "0xd4d" to "ARMv9-A",
                    "0xd4e" to "ARMv9-A", "0xd80" to "ARMv9-A", "0xd81" to "ARMv9-A",
                    "0xd82" to "ARMv9-A", "0xd92" to "ARMv9-A", "0xd93" to "ARMv9-A",
                    "0x804" to "ARMv8.2-A", "0x805" to "ARMv8.2-A",
                )
                if (part != null && cpuPartToArch.containsKey(part)) {
                    return cpuPartToArch[part]!!
                }
            }

            // Fallback to CPU architecture line
            val archLine = cpuInfo.lines().findLast { it.startsWith("CPU architecture") }
            if (archLine != null) {
                val archStr = archLine.split(":").lastOrNull()?.trim()
                if (archStr == "8" || archStr == "AArch64") {
                    return "ARMv8-A"
                } else if (archStr == "7" || archStr == "7I") {
                    return "ARMv7-A"
                }
            }
        }
        "Unknown"
    }.getOrElse { "Unknown" }

    fun getAbi(): String = runCatching {
        val abi = Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown"
        val bitness = if (abi.contains("64")) "64-bit" else "32-bit"
        "$abi ($bitness)"
    }.getOrElse { "Unknown" }

    fun getCoreCount(): Int = runCatching {
        getCoreCountNative()
    }.getOrElse {
        Log.e(TAG, "getCoreCount: ${it.message}", it)
        0
    }

    fun getStaticCoreInfo(): LongArray = runCatching {
        getStaticCoreInfoNative()
    }.getOrElse { LongArray(0) }

    fun getAllCoreGovernors(): Array<String> = runCatching {
        getAllCoreGovernorsNative()
    }.getOrElse { emptyArray() }

    fun getCoreFrequencyKhz(coreId: Int, type: String): Long = runCatching {
        getCoreFrequencyNative(coreId, type)
    }.getOrElse { 0L }

    fun getCoreFrequency(coreId: Int, type: String): String = runCatching {
        formatFrequency(getCoreFrequencyKhz(coreId, type))
    }.getOrElse { "N/A" }

    fun getCoreGovernor(coreId: Int): String = runCatching {
        getCoreGovernorNative(coreId)
    }.getOrElse { "N/A" }
}
