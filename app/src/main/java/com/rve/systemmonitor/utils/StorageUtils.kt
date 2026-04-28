package com.rve.systemmonitor.utils

import android.os.Environment
import android.os.StatFs
import com.rve.systemmonitor.domain.model.Storage
import java.io.File
import kotlin.math.round

object StorageUtils {
    fun getStorageData(): Storage {
        return try {
            val dataDir = Environment.getDataDirectory()
            val mountPath = dataDir.path
            val stat = StatFs(mountPath)
            
            val totalBytes = stat.totalBytes
            val availableBytes = stat.availableBytes
            val usedBytes = totalBytes - availableBytes
            
            val totalGb = totalBytes.toDouble() / (1024.0 * 1024.0 * 1024.0)
            val availableGb = availableBytes.toDouble() / (1024.0 * 1024.0 * 1024.0)
            val usedGb = usedBytes.toDouble() / (1024.0 * 1024.0 * 1024.0)
            val usedPercentage = if (totalGb > 0) (usedGb / totalGb) * 100.0 else 0.0

            val fsType = getFsType(mountPath)

            Storage(
                total = round(totalGb * 100) / 100,
                available = round(availableGb * 100) / 100,
                used = round(usedGb * 100) / 100,
                usedPercentage = round(usedPercentage * 100) / 100,
                mountPath = mountPath,
                fileSystemType = fsType
            )
        } catch (e: Exception) {
            Storage()
        }
    }

    private fun getFsType(path: String): String {
        return try {
            File("/proc/mounts").readLines().find { line ->
                val parts = line.split(Regex("\\s+"))
                parts.size >= 3 && parts[1] == path
            }?.split(Regex("\\s+"))?.get(2) ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }
}
