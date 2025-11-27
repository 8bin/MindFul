package com.mindfulscrolling.app.data.datasource

import android.app.usage.UsageStatsManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject

class UsageStatsDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    fun getUsageStats(startTime: Long, endTime: Long): Map<String, Long> {
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )

        // Aggregate hourly stats by package name
        val aggregatedStats = mutableMapOf<String, Long>()
        stats.forEach { usageStat ->
            val currentTotal = aggregatedStats.getOrDefault(usageStat.packageName, 0L)
            aggregatedStats[usageStat.packageName] = currentTotal + usageStat.totalTimeInForeground
        }

        return aggregatedStats
    }
}
