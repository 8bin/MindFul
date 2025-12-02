package com.mindfulscrolling.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindfulscrolling.app.domain.usecase.SettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsUseCase: SettingsUseCase
) : ViewModel() {

    val themeMode: StateFlow<String> = settingsUseCase.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "SYSTEM")

    val isStrictModeEnabled: StateFlow<Boolean> = settingsUseCase.isStrictModeEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            settingsUseCase.setThemeMode(mode)
        }
    }

    fun enableStrictMode(pin: String) {
        viewModelScope.launch {
            settingsUseCase.enableStrictMode(pin)
        }
    }

    fun disableStrictMode(pin: String, onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            if (settingsUseCase.disableStrictMode(pin)) {
                onSuccess()
            } else {
                onError()
            }
        }
    }

    fun changePin(oldPin: String, newPin: String, onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            if (settingsUseCase.changePin(oldPin, newPin)) {
                onSuccess()
            } else {
                onError()
            }
        }
    }
    
    fun verifyPin(pin: String, onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            if (settingsUseCase.validatePin(pin)) {
                onSuccess()
            } else {
                onError()
            }
        }
    }
}
