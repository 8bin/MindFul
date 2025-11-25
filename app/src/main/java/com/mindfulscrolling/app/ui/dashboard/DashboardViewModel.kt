package com.mindfulscrolling.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindfulscrolling.app.data.local.entity.UsageLogEntity
import com.mindfulscrolling.app.domain.usecase.GetAppUsageStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getAppUsageStatsUseCase: GetAppUsageStatsUseCase
) : ViewModel() {

    private val _usageState = MutableStateFlow<List<UsageLogEntity>>(emptyList())
    val usageState: StateFlow<List<UsageLogEntity>> = _usageState.asStateFlow()

    init {
        loadTodayUsage()
    }

    private fun loadTodayUsage() {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        viewModelScope.launch {
            getAppUsageStatsUseCase(today).collect { logs ->
                _usageState.value = logs.sortedByDescending { it.durationMillis }
            }
        }
    }
}
