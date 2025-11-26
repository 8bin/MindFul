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
    private val getDailyUsageUseCase: GetDailyUsageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadTodayUsage()
    }

    private fun loadTodayUsage() {
        viewModelScope.launch {
            getDailyUsageUseCase().collect { logs ->
                val totalDuration = logs.sumOf { it.durationMillis }
                _uiState.value = DashboardUiState(
                    usageLogs = logs,
                    totalUsageMillis = totalDuration
                )
            }
        }
    }
}

data class DashboardUiState(
    val usageLogs: List<UsageLogEntity> = emptyList(),
    val totalUsageMillis: Long = 0
)
