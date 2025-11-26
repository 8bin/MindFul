package com.mindfulscrolling.app.ui.limits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindfulscrolling.app.data.local.entity.AppLimitEntity
import com.mindfulscrolling.app.domain.model.AppInfo
import com.mindfulscrolling.app.domain.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppListViewModel @Inject constructor(
    private val appRepository: AppRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AppListUiState>(AppListUiState.Loading)
    val uiState: StateFlow<AppListUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                val installedApps = appRepository.getInstalledApps()
                
                kotlinx.coroutines.flow.combine(
                    appRepository.getAllLimits(),
                    _searchQuery
                ) { limits, query ->
                    val limitMap = limits.associateBy { it.packageName }
                    val filteredApps = if (query.isBlank()) {
                        installedApps
                    } else {
                        installedApps.filter { 
                            it.name.contains(query, ignoreCase = true) || 
                            it.packageName.contains(query, ignoreCase = true) 
                        }
                    }
                    
                    filteredApps.map { app ->
                        AppItemUiState(
                            appInfo = app,
                            limitMinutes = limitMap[app.packageName]?.limitDurationMinutes
                        )
                    }
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

    fun setLimit(packageName: String, minutes: Int) {
        viewModelScope.launch {
            appRepository.insertLimit(
                AppLimitEntity(
                    packageName = packageName,
                    limitDurationMinutes = minutes
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
    val limitMinutes: Int?
)
