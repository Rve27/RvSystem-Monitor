package com.rve.systemmonitor.utils

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import com.rve.systemmonitor.BuildConfig
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

object BatteryUtils {
    fun getBatteryIntent(context: Context): Intent? {
        return context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    fun getBatteryFlow(context: Context): Flow<Intent> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                trySend(intent)
            }
        }
        val sticky = context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        if (sticky != null) trySend(sticky)
        awaitClose { context.unregisterReceiver(receiver) }
    }

    fun getLevel(intent: Intent): Int {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        return if (level != -1 && scale != -1) (level * 100 / scale.toFloat()).toInt() else -1
    }

    fun getHealth(intent: Intent): String {
        return when (intent.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Unspecified Failure"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            else -> "Unknown"
        }
    }

    fun getStatus(intent: Intent): String {
        return when (intent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN)) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            BatteryManager.BATTERY_STATUS_FULL -> "Full"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
            else -> "Unknown"
        }
    }

    fun getPowerSource(intent: Intent): String {
        return when (intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)) {
            BatteryManager.BATTERY_PLUGGED_AC -> "AC Charger"
            BatteryManager.BATTERY_PLUGGED_USB -> "USB Port"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
            0 -> "Battery"
            else -> "Unknown"
        }
    }

    fun getTechnology(intent: Intent): String {
        return intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown"
    }

    fun getVoltage(intent: Intent): Int {
        return intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
    }

    fun getTemperature(intent: Intent): Float {
        return intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) / 10f
    }

    fun getCycleCount(intent: Intent): Int {
        return intent.getIntExtra(BatteryManager.EXTRA_CYCLE_COUNT, -1)
    }

    @SuppressLint("PrivateApi")
    fun getCapacity(context: Context): Double {
        val powerProfileClass = "com.android.internal.os.PowerProfile"
        return try {
            val mPowerProfile = Class.forName(powerProfileClass)
                .getConstructor(Context::class.java)
                .newInstance(context)
            Class.forName(powerProfileClass)
                .getMethod("getBatteryCapacity")
                .invoke(mPowerProfile) as Double
        } catch (e: Exception) {
            -1.0
        }
    }

    fun getActualCapacity(context: Context): Double {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

        // On Android 14+ we can try to get it from BatteryManager (System API unfortunately, but let's see)
        // For 3rd party apps, we usually use the Charge Counter / Percentage estimation
        val chargeCounter = try {
            batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
        } catch (e: Exception) {
            -1L
        }
        val capacityPercent = try {
            batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        } catch (e: Exception) {
            -1
        }

        if (chargeCounter > 0 && capacityPercent > 0) {
            val capacity = (chargeCounter / capacityPercent.toDouble()) * 100.0 / 1000.0
            if (BuildConfig.DEBUG) {
                Log.d(
                    "BatteryUtils",
                    "Maximum capacity obtained from BATTERY_PROPERTY_CHARGE_COUNTER and " +
                        "BATTERY_PROPERTY_CAPACITY: $capacity mAh",
                )
            }
            return capacity
        }

        if (BuildConfig.DEBUG) {
            Log.d("BatteryUtils", "Maximum capacity could not be obtained from BatteryManager properties")
        }
        return -1.0
    }

    fun getHealthPercentage(actualCapacity: Double, designCapacity: Double): Int {
        if (actualCapacity <= 0 || designCapacity <= 0) return -1
        return ((actualCapacity / designCapacity) * 100).toInt().coerceIn(0, 100)
    }

    fun getCurrent(context: Context): Int {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        // CURRENT_NOW is in microamperes
        val currentMicroAmps = batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        return if (currentMicroAmps != Long.MIN_VALUE) (currentMicroAmps / 1000).toInt() else 0
    }
}
