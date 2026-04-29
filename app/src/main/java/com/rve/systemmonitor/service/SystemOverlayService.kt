package com.rve.systemmonitor.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Choreographer
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.rve.systemmonitor.R
import com.rve.systemmonitor.utils.MemoryUtils
import java.util.Locale

class SystemOverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var metricsTextView: TextView? = null

    private var showFps = true
    private var showRam = true

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
                        metrics.add(
                            String.format(
                                Locale.US,
                                "RAM: %.1f / %.1f GB (%.0f%%)",
                                ram.used,
                                ram.total,
                                ram.usedPercentage,
                            ),
                        )
                    }

                    metricsTextView?.text = if (metrics.isEmpty()) "No metrics" else metrics.joinToString(" | ")
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
        return START_NOT_STICKY
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
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 100
        }

        val textView = TextView(this).apply {
            text = "Loading metrics..."
            setTextColor(Color.GREEN)
            setBackgroundColor(Color.argb(128, 0, 0, 0))
            setPadding(16, 8, 16, 8)
            textSize = 14f
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "System Overlay Service",
                NotificationManager.IMPORTANCE_LOW,
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

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
