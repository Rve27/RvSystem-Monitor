package com.rve.systemmonitor.utils

import android.content.Context
import android.hardware.display.DisplayManager
import android.util.Log
import android.view.Display
import com.rve.systemmonitor.R
import java.util.Locale
import kotlin.math.pow
import kotlin.math.sqrt

object DisplayUtils {
    private const val TAG = "DisplayUtils"

    private fun getDisplay(context: Context): Display? {
        val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        return displayManager.getDisplay(Display.DEFAULT_DISPLAY)
    }

    fun getResolution(context: Context): String = runCatching {
        val metrics = context.resources.displayMetrics
        "${metrics.widthPixels}x${metrics.heightPixels}"
    }.getOrElse {
        Log.e(TAG, "getResolution: ${it.message}", it)
        "unknown"
    }

    fun getSupportedRefreshRates(context: Context): List<Int> = runCatching {
        getDisplay(context)?.supportedModes?.map { Math.round(it.refreshRate) }?.distinct()?.sorted() ?: emptyList()
    }.getOrElse {
        Log.e(TAG, "getSupportedRefreshRates: ${it.message}", it)
        emptyList()
    }

    fun getDensityDpi(context: Context): Int = runCatching {
        context.resources.displayMetrics.densityDpi
    }.getOrElse {
        Log.e(TAG, "getDensityDpi: ${it.message}", it)
        0
    }

    fun getScreenSizeInches(context: Context): Double = runCatching {
        val metrics = context.resources.displayMetrics
        val widthInches = metrics.widthPixels.toDouble() / metrics.xdpi
        val heightInches = metrics.heightPixels.toDouble() / metrics.ydpi

        val diagonal = sqrt(widthInches.pow(2.0) + heightInches.pow(2.0))

        "%.2f".format(Locale.US, diagonal).toDouble()
    }.getOrElse {
        Log.e(TAG, "getScreenSizeInches: ${it.message}", it)
        0.0
    }

    fun getHdrCapabilities(context: Context): Pair<Boolean, List<String>> {
        return try {
            val display = getDisplay(context) ?: return false to emptyList()
            val supportedTypes = display.mode.supportedHdrTypes

            val types = mutableListOf<String>()
            for (type in supportedTypes) {
                when (type) {
                    Display.HdrCapabilities.HDR_TYPE_DOLBY_VISION -> types.add(context.getString(R.string.hdr_type_dolby_vision))
                    Display.HdrCapabilities.HDR_TYPE_HDR10 -> types.add(context.getString(R.string.hdr_type_hdr10))
                    Display.HdrCapabilities.HDR_TYPE_HLG -> types.add(context.getString(R.string.hdr_type_hlg))
                    Display.HdrCapabilities.HDR_TYPE_HDR10_PLUS -> types.add(context.getString(R.string.hdr_type_hdr10_plus))
                }
            }

            (types.isNotEmpty()) to types
        } catch (e: Exception) {
            Log.e(TAG, "getHdrCapabilities: ${e.message}", e)
            false to emptyList()
        }
    }
}
