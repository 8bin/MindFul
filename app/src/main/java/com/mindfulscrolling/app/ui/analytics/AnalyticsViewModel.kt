package com.mindfulscrolling.app.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindfulscrolling.app.domain.model.AppInfo
import com.mindfulscrolling.app.domain.model.AppUsageItem
import com.mindfulscrolling.app.domain.model.BarData
import com.mindfulscrolling.app.domain.model.DailyAnalytics
import com.mindfulscrolling.app.domain.repository.AppRepository
import com.mindfulscrolling.app.domain.usecase.GetAnalyticsDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val getAnalyticsDataUseCase: GetAnalyticsDataUseCase,
    private val appRepository: AppRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        loadAppInfo()
        loadAnalytics(LocalDate.now())
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
        loadAnalytics(date)
    }

    fun onTabSelected(tab: String) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
        updateUiData()
    }

    private fun loadAppInfo() {
        viewModelScope.launch {
            val apps = appRepository.getInstalledApps()
            val map = apps.associateBy { it.packageName }
            _uiState.value = _uiState.value.copy(appInfoMap = map)
            updateUiData() // Refresh if data loaded first
        }
    }

    private fun loadAnalytics(date: LocalDate) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            // Start of day in millis
            val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            
            val analytics = getAnalyticsDataUseCase(startOfDay)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                dailyAnalytics = analytics
            )
            updateUiData()
        }
    }

    private fun updateUiData() {
        val state = _uiState.value
        val analytics = state.dailyAnalytics ?: return
        val tab = state.selectedTab
        val appMap = state.appInfoMap

        // Graph Data
        val graphData = when (tab) {
            "Screen Time" -> analytics.hourlyUsageMap.map { BarData(it.key, it.value) }
            "App Launches" -> analytics.hourlyLaunchMap.map { BarData(it.key, it.value.toLong()) }
            "Screen Unlocks" -> analytics.hourlyUnlockMap.map { BarData(it.key, it.value.toLong()) }
            else -> emptyList()
        }
        
        // Fill missing hours with 0
        val fullGraphData = (0..23).map { hour ->
            graphData.find { it.hour == hour } ?: BarData(hour, 0)
        }

        // Overview List
        val overviewList = when (tab) {
            "Screen Time" -> {
                val total = analytics.totalScreenTime.toFloat().coerceAtLeast(1f)
                analytics.appUsageMap.map { (pkg, usage) ->
                    val app = appMap[pkg]
                    AppUsageItem(
                        packageName = pkg,
                        appName = app?.name ?: pkg,
                        usageMillis = usage,
                        percentage = usage / total
                    )
                }.sortedByDescending { it.usageMillis }
            }
            "App Launches" -> {
                val total = analytics.appLaunchMap.values.sum().toFloat().coerceAtLeast(1f)
                analytics.appLaunchMap.map { (pkg, count) ->
                    val app = appMap[pkg]
                    AppUsageItem(
                        packageName = pkg,
                        appName = app?.name ?: pkg,
                        usageMillis = count.toLong(), // Reusing field for count
                        percentage = count / total
                    )
                }.sortedByDescending { it.usageMillis }
            }
             "Screen Unlocks" -> {
                 // Unlocks are system-wide, typically no app breakdown unless we track "unlocked to app X"
                 // For now, show total unlocks as a single item or empty
                 emptyList()
             }
             "Usage Timeline", "Browsing Time" -> emptyList()
             else -> emptyList()
        }

        _uiState.value = _uiState.value.copy(
            graphData = fullGraphData,
            overviewList = overviewList
        )
    }
}

data class AnalyticsUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedTab: String = "Screen Time",
    val isLoading: Boolean = false,
    val dailyAnalytics: DailyAnalytics? = null,
    val appInfoMap: Map<String, AppInfo> = emptyMap(),
    val graphData: List<BarData> = emptyList(),
    val overviewList: List<AppUsageItem> = emptyList()
)
