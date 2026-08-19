package com.rve.systemmonitor.utils

import android.app.ActivityManager
import android.content.Context
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.GLES20
import android.opengl.GLES30
import android.util.Log

object GpuUtils {
    private const val TAG = "GpuUtils"

    init {
        NativeLoader.load()
    }

    @JvmStatic
    private external fun getVulkanVersionNative(): String

    @JvmStatic
    private external fun getGpuTemperatureNative(): Double

    fun getGpuTemperature(): Double = runCatching {
        getGpuTemperatureNative()
    }.getOrElse {
        Log.e(TAG, "getGpuTemperature error: ${it.message}", it)
        0.0
    }

    private var cachedGpuDetails: Triple<String, String, Pair<Int, Int>>? = null
    private var cachedGlesVersion: String? = null
    private var cachedDetailedGlesVersion: String? = null
    private var cachedVulkanVersion: String? = null
    private var cachedVulkanDriverVersion: String? = null
    private var cachedVulkanDeviceType: String? = null
    private var cachedVulkanExtensionsCount: Int? = null
    private var cachedVulkanExtensions: List<String>? = null
    private var cachedVulkanMaxImage1D: Int? = null
    private var cachedVulkanMaxImage2D: Int? = null
    private var cachedVulkanMaxImage3D: Int? = null
    private var cachedVulkanMaxImageCube: Int? = null
    private var cachedVulkanMaxImageArrayLayers: Int? = null
    private var cachedVulkanMaxUniformBufferRange: Int? = null
    private var cachedVulkanMaxStorageBufferRange: Int? = null
    private var cachedVulkanMaxSamplerAnisotropy: Float? = null
    private var cachedVulkanMaxFramebufferColorSamples: Int? = null
    private var cachedVulkanMaxFramebufferDepthSamples: Int? = null
    private var cachedShadingLanguageVersion: String? = null
    private var cachedOpenGlExtensions: List<String>? = null
    private var cachedMaxCubeMapSize: Int? = null
    private var cachedMax3DTextureSize: Int? = null
    private var cachedMaxRenderbufferSize: Int? = null
    private var cachedMaxMsaaSamples: Int? = null
    private var cachedMaxVertexAttribs: Int? = null

