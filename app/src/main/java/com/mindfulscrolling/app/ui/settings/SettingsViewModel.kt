package com.mindfulscrolling.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindfulscrolling.app.data.local.entity.AppLimitEntity
import com.mindfulscrolling.app.domain.usecase.GetAllAppLimitsUseCase
import com.mindfulscrolling.app.domain.usecase.RemoveAppLimitUseCase
import com.mindfulscrolling.app.domain.usecase.SetAppLimitUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getAllAppLimitsUseCase: GetAllAppLimitsUseCase,
    private val setAppLimitUseCase: SetAppLimitUseCase,
    private val removeAppLimitUseCase: RemoveAppLimitUseCase
) : ViewModel() {

    private val _limits = MutableStateFlow<List<AppLimitEntity>>(emptyList())
    val limits: StateFlow<List<AppLimitEntity>> = _limits.asStateFlow()

    init {
        viewModelScope.launch {
            getAllAppLimitsUseCase().collect {
                _limits.value = it
            }
        }
    }

    fun setLimit(packageName: String, minutes: Int) {
        viewModelScope.launch {
            setAppLimitUseCase(packageName, minutes)
        }
    }

    fun removeLimit(packageName: String) {
        viewModelScope.launch {
            removeAppLimitUseCase(packageName)
        }
    }
}
