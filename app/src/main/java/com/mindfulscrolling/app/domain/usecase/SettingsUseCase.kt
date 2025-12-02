package com.mindfulscrolling.app.domain.usecase

import com.mindfulscrolling.app.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SettingsUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {
    val themeMode: Flow<String> = userPreferencesRepository.themeMode
    val isStrictModeEnabled: Flow<Boolean> = userPreferencesRepository.isStrictModeEnabled

    suspend fun setThemeMode(mode: String) {
        userPreferencesRepository.setThemeMode(mode)
    }

    suspend fun enableStrictMode(pin: String) {
        userPreferencesRepository.setPinCode(pin)
        userPreferencesRepository.setStrictModeEnabled(true)
    }

    suspend fun disableStrictMode(pin: String): Boolean {
        val storedPin = userPreferencesRepository.pinCode.first()
        return if (storedPin == pin) {
            userPreferencesRepository.setStrictModeEnabled(false)
            true
        } else {
            false
        }
    }

    suspend fun validatePin(pin: String): Boolean {
        val storedPin = userPreferencesRepository.pinCode.first()
        return storedPin == pin
    }
    
    suspend fun changePin(oldPin: String, newPin: String): Boolean {
         val storedPin = userPreferencesRepository.pinCode.first()
         return if (storedPin == oldPin) {
             userPreferencesRepository.setPinCode(newPin)
             true
         } else {
             false
         }
    }
}
