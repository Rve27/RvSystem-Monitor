package com.rve.systemmonitor.ui.viewmodel

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rve.systemmonitor.domain.repository.OverlayRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@androidx.compose.runtime.Immutable
data class AppInfo(val packageName: String, val name: String)

@HiltViewModel
class AutoToggleSettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val overlayRepository: OverlayRepository,
) : ViewModel() {

    val isOverlayEnabled: StateFlow<Boolean> = overlayRepository.isOverlayEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false,
        )

    val isAutoToggleEnabled: StateFlow<Boolean> = overlayRepository.isAutoToggleEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false,
        )

    val autoToggleApps: StateFlow<Set<String>> = overlayRepository.autoToggleApps
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet(),
        )

    private val _installedApps = MutableStateFlow<ImmutableList<AppInfo>>(persistentListOf())
    val installedApps: StateFlow<ImmutableList<AppInfo>> = _installedApps.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadInstalledApps()
    }

    private fun loadInstalledApps() {
        viewModelScope.launch {
            _isLoading.value = true
            val apps = withContext(Dispatchers.IO) {
                val pm = context.packageManager
                val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                packages.filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 || it.packageName == "com.rve.systemmonitor" }
                    .map {
                        AppInfo(
                            packageName = it.packageName,
                            name = it.loadLabel(pm).toString(),
                        )
                    }
                    .sortedBy { it.name.lowercase() }
            }
            _installedApps.value = apps.toImmutableList()
            _isLoading.value = false
        }
    }

    fun setAutoToggleEnabled(enabled: Boolean) {
        viewModelScope.launch {
            if (enabled) {
                overlayRepository.setOverlayEnabled(false)
            }
            overlayRepository.setAutoToggleEnabled(enabled)
        }
    }

    fun toggleApp(packageName: String) {
        viewModelScope.launch {
            val currentApps = autoToggleApps.value.toMutableSet()
            if (currentApps.contains(packageName)) {
                currentApps.remove(packageName)
            } else {
                currentApps.add(packageName)
            }
            overlayRepository.setAutoToggleApps(currentApps)
        }
    }
}
