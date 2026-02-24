package com.mindfulscrolling.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindfulscrolling.app.data.datasource.UsageStatsDataSource
import com.mindfulscrolling.app.data.local.entity.UsageLogEntity
import com.mindfulscrolling.app.domain.usecase.GetDailyUsageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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
    private val appRepository: com.mindfulscrolling.app.domain.repository.AppRepository,
    private val permissionManager: com.mindfulscrolling.app.domain.manager.PermissionManager,
    private val usageStatsDataSource: UsageStatsDataSource
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    companion object {
        const val DAILY_GOAL_MS = 4 * 3600 * 1000L // 4 hours
        private const val REFRESH_INTERVAL_MS = 30_000L // 30 seconds
    }

    init {
        observeBreakState()
        loadInstalledApps()
        loadProfiles()
        checkPermissions()
        // System API is the SINGLE source of truth — no Room DB flow for dashboard totals
        loadSystemAnalytics()
        load7DayHistory()
        startPeriodicRefresh()
    }

    fun checkPermissions() {
        val allGranted = permissionManager.hasUsageStatsPermission() && 
                         permissionManager.hasOverlayPermission() && 
                         permissionManager.isAccessibilityServiceEnabled()
        _uiState.value = _uiState.value.copy(allPermissionsGranted = allGranted)
    }

    private fun loadInstalledApps() {
        viewModelScope.launch {
            val apps = appRepository.getInstalledApps()
            _uiState.value = _uiState.value.copy(installedApps = apps)
        }
    }

    /**
     * Periodic refresh — updates dashboard from system API every 30s
     * so screen time stays current without manual refresh.
     */
    private fun startPeriodicRefresh() {
        viewModelScope.launch {
            while (true) {
                delay(REFRESH_INTERVAL_MS)
                loadSystemAnalytics()
            }
        }
    }

    /**
     * SINGLE SOURCE OF TRUTH for all dashboard metrics.
     * 
     * Uses UsageStatsManager.queryAndAggregateUsageStats() — the EXACT same API
     * Android's built-in Digital Wellbeing uses.
     * 
     * Previously, a Room DB Flow (loadTodayUsage) would continuously overwrite
     * these values with stale/inconsistent data. That race condition is eliminated.
     */
    private fun loadSystemAnalytics() {
        viewModelScope.launch {
            try {
                val todayStart = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                val now = System.currentTimeMillis()
                
                val analytics = usageStatsDataSource.getDailyAnalytics(todayStart, now)
                
                val totalAppLaunches = analytics.appLaunchMap.values.sum()
                val totalUnlocks = analytics.totalUnlocks
                val totalScreenTime = analytics.totalScreenTime
                val sessionCount = analytics.sessions.size
                val topApp = analytics.appUsageMap.maxByOrNull { it.value }
                
                _uiState.value = _uiState.value.copy(
                    totalUsageMillis = totalScreenTime,
                    appLaunchCount = totalAppLaunches,
                    unlockCount = totalUnlocks,
                    sessionCount = sessionCount,
                    topAppPackage = topApp?.key ?: "",
                    topAppUsageMillis = topApp?.value ?: 0L,
                    dataLoaded = true
                )
                
                generateInsight()
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(dataLoaded = true)
            }
        }
    }

    /**
     * Fetch daily screen time for past 7 days from system API.
     * Uses queryAndAggregateUsageStats for each day — simple and accurate.
     */
    private fun load7DayHistory() {
        viewModelScope.launch {
            try {
                val history = mutableListOf<Long>()
                val cal = Calendar.getInstance()
                
                for (daysAgo in 6 downTo 0) {
                    val dayCal = cal.clone() as Calendar
                    dayCal.add(Calendar.DAY_OF_YEAR, -daysAgo)
                    dayCal.set(Calendar.HOUR_OF_DAY, 0)
                    dayCal.set(Calendar.MINUTE, 0)
                    dayCal.set(Calendar.SECOND, 0)
                    dayCal.set(Calendar.MILLISECOND, 0)
                    
                    val dayStart = dayCal.timeInMillis
                    val dayEnd = if (daysAgo == 0) {
                        System.currentTimeMillis()
                    } else {
                        dayStart + 24 * 60 * 60 * 1000 - 1
                    }
                    
                    try {
                        // queryAndAggregateUsageStats directly — same as Digital Wellbeing
                        val statsMap = usageStatsDataSource.getUsageStats(dayStart, dayEnd)
                        val dayTotal = statsMap.values.filter { it > 0 }.sum()
                        history.add(dayTotal)
                    } catch (_: Exception) {
                        history.add(0L)
                    }
                }
                
                _uiState.value = _uiState.value.copy(weeklyUsageHistory = history)
                generateInsight()
            } catch (_: Exception) { }
        }
    }

    /**
     * Generate personalized insight based on REAL system data.
     * Guards against insufficient data (early morning, no history, etc.)
     */
    private fun generateInsight() {
        val state = _uiState.value
        if (!state.dataLoaded) return
        
        val totalMs = state.totalUsageMillis
        val totalHours = totalMs / 3600000f
        val goalProgress = totalHours / 4f
        val unlocks = state.unlockCount
        val launches = state.appLaunchCount
        val sessions = state.sessionCount
        val history = state.weeklyUsageHistory
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val minuteOfDay = hour * 60 + Calendar.getInstance().get(Calendar.MINUTE)

        // Only compute comparisons with sufficient historical data
        val hasHistory = history.size >= 2 && history.any { it > 0 }
        val yesterdayMs = if (history.size >= 2) history[history.size - 2] else 0L
        val weekAvgMs = if (hasHistory) {
            val pastDays = history.dropLast(1).filter { it > 0 }
            if (pastDays.isNotEmpty()) pastDays.average().toLong() else 0L
        } else 0L
        
        val has3DayTrend = history.size >= 4 && history.subList(history.size - 4, history.size - 1).all { it > 0 }
        val isTrendingDown = has3DayTrend && history.subList(history.size - 4, history.size - 1).let {
            it[0] > it[1] && it[1] > it[2]
        }
        val isTrendingUp = has3DayTrend && history.subList(history.size - 4, history.size - 1).let {
            it[0] < it[1] && it[1] < it[2]
        }
        val tooEarlyForComparison = minuteOfDay < 120

        val insight = when {
            // ── Insufficient data ──
            totalMs == 0L && hour < 8 ->
                "🌅 Fresh morning — no screen time yet!"

            totalMs == 0L ->
                "📊 No usage data — check that usage access permission is enabled."

            // ── Morning (low usage) ──
            hour < 10 && totalMs < 900_000L ->
                "🌅 Only ${totalMs / 60000}min so far — great screen-free start!"

            hour < 12 && goalProgress < 0.2f ->
                "☀️ ${(totalHours * 60).toInt()}min used this morning — well on track."

            // ── Under goal ──
            goalProgress < 0.25f && hour > 12 ->
                "🏆 Impressive! Well under your daily goal this afternoon."

            goalProgress < 0.5f && hour >= 17 ->
                "🎯 Past 5 PM with under 2h used — strong willpower!"

            goalProgress < 0.5f && unlocks in 1..19 ->
                "🧘 Only $unlocks unlocks and under 2h — mindful day!"

            // ── Trending down ──
            isTrendingDown ->
                "📉 3 days of declining screen time — keep it going!"

            !tooEarlyForComparison && yesterdayMs > 3600_000 && totalMs < yesterdayMs * 0.7 && hour >= 15 ->
                "⬇️ ${((1 - totalMs.toFloat() / yesterdayMs) * 100).toInt()}% less than yesterday!"

            // ── Approaching goal ──
            goalProgress in 0.75f..0.95f ->
                "⚠️ ${(goalProgress * 100).toInt()}% of your 4h goal — maybe take a break?"

            goalProgress in 0.95f..1.0f ->
                "🔶 Almost at your limit — consider wrapping up."

            // ── Over goal ──
            goalProgress in 1.0f..1.25f ->
                "🔴 Over your daily goal — step away for a bit."

            goalProgress > 1.5f ->
                "🚨 ${String.format("%.1f", totalHours - 4)}h over your goal — rest your eyes."

            // ── High unlocks ──
            unlocks > 80 && hour >= 10 ->
                "📱 $unlocks unlocks! Try batching your phone checks."

            unlocks > 50 && hour in 10..17 ->
                "🔓 $unlocks unlocks — every ~${minuteOfDay / unlocks.coerceAtLeast(1)}min."

            // ── Sessions ──
            sessions > 30 && hour >= 10 ->
                "🔄 $sessions sessions — lots of app switching today."

            launches > 50 && hour >= 10 ->
                "🚀 $launches app launches — do you need them all?"

            // ── Evening ──
            hour >= 21 && totalMs > 3600_000 ->
                "🌙 ${String.format("%.1f", totalHours)}h today — wind down for sleep."

            hour >= 22 ->
                "😴 Late night! Time to put the phone away."

            // ── Trending up ──
            isTrendingUp ->
                "📈 Usage climbing for 3 days — let's reverse it!"

            // ── Weekly comparison ──
            !tooEarlyForComparison && weekAvgMs > 3600_000 && totalMs > weekAvgMs * 1.3 && hour >= 14 ->
                "📊 ${((totalMs.toFloat() / weekAvgMs - 1) * 100).toInt()}% above your weekly average."

            !tooEarlyForComparison && weekAvgMs > 3600_000 && totalMs < weekAvgMs * 0.7 && hour >= 14 ->
                "⭐ ${((1 - totalMs.toFloat() / weekAvgMs) * 100).toInt()}% below your weekly average!"

            else ->
                "Let's make today productive! 💪"
        }
        
        _uiState.value = _uiState.value.copy(insightText = insight)
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
    
    fun getProfileApps(profileId: Long): kotlinx.coroutines.flow.Flow<List<com.mindfulscrolling.app.data.local.entity.ProfileAppCrossRef>> {
        return appRepository.getProfileApps(profileId)
    }
}

data class DashboardUiState(
    val usageLogs: List<UsageLogEntity> = emptyList(),
    val totalUsageMillis: Long = 0,
    val isBreakActive: Boolean = false,
    val breakEndTime: Long = 0,
    val installedApps: List<com.mindfulscrolling.app.domain.model.AppInfo> = emptyList(),
    val profiles: List<com.mindfulscrolling.app.data.local.entity.FocusProfileEntity> = emptyList(),
    val allPermissionsGranted: Boolean = false,
    val appLaunchCount: Int = 0,
    val unlockCount: Int = 0,
    val sessionCount: Int = 0,
    val weeklyUsageHistory: List<Long> = emptyList(),
    val insightText: String = "",
    val topAppPackage: String = "",
    val topAppUsageMillis: Long = 0L,
    val dataLoaded: Boolean = false
)
