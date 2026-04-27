package com.rve.systemmonitor.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rve.systemmonitor.domain.repository.SettingsRepository
import com.rve.systemmonitor.utils.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class MainViewModel @Inject constructor(settingsRepository: SettingsRepository) : ViewModel() {
    val uiState: StateFlow<MainUiState> = settingsRepository.themeMode
        .map { MainUiState.Success(it) }
        .stateIn(
            scope = viewModelScope,
            initialValue = MainUiState.Loading,
            started = SharingStarted.WhileSubscribed(5_000),
        )
}

sealed interface MainUiState {
    data object Loading : MainUiState
    data class Success(val themeMode: ThemeMode) : MainUiState
}
