package com.mindfulscrolling.app.ui.limits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindfulscrolling.app.data.datasource.UsageStatsDataSource
import com.mindfulscrolling.app.data.local.entity.AppLimitEntity
import com.mindfulscrolling.app.domain.model.AppInfo
import com.mindfulscrolling.app.domain.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

enum class AppFilterMode(val label: String) {
    ALL("All Apps"),
    INSTALLED("Installed"),
    SYSTEM("System")
}

@HiltViewModel
class AppListViewModel @Inject constructor(
    private val appRepository: AppRepository,
    private val usageStatsDataSource: UsageStatsDataSource
) : ViewModel() {

    private val _uiState = MutableStateFlow<AppListUiState>(AppListUiState.Loading)
    val uiState: StateFlow<AppListUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _filterMode = MutableStateFlow(AppFilterMode.ALL)
    val filterMode = _filterMode.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                val installedApps = appRepository.getInstalledApps()
                
                // Load real usage data for today
                val todayStart = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                val now = System.currentTimeMillis()
                
                val usageMap = try {
                    usageStatsDataSource.getUsageStats(todayStart, now)
                } catch (_: Exception) {
                    emptyMap()
                }
                
                kotlinx.coroutines.flow.combine(
                    appRepository.getAllLimits(),
                    _searchQuery,
                    _filterMode
                ) { limits, query, filter ->
                    val limitMap = limits.associateBy { it.packageName }
                    
                    // Apply filter
                    val filteredByType = when (filter) {
                        AppFilterMode.ALL -> installedApps
                        AppFilterMode.INSTALLED -> installedApps.filter { !it.isSystemApp }
                        AppFilterMode.SYSTEM -> installedApps.filter { it.isSystemApp }
                    }
                    
                    // Apply search
                    val filteredApps = if (query.isBlank()) {
                        filteredByType
                    } else {
                        filteredByType.filter { 
                            it.name.contains(query, ignoreCase = true) || 
                            it.packageName.contains(query, ignoreCase = true) 
                        }
                    }
                    
                    filteredApps.map { app ->
                        AppItemUiState(
                            appInfo = app,
                            limitMinutes = limitMap[app.packageName]?.limitDurationMinutes,
                            notificationIntervalMinutes = limitMap[app.packageName]?.notificationIntervalMinutes,
                            usageMillis = usageMap[app.packageName] ?: 0L
                        )
                    }.sortedByDescending { it.usageMillis }
                }.collect { appItems ->
                    _uiState.value = AppListUiState.Success(appItems)
                }
            } catch (e: Exception) {
                _uiState.value = AppListUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onFilterModeChanged(mode: AppFilterMode) {
        _filterMode.value = mode
    }

    fun setLimit(packageName: String, minutes: Int, notificationInterval: Int? = null) {
        viewModelScope.launch {
            appRepository.insertLimit(
                AppLimitEntity(
                    packageName = packageName,
                    limitDurationMinutes = minutes,
                    notificationIntervalMinutes = notificationInterval
                )
            )
        }
    }

    fun removeLimit(packageName: String) {
        viewModelScope.launch {
            appRepository.deleteLimit(packageName)
        }
    }
}

sealed class AppListUiState {
    object Loading : AppListUiState()
    data class Success(val apps: List<AppItemUiState>) : AppListUiState()
    data class Error(val message: String) : AppListUiState()
}

data class AppItemUiState(
    val appInfo: AppInfo,
    val limitMinutes: Int?,
    val notificationIntervalMinutes: Int? = null,
    val usageMillis: Long = 0
)
