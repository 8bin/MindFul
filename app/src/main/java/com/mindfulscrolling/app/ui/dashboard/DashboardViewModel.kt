package com.mindfulscrolling.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindfulscrolling.app.data.local.entity.UsageLogEntity
import com.mindfulscrolling.app.domain.usecase.GetDailyUsageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getDailyUsageUseCase: GetDailyUsageUseCase,
    private val manageBreakUseCase: com.mindfulscrolling.app.domain.usecase.ManageBreakUseCase,
    private val appRepository: com.mindfulscrolling.app.domain.repository.AppRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadTodayUsage()
        observeBreakState()
        loadInstalledApps()
        loadProfiles()
    }

    private fun loadInstalledApps() {
        viewModelScope.launch {
            val apps = appRepository.getInstalledApps()
            _uiState.value = _uiState.value.copy(installedApps = apps)
        }
    }

    private fun loadTodayUsage() {
        viewModelScope.launch {
            getDailyUsageUseCase().collect { logs ->
                val totalDuration = logs.sumOf { it.durationMillis }
                _uiState.value = _uiState.value.copy(
                    usageLogs = logs,
                    totalUsageMillis = totalDuration
                )
            }
        }
    }
    
    private fun observeBreakState() {
        viewModelScope.launch {
            kotlinx.coroutines.flow.combine(
                manageBreakUseCase.isBreakActive,
                manageBreakUseCase.breakEndTime
            ) { isActive, endTime ->
                Pair(isActive, endTime)
            }.collect { (isActive, endTime) ->
                _uiState.value = _uiState.value.copy(
                    isBreakActive = isActive,
                    breakEndTime = endTime
                )
            }
        }
    }
    
    fun startBreak(durationMinutes: Int, whitelist: Set<String>) {
        viewModelScope.launch {
            manageBreakUseCase.startBreak(durationMinutes, whitelist)
        }
    }
    
    fun startBreakWithProfile(durationMinutes: Int, profileId: Long) {
        viewModelScope.launch {
            manageBreakUseCase.startBreakWithProfile(durationMinutes, profileId)
        }
    }
    
    fun stopBreak() {
        viewModelScope.launch {
            manageBreakUseCase.stopBreak()
        }
    }
    
    private fun loadProfiles() {
        viewModelScope.launch {
            appRepository.getAllProfiles().collect { profiles ->
                _uiState.value = _uiState.value.copy(profiles = profiles)
            }
        }
    }
}

data class DashboardUiState(
    val usageLogs: List<UsageLogEntity> = emptyList(),
    val totalUsageMillis: Long = 0,
    val isBreakActive: Boolean = false,
    val breakEndTime: Long = 0,
    val installedApps: List<com.mindfulscrolling.app.domain.model.AppInfo> = emptyList(),
    val profiles: List<com.mindfulscrolling.app.data.local.entity.FocusProfileEntity> = emptyList()
)
