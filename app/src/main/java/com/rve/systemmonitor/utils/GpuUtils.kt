package com.rve.systemmonitor.utils

import android.app.ActivityManager
import android.content.Context
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.GLES20
import android.util.Log

object GpuUtils {
    private const val TAG = "GpuUtils"

    fun getGpuDetails(): Pair<String, String> = runCatching {
        val display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        val version = IntArray(2)
        EGL14.eglInitialize(display, version, 0, version, 1)

        val configAttribs = intArrayOf(
            EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
            EGL14.EGL_NONE
        )
        val configs = arrayOfNulls<EGLConfig>(1)
        val numConfigs = IntArray(1)
        EGL14.eglChooseConfig(display, configAttribs, 0, configs, 0, 1, numConfigs, 0)

        val contextAttribs = intArrayOf(
            EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
            EGL14.EGL_NONE
        )
        val context = EGL14.eglCreateContext(display, configs[0], EGL14.EGL_NO_CONTEXT, contextAttribs, 0)

        val surfaceAttribs = intArrayOf(
            EGL14.EGL_WIDTH, 1,
            EGL14.EGL_HEIGHT, 1,
            EGL14.EGL_NONE
        )
        val surface = EGL14.eglCreatePbufferSurface(display, configs[0], surfaceAttribs, 0)

        EGL14.eglMakeCurrent(display, surface, surface, context)
        val renderer = GLES20.glGetString(GLES20.GL_RENDERER) ?: "Unknown"
        val vendor = GLES20.glGetString(GLES20.GL_VENDOR) ?: "Unknown"

        EGL14.eglMakeCurrent(display, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)
        EGL14.eglDestroySurface(display, surface)
        EGL14.eglDestroyContext(display, context)
        EGL14.eglTerminate(display)

        Pair(renderer, vendor)
    }.getOrElse {
        Log.e(TAG, "getGpuDetails error: ${it.message}", it)
        Pair("Unknown", "Unknown")
    }

    fun getGlesVersion(context: Context): String = runCatching {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val configurationInfo = activityManager.deviceConfigurationInfo
        configurationInfo.glEsVersion
    }.getOrElse {
        Log.e(TAG, "getGlesVersion error: ${it.message}", it)
        "Unknown"
    }
}