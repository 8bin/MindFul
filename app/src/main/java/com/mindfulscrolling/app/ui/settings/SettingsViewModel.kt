package com.mindfulscrolling.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindfulscrolling.app.domain.usecase.SettingsUseCase
import com.mindfulscrolling.app.service.UsageNotificationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsUseCase: SettingsUseCase,
    private val usageNotificationManager: UsageNotificationManager
) : ViewModel() {

    val themeMode: StateFlow<String> = settingsUseCase.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "SYSTEM")

    val isStrictModeEnabled: StateFlow<Boolean> = settingsUseCase.isStrictModeEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _notificationsEnabled = MutableStateFlow(usageNotificationManager.isEnabled)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _notificationInterval = MutableStateFlow(usageNotificationManager.intervalMinutes)
    val notificationInterval: StateFlow<Int> = _notificationInterval.asStateFlow()

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            settingsUseCase.setThemeMode(mode)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        usageNotificationManager.isEnabled = enabled
        _notificationsEnabled.value = enabled
    }

    fun setNotificationInterval(minutes: Int) {
        usageNotificationManager.intervalMinutes = minutes
        _notificationInterval.value = minutes
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