    fun getGpuDetails(): Triple<String, String, Pair<Int, Int>> {
        cachedGpuDetails?.let { return it }
        return runCatching {
            val display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
            val version = IntArray(2)
            EGL14.eglInitialize(display, version, 0, version, 1)

            val configAttribs = intArrayOf(
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL14.EGL_NONE,
            )
            val configs = arrayOfNulls<EGLConfig>(1)
            val numConfigs = IntArray(1)
            EGL14.eglChooseConfig(display, configAttribs, 0, configs, 0, 1, numConfigs, 0)

            val contextAttribs = intArrayOf(
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL14.EGL_NONE,
            )
            val context = EGL14.eglCreateContext(display, configs[0], EGL14.EGL_NO_CONTEXT, contextAttribs, 0)

            val surfaceAttribs = intArrayOf(
                EGL14.EGL_WIDTH, 1,
                EGL14.EGL_HEIGHT, 1,
                EGL14.EGL_NONE,
            )
            val surface = EGL14.eglCreatePbufferSurface(display, configs[0], surfaceAttribs, 0)

            EGL14.eglMakeCurrent(display, surface, surface, context)
            val renderer = GLES20.glGetString(GLES20.GL_RENDERER) ?: "Unknown"
            val vendor = GLES20.glGetString(GLES20.GL_VENDOR) ?: "Unknown"
            val fullVersion = GLES20.glGetString(GLES20.GL_VERSION)
            if (fullVersion != null) {
                cachedDetailedGlesVersion = fullVersion.removePrefix("OpenGL ES ").trim()
            }

            cachedShadingLanguageVersion = GLES20.glGetString(GLES20.GL_SHADING_LANGUAGE_VERSION)
                ?.removePrefix("OpenGL ES GLSL ES ")?.trim()

            val maxTexSize = IntArray(1)
            GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, maxTexSize, 0)

            val maxCubeMapSize = IntArray(1)
            GLES20.glGetIntegerv(GLES20.GL_MAX_CUBE_MAP_TEXTURE_SIZE, maxCubeMapSize, 0)
            cachedMaxCubeMapSize = maxCubeMapSize[0]

            val max3DTextureSize = IntArray(1)
            // Try fetching from GLES30 (or GLES20 with OES_texture_3D)
            GLES30.glGetIntegerv(GLES30.GL_MAX_3D_TEXTURE_SIZE, max3DTextureSize, 0)
            cachedMax3DTextureSize = max3DTextureSize[0]

            val maxRenderbufferSize = IntArray(1)
            GLES20.glGetIntegerv(GLES20.GL_MAX_RENDERBUFFER_SIZE, maxRenderbufferSize, 0)
            cachedMaxRenderbufferSize = maxRenderbufferSize[0]

            val maxMsaaSamples = IntArray(1)
            GLES30.glGetIntegerv(GLES30.GL_MAX_SAMPLES, maxMsaaSamples, 0)
            cachedMaxMsaaSamples = maxMsaaSamples[0]

            val maxVertexAttribs = IntArray(1)
            GLES20.glGetIntegerv(GLES20.GL_MAX_VERTEX_ATTRIBS, maxVertexAttribs, 0)
            cachedMaxVertexAttribs = maxVertexAttribs[0]

            val extensions = GLES20.glGetString(GLES20.GL_EXTENSIONS) ?: ""
            val extList = if (extensions.isEmpty()) emptyList() else extensions.split(" ")
            val extCount = extList.size
            cachedOpenGlExtensions = extList

            EGL14.eglMakeCurrent(display, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)
            EGL14.eglDestroySurface(display, surface)
            EGL14.eglDestroyContext(display, context)
            EGL14.eglTerminate(display)

            val result = Triple(renderer, vendor, Pair(maxTexSize[0], extCount))
            cachedGpuDetails = result
            result
        }.getOrElse {
            Log.e(TAG, "getGpuDetails error: ${it.message}", it)
            Triple("Unknown", "Unknown", Pair(0, 0))
        }
    }

    fun getShadingLanguageVersion(): String {
        cachedShadingLanguageVersion?.let { return it }
        getGpuDetails()
        return cachedShadingLanguageVersion ?: "Unknown"
    }

    fun getVulkanDeviceType(): String {
        cachedVulkanDeviceType?.let { return it }
        updateVulkanInfo()
        return cachedVulkanDeviceType ?: "Unknown"
    }

    fun getDetailedGlesVersion(): String {
        cachedDetailedGlesVersion?.let { return it }
        getGpuDetails()
        return cachedDetailedGlesVersion ?: "Unknown"
    }

    fun getGlesVersion(context: Context): String {
        cachedGlesVersion?.let { return it }
        return runCatching {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val configurationInfo = activityManager.deviceConfigurationInfo
            val result = configurationInfo.glEsVersion
            cachedGlesVersion = result
            result
        }.getOrElse {
            Log.e(TAG, "getGlesVersion error: ${it.message}", it)
            "Unknown"
        }
    }

    fun getVulkanVersion(context: Context): String {
        cachedVulkanVersion?.let { return it }
        updateVulkanInfo()
        return cachedVulkanVersion ?: "Unknown"
    }

    fun getVulkanDriverVersion(): String {
        cachedVulkanDriverVersion?.let { return it }
        updateVulkanInfo()
        return cachedVulkanDriverVersion ?: "Unknown"
    }

    fun getVulkanExtensionsCount(): Int {
        cachedVulkanExtensionsCount?.let { return it }
        updateVulkanInfo()
        return cachedVulkanExtensionsCount ?: 0
    }

    fun getVulkanExtensions(): List<String> {
        cachedVulkanExtensions?.let { return it }
        updateVulkanInfo()
        return cachedVulkanExtensions ?: emptyList()
    }

    fun getVulkanMaxImage1D(): Int {
        cachedVulkanMaxImage1D?.let { return it }
        updateVulkanInfo()
        return cachedVulkanMaxImage1D ?: 0
    }

    fun getVulkanMaxImage2D(): Int {
        cachedVulkanMaxImage2D?.let { return it }
        updateVulkanInfo()
        return cachedVulkanMaxImage2D ?: 0
    }

    fun getVulkanMaxImage3D(): Int {
        cachedVulkanMaxImage3D?.let { return it }
        updateVulkanInfo()
        return cachedVulkanMaxImage3D ?: 0
    }

    fun getVulkanMaxImageCube(): Int {
        cachedVulkanMaxImageCube?.let { return it }
        updateVulkanInfo()
        return cachedVulkanMaxImageCube ?: 0
    }

    fun getVulkanMaxImageArrayLayers(): Int {
        cachedVulkanMaxImageArrayLayers?.let { return it }
        updateVulkanInfo()
        return cachedVulkanMaxImageArrayLayers ?: 0
    }

    fun getVulkanMaxUniformBufferRange(): Int {
        cachedVulkanMaxUniformBufferRange?.let { return it }
        updateVulkanInfo()
        return cachedVulkanMaxUniformBufferRange ?: 0
    }

    fun getVulkanMaxStorageBufferRange(): Int {
        cachedVulkanMaxStorageBufferRange?.let { return it }
        updateVulkanInfo()
        return cachedVulkanMaxStorageBufferRange ?: 0
    }

    fun getVulkanMaxSamplerAnisotropy(): Float {
        cachedVulkanMaxSamplerAnisotropy?.let { return it }
        updateVulkanInfo()
        return cachedVulkanMaxSamplerAnisotropy ?: 0f
    }

    fun getVulkanMaxFramebufferColorSamples(): Int {
        cachedVulkanMaxFramebufferColorSamples?.let { return it }
        updateVulkanInfo()
        return cachedVulkanMaxFramebufferColorSamples ?: 0
    }

    fun getVulkanMaxFramebufferDepthSamples(): Int {
        cachedVulkanMaxFramebufferDepthSamples?.let { return it }
        updateVulkanInfo()
        return cachedVulkanMaxFramebufferDepthSamples ?: 0
    }

    fun getMaxCubeMapTextureSize(): Int {
        cachedMaxCubeMapSize?.let { return it }
        getGpuDetails()
        return cachedMaxCubeMapSize ?: 0
    }

    fun getMax3DTextureSize(): Int {
        cachedMax3DTextureSize?.let { return it }
        getGpuDetails()
        return cachedMax3DTextureSize ?: 0
    }

    fun getMaxRenderbufferSize(): Int {
        cachedMaxRenderbufferSize?.let { return it }
        getGpuDetails()
        return cachedMaxRenderbufferSize ?: 0
    }

    fun getMaxMsaaSamples(): Int {
        cachedMaxMsaaSamples?.let { return it }
        getGpuDetails()
        return cachedMaxMsaaSamples ?: 0
    }

    fun getMaxVertexAttribs(): Int {
        cachedMaxVertexAttribs?.let { return it }
        getGpuDetails()
        return cachedMaxVertexAttribs ?: 0
    }

    fun getOpenGlExtensions(): List<String> {
        cachedOpenGlExtensions?.let { return it }
        getGpuDetails()
        return cachedOpenGlExtensions ?: emptyList()
    }

    private fun updateVulkanInfo() {
        runCatching {
            val result = getVulkanVersionNative()
            val parts = result.split("|")
            cachedVulkanVersion = parts.getOrNull(0) ?: "Unknown"
            cachedVulkanDriverVersion = parts.getOrNull(1) ?: "Unknown"
            cachedVulkanDeviceType = parts.getOrNull(2) ?: "Unknown"
            cachedVulkanExtensionsCount = parts.getOrNull(3)?.toIntOrNull() ?: 0
            val extensionsStr = parts.getOrNull(4) ?: ""
            cachedVulkanExtensions = if (extensionsStr.isNotEmpty()) extensionsStr.split(",") else emptyList()
            cachedVulkanMaxImage1D = parts.getOrNull(5)?.toIntOrNull() ?: 0
            cachedVulkanMaxImage2D = parts.getOrNull(6)?.toIntOrNull() ?: 0
            cachedVulkanMaxImage3D = parts.getOrNull(7)?.toIntOrNull() ?: 0
            cachedVulkanMaxImageCube = parts.getOrNull(8)?.toIntOrNull() ?: 0
            cachedVulkanMaxImageArrayLayers = parts.getOrNull(9)?.toIntOrNull() ?: 0
            cachedVulkanMaxUniformBufferRange = parts.getOrNull(10)?.toIntOrNull() ?: 0
            cachedVulkanMaxStorageBufferRange = parts.getOrNull(11)?.toIntOrNull() ?: 0
            cachedVulkanMaxSamplerAnisotropy = parts.getOrNull(12)?.toFloatOrNull() ?: 0f
            cachedVulkanMaxFramebufferColorSamples = parts.getOrNull(13)?.toIntOrNull() ?: 0
            cachedVulkanMaxFramebufferDepthSamples = parts.getOrNull(14)?.toIntOrNull() ?: 0
        }.onFailure {
            Log.e(TAG, "updateVulkanInfo error: ${it.message}", it)
            cachedVulkanVersion = "Unknown"
            cachedVulkanDriverVersion = "Unknown"
            cachedVulkanDeviceType = "Unknown"
            cachedVulkanExtensionsCount = 0
            cachedVulkanExtensions = emptyList()
            cachedVulkanMaxImage1D = 0
            cachedVulkanMaxImage2D = 0
            cachedVulkanMaxImage3D = 0
            cachedVulkanMaxImageCube = 0
            cachedVulkanMaxImageArrayLayers = 0
            cachedVulkanMaxUniformBufferRange = 0
            cachedVulkanMaxStorageBufferRange = 0
            cachedVulkanMaxSamplerAnisotropy = 0f
            cachedVulkanMaxFramebufferColorSamples = 0
            cachedVulkanMaxFramebufferDepthSamples = 0
        }
    }

    fun formatBinarySize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        val formatted = String.format(java.util.Locale.US, "%.1f %s", bytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
        return formatted.replace(".0 ", " ")
    }
}
