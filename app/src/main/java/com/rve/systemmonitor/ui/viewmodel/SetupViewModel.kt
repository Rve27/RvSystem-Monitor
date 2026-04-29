package com.rve.systemmonitor.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rve.systemmonitor.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class SetupViewModel @Inject constructor(private val settingsRepository: SettingsRepository) : ViewModel() {

    fun completeSetup() {
        viewModelScope.launch {
            settingsRepository.setSetupCompleted(true)
        }
    }
}
