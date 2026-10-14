package com.rve.systemmonitor.root

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Root backend over a single persistent `su` shell.
 *
 * `/system/bin/su` is tried first and is exec'd directly without an exists() check: KernelSU
 * intercepts execve on that path, so the file does not have to exist on disk. If it fails,
 * well-known locations are tried and finally `find` is used to locate any `su` binary.
 */
@Singleton
class RootManager @Inject constructor(@param:ApplicationContext private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val prefs by lazy { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    private val mutex = Mutex()

    private val _isRootGranted = MutableStateFlow(false)
    val isRootGranted: StateFlow<Boolean> = _isRootGranted.asStateFlow()

    private var process: Process? = null
    private var writer: BufferedWriter? = null
    private var reader: BufferedReader? = null

    /** Silent reconnect, only when root was granted before. Never prompts on a fresh install. */
    fun restore() {
        if (!prefs.getBoolean(KEY_GRANTED, false)) return
        scope.launch { requestRoot() }
    }

    /** Locates su, opens the shell and verifies uid 0. May show the su manager prompt. */
    suspend fun requestRoot(): Boolean = mutex.withLock {
        withContext(Dispatchers.IO) {
            if (_isRootGranted.value && isShellAlive()) return@withContext true
            closeShell()

            val tried = HashSet<String>()
            val cached = prefs.getString(KEY_SU_PATH, null)
            val candidates = sequence {
                if (cached != null) yield(cached)
                yieldAll(KNOWN_SU_PATHS)
            }
            var ok = candidates.any { tried.add(it) && openShell(it) }
            if (!ok) {
                ok = findSuBinaries().any { tried.add(it) && openShell(it) }
            }
            _isRootGranted.value = ok
            prefs.edit().putBoolean(KEY_GRANTED, ok).apply()
            ok
        }
    }

    suspend fun executeCommand(command: String): String {
        if (!_isRootGranted.value) return ""
        return mutex.withLock {
            withContext(Dispatchers.IO) {
                try {
                    runInShell(command)
                } catch (e: IOException) {
                    Log.e(TAG, "su shell died: ${e.message}")
                    closeShell()
                    val path = prefs.getString(KEY_SU_PATH, null)
                    if (path != null && openShell(path)) {
                        try {
                            runInShell(command)
                        } catch (e2: IOException) {
                            closeShell()
                            _isRootGranted.value = false
                            ""
                        }
                    } else {
                        _isRootGranted.value = false
                        ""
                    }
                }
            }
        }
    }

    fun close() {
        closeShell()
        _isRootGranted.value = false
    }

    private fun isShellAlive(): Boolean = process?.isAlive == true

    private fun openShell(suPath: String): Boolean {
        var p: Process? = null
        return try {
            val proc = ProcessBuilder(suPath).redirectErrorStream(true).start()
            p = proc
            val w = BufferedWriter(OutputStreamWriter(proc.outputStream))
            val r = BufferedReader(InputStreamReader(proc.inputStream))
            process = proc
            writer = w
            reader = r

            // Generous timeout: the first call blocks on the su manager grant dialog.
            val watchdog = scope.launch {
                delay(HANDSHAKE_TIMEOUT_MS)
                proc.destroy()
            }
            val out = try {
                w.write("id\necho $MARKER\n")
                w.flush()
                readUntilMarker(r)
            } finally {
                watchdog.cancel()
            }
            if (out.contains("uid=0")) {
                prefs.edit().putString(KEY_SU_PATH, suPath).apply()
                true
            } else {
                closeShell()
                false
            }
        } catch (e: IOException) {
            p?.destroy()
            closeShell()
            false
        }
    }

    private fun runInShell(command: String): String {
        val w = writer ?: throw IOException("no shell")
        val r = reader ?: throw IOException("no shell")
        val p = process ?: throw IOException("no shell")
        val watchdog = scope.launch {
            delay(COMMAND_TIMEOUT_MS)
            p.destroy()
        }
        try {
            w.write("{ $command\n} 2>/dev/null </dev/null\necho\necho $MARKER\n")
            w.flush()
            return readUntilMarker(r)
        } finally {
            watchdog.cancel()
        }
    }

    private fun readUntilMarker(r: BufferedReader): String {
        val sb = StringBuilder()
        while (true) {
            val line = r.readLine() ?: throw IOException("shell closed")
            if (line == MARKER) break
            sb.append(line).append('\n')
        }
        return sb.toString().trim()
    }

    private fun closeShell() {
        runCatching { writer?.close() }
        runCatching { process?.destroy() }
        process = null
        writer = null
        reader = null
    }

    private fun findSuBinaries(): List<String> {
        return try {
            val cmd = "find ${FIND_ROOTS.joinToString(" ")} -maxdepth 4 -name su 2>/dev/null"
            val p = ProcessBuilder("sh", "-c", cmd).redirectErrorStream(false).start()
            val lines = p.inputStream.bufferedReader().readLines()
            p.waitFor(10, TimeUnit.SECONDS)
            p.destroy()
            lines.map { it.trim() }
                .filter { it.startsWith("/") && File(it).let { f -> f.isFile && f.canExecute() } }
        } catch (e: Exception) {
            Log.e(TAG, "find su failed: ${e.message}")
            emptyList()
        }
    }

    private companion object {
        const val TAG = "RootManager"
        const val PREFS_NAME = "root_backend"
        const val KEY_GRANTED = "granted"
        const val KEY_SU_PATH = "su_path"
        const val MARKER = "__RVSM_CMD_DONE__"
        const val HANDSHAKE_TIMEOUT_MS = 30_000L
        const val COMMAND_TIMEOUT_MS = 5_000L

        val KNOWN_SU_PATHS = listOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/su/bin/su",
            "/debug_ramdisk/su",
            "/data/adb/ksu/bin/su",
            "/data/adb/magisk/su",
            "su",
        )
        val FIND_ROOTS = listOf(
            "/system", "/system_ext", "/product", "/vendor", "/odm",
            "/sbin", "/su", "/debug_ramdisk", "/data/adb",
        )
    }
}
