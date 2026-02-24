package com.mindfulscrolling.app.domain.model

data class AnalyticsData(
    val totalUsageMillis: Long,
    val hourlyUsage: List<BarData>,
    val appUsageList: List<AppUsageItem>,
    val sessions: List<UsageSession>
)

data class BarData(
    val hour: Int, // 0-23
    val value: Long // Millis or Count
)

data class AppUsageItem(
    val packageName: String,
    val appName: String,
    val usageMillis: Long,
    val percentage: Float
)

data class UsageSession(
    val packageName: String,
    val startTime: Long,
    val endTime: Long
)

data class DailyAnalytics(
    val totalScreenTime: Long,
    val totalUnlocks: Int,
    val appUsageMap: Map<String, Long>,
    val hourlyUsageMap: Map<Int, Long>,
    val appLaunchMap: Map<String, Int>,
    val hourlyLaunchMap: Map<Int, Int>,
    val hourlyUnlockMap: Map<Int, Int>,
    val sessions: List<UsageSession>
)
