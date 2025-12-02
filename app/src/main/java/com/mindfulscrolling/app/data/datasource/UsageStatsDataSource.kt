package com.mindfulscrolling.app.data.datasource

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class UsageStatsDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    private val packageManager = context.packageManager

    fun getUsageStats(startTime: Long, endTime: Long): Map<String, Long> {
        val events = usageStatsManager.queryEvents(startTime, endTime)
        val aggregatedStats = mutableMapOf<String, Long>()
        val startMap = mutableMapOf<String, Long>()

        val event = android.app.usage.UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val packageName = event.packageName

            // Filter out system apps/services by checking if they have a launch intent
            if (packageManager.getLaunchIntentForPackage(packageName) == null) {
                continue
            }

            if (event.eventType == android.app.usage.UsageEvents.Event.MOVE_TO_FOREGROUND) {
                startMap[packageName] = event.timeStamp
            } else if (event.eventType == android.app.usage.UsageEvents.Event.MOVE_TO_BACKGROUND) {
                val start = startMap.remove(packageName)
                if (start != null) {
                    val duration = event.timeStamp - start
                    val currentTotal = aggregatedStats.getOrDefault(packageName, 0L)
                    aggregatedStats[packageName] = currentTotal + duration
                } else {
                    // App was already open at startTime
                    // We can assume it was open since startTime, but to be safe and avoid overcounting
                    // if we missed the start event (e.g. phone restart), we might just ignore or clamp.
                    // For "Today" stats, clamping to startTime is reasonable.
                    val duration = event.timeStamp - startTime
                    if (duration > 0) {
                         val currentTotal = aggregatedStats.getOrDefault(packageName, 0L)
                         aggregatedStats[packageName] = currentTotal + duration
                    }
                }
            }
        }

        // Handle apps still in foreground
        startMap.forEach { (packageName, start) ->
             val duration = endTime - start
             val currentTotal = aggregatedStats.getOrDefault(packageName, 0L)
             aggregatedStats[packageName] = currentTotal + duration
        }

        return aggregatedStats
    }
}
