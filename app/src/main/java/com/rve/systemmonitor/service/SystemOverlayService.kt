package com.rve.systemmonitor.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.IBinder
import android.view.Choreographer
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.rve.systemmonitor.R
import com.rve.systemmonitor.utils.BatteryUtils
import com.rve.systemmonitor.utils.CpuUtils
import com.rve.systemmonitor.utils.MemoryUtils
import java.util.Locale

class SystemOverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var metricsTextView: TextView? = null

    private var showFps = true
    private var showRam = true
    private var showRamPercentage = false
    private var showRamGb = false
    private var showBatteryTemp = false
    private var showCpuTemp = false
    private var overlayTextSize = 14f
    private var overlayBgOpacity = 0.5f
    private var overlayPadding = 16
    private var overlayTextColor = Color.GREEN
    private var isVerticalLayout = false
    private var overlayCornerRadius = 8

    private val choreographer = Choreographer.getInstance()
    private var lastFrameTimeNanos: Long = 0
    private var frameCount = 0
    private var lastFpsUpdateTime: Long = 0
    private var updateDelayNanos: Long = 1_000_000_000L // Default 1s

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (lastFrameTimeNanos != 0L) {
                frameCount++
                val elapsedNanos = frameTimeNanos - lastFpsUpdateTime
                if (elapsedNanos >= updateDelayNanos) {
                    val metrics = mutableListOf<String>()

                    if (showFps) {
                        val fps = (frameCount * 1_000_000_000L) / elapsedNanos
                        metrics.add(String.format(Locale.US, "FPS: %d", fps))
                    }

                    if (showRam) {
                        val ram = MemoryUtils.getRamData()
                        val ramText = when {
                            showRamGb && showRamPercentage -> {
                                String.format(Locale.US, "%.1f/%.1f GB (%.0f%%)", ram.used, ram.total, ram.usedPercentage)
                            }

                            showRamGb -> {
                                String.format(Locale.US, "%.1f/%.1f GB", ram.used, ram.total)
                            }

                            showRamPercentage -> {
                                String.format(Locale.US, "%.0f%%", ram.usedPercentage)
                            }

                            else -> {
                                // Default fallback if both false but showRam is true
                                String.format(Locale.US, "%.1f/%.1f GB (%.0f%%)", ram.used, ram.total, ram.usedPercentage)
                            }
                        }
                        metrics.add("RAM: $ramText")
                    }

                    if (showBatteryTemp) {
                        val batteryIntent = BatteryUtils.getBatteryIntent(this@SystemOverlayService)
                        if (batteryIntent != null) {
                            val temp = BatteryUtils.getTemperature(batteryIntent)
                            metrics.add(String.format(Locale.US, "BATT: %.1f°C", temp))
                        }
                    }

                    if (showCpuTemp) {
                        val cpuData = CpuUtils.getCpuDynamicData()
                        if (cpuData.isNotEmpty()) {
                            val temp = cpuData[0] // overall_temp is at index 0
                            metrics.add(String.format(Locale.US, "CPU: %.1f°C", temp))
                        }
                    }

                    val separator = if (isVerticalLayout) "\n" else " | "
                    metricsTextView?.text = if (metrics.isEmpty()) "No metrics" else metrics.joinToString(separator)
                    frameCount = 0
                    lastFpsUpdateTime = frameTimeNanos
                }
            } else {
                lastFpsUpdateTime = frameTimeNanos
            }
            lastFrameTimeNanos = frameTimeNanos
            choreographer.postFrameCallback(this)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val delayMillis = intent?.getLongExtra("update_delay", 1000L) ?: 1000L
        updateDelayNanos = delayMillis * 1_000_000L
        showFps = intent?.getBooleanExtra("show_fps", true) ?: true
        showRam = intent?.getBooleanExtra("show_ram", true) ?: true
        showRamPercentage = intent?.getBooleanExtra("show_ram_percentage", false) ?: false
        showRamGb = intent?.getBooleanExtra("show_ram_gb", false) ?: false
        showBatteryTemp = intent?.getBooleanExtra("show_battery_temp", false) ?: false
        showCpuTemp = intent?.getBooleanExtra("show_cpu_temp", false) ?: false

        overlayTextSize = intent?.getFloatExtra("text_size", 14f) ?: 14f
        overlayBgOpacity = intent?.getFloatExtra("bg_opacity", 0.5f) ?: 0.5f
        overlayPadding = intent?.getIntExtra("padding", 16) ?: 16
        overlayTextColor = intent?.getIntExtra("text_color", Color.GREEN) ?: Color.GREEN
        isVerticalLayout = intent?.getBooleanExtra("is_vertical", false) ?: false
        overlayCornerRadius = intent?.getIntExtra("corner_radius", 8) ?: 8

        applySettings()

        return START_NOT_STICKY
    }

    private fun applySettings() {
        metricsTextView?.apply {
            textSize = overlayTextSize
            setTextColor(overlayTextColor)
            val alphaInt = (overlayBgOpacity * 255).toInt()

            val shape = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(Color.argb(alphaInt, 0, 0, 0))
                cornerRadius = overlayCornerRadius.toFloat()
            }
            background = shape

            setPadding(overlayPadding, overlayPadding / 2, overlayPadding, overlayPadding / 2)
        }
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, createNotification())
        showOverlay()
        choreographer.postFrameCallback(frameCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        choreographer.removeFrameCallback(frameCallback)
        if (overlayView != null) {
            windowManager?.removeView(overlayView)
        }
    }

    private fun showOverlay() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 100
        }

        val textView = TextView(this).apply {
            text = "Loading ..."
            textSize = overlayTextSize
            setTextColor(overlayTextColor)

            val alphaInt = (overlayBgOpacity * 255).toInt()
            val shape = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(Color.argb(alphaInt, 0, 0, 0))
                cornerRadius = overlayCornerRadius.toFloat()
            }
            background = shape

            setPadding(overlayPadding, overlayPadding / 2, overlayPadding, overlayPadding / 2)
        }

        textView.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        params.x = initialX + (event.rawX - initialTouchX).toInt()
                        params.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager?.updateViewLayout(v, params)
                        return true
                    }
                }
                return false
            }
        })

        metricsTextView = textView
        overlayView = textView
        windowManager?.addView(overlayView, params)
    }

    private fun createNotification(): Notification {
        val channelId = "system_overlay_channel"
        val channel = NotificationChannel(
            channelId,
            "System Overlay Service",
            NotificationManager.IMPORTANCE_LOW,
        )
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("System Overlay Active")
            .setContentText("Monitoring system performance (FPS & RAM)")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
    }
}
