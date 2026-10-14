package com.rve.systemmonitor.privileged

import com.rve.systemmonitor.data.di.ApplicationScope
import com.rve.systemmonitor.domain.repository.SettingsRepository
import com.rve.systemmonitor.root.RootManager
import com.rve.systemmonitor.shizuku.ShizukuManager
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Single entry point for privileged shell commands. Shizuku and root are independent backends,
 * each with its own toggle, and there is no fallback between them: the enabled backend is the
 * only one ever used, even when it is not ready. The settings keep the two toggles mutually
 * exclusive; if a stale/imported state has both on, Shizuku wins.
 */
@Singleton
class PrivilegedAccess @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val shizukuManager: ShizukuManager,
    private val rootManager: RootManager,
    @param:ApplicationScope private val externalScope: CoroutineScope,
) {
    val isRootGranted: StateFlow<Boolean> = rootManager.isRootGranted

    /** True when the selected backend is usable right now. */
    val isReady: Flow<Boolean> = combine(
        settingsRepository.useShizuku,
        shizukuManager.isShizukuAvailable,
        shizukuManager.hasPermission,
        settingsRepository.useRoot,
        rootManager.isRootGranted,
    ) { useShizuku, shizukuAvailable, shizukuGranted, useRoot, rootGranted ->
        when {
            useShizuku -> shizukuAvailable && shizukuGranted
            useRoot -> rootGranted
            else -> false
        }
    }.distinctUntilChanged()

    fun init() {
        externalScope.launch {
            settingsRepository.useRoot.distinctUntilChanged().collect { enabled ->
                if (enabled) rootManager.restore() else rootManager.close()
            }
        }
    }

    suspend fun requestRoot(): Boolean = rootManager.requestRoot()

    suspend fun executeCommand(command: String): String {
        return when {
            settingsRepository.useShizuku.first() -> shizukuManager.executeCommand(command)
            settingsRepository.useRoot.first() -> rootManager.executeCommand(command)
            else -> ""
        }
    }
}
