package com.mindfulscrolling.app.data.datasource

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import com.mindfulscrolling.app.domain.model.DailyAnalytics
import com.mindfulscrolling.app.domain.model.UsageSession
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject

class UsageStatsDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    private val packageManager = context.packageManager

    /**
     * Get per-app screen time using INTERVAL_DAILY — matches Digital Wellbeing exactly.
     * 
     * queryAndAggregateUsageStats() inflates totals by summing across overlapping
     * internal interval buckets. queryUsageStats(INTERVAL_DAILY) returns Android's
     * pre-computed daily totals, which is what Digital Wellbeing uses.
     */
    fun getUsageStats(startTime: Long, endTime: Long): Map<String, Long> {
        val statsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY, startTime, endTime
        )
        // Group by package, take max totalTimeInForeground per package
        // (Android may return multiple entries for overlapping day boundaries)
        return statsList
            .filter { it.totalTimeInForeground > 0 }
            .groupBy { it.packageName }
            .mapValues { (_, stats) -> stats.maxOf { it.totalTimeInForeground } }
    }

    fun getDailyAnalytics(startTime: Long, endTime: Long): DailyAnalytics {
        // 1. Authoritative Totals — using INTERVAL_DAILY to match Digital Wellbeing
        // queryAndAggregateUsageStats inflates totals by summing overlapping interval buckets.
        // queryUsageStats(INTERVAL_DAILY) returns Android's pre-computed daily totals.
        val statsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY, startTime, endTime
        )
        
        // Group by package, take max foreground time (handles duplicate entries)
        val authoritativeAppUsageMap = statsList
            .filter { it.totalTimeInForeground > 0 }
            .filter { packageManager.getLaunchIntentForPackage(it.packageName) != null }
            .groupBy { it.packageName }
            .mapValues { (_, stats) -> stats.maxOf { it.totalTimeInForeground } }

        val authoritativeTotalScreenTime = authoritativeAppUsageMap.values.sum()
        val validPackages = authoritativeAppUsageMap.keys

        // 2. Granular Structure (Graph/Timeline)
        // tailored to match the Authoritative Totals
        val events = usageStatsManager.queryEvents(startTime, endTime)
        
        val hourlyUsageMap = mutableMapOf<Int, Long>()
        val appLaunchMap = mutableMapOf<String, Int>()
        val hourlyLaunchMap = mutableMapOf<Int, Int>()
        val hourlyUnlockMap = mutableMapOf<Int, Int>()
        val sessions = mutableListOf<UsageSession>()
        
        var totalUnlocks = 0

        val startMap = mutableMapOf<String, Long>()
        val event = UsageEvents.Event()

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val packageName = event.packageName
            val timeStamp = event.timeStamp
            
            val c = Calendar.getInstance()
            c.timeInMillis = timeStamp
            val hour = c.get(Calendar.HOUR_OF_DAY)

            if (event.eventType == UsageEvents.Event.KEYGUARD_HIDDEN) {
                totalUnlocks++
                hourlyUnlockMap[hour] = hourlyUnlockMap.getOrDefault(hour, 0) + 1
                continue
            }

            // FILTER: Only process events for apps that are in our Authoritative List.
            // This ensures the Graph and Timeline do not show "MyJio" or other background processes 
            // that were filtered out of the Aggregate stats.
            if (!validPackages.contains(packageName)) continue

            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                startMap[packageName] = timeStamp
                
                appLaunchMap[packageName] = appLaunchMap.getOrDefault(packageName, 0) + 1
                hourlyLaunchMap[hour] = hourlyLaunchMap.getOrDefault(hour, 0) + 1
                
            } else if (event.eventType == UsageEvents.Event.MOVE_TO_BACKGROUND) {
                val start = startMap.remove(packageName) ?: continue
                val duration = timeStamp - start
                
                if (duration > 0) {
                    sessions.add(UsageSession(packageName, start, timeStamp))
                    
                    var currentStart = start
                    while (currentStart < timeStamp) {
                        val cStart = Calendar.getInstance().apply { timeInMillis = currentStart }
                        val currentHour = cStart.get(Calendar.HOUR_OF_DAY)
                        
                        val cEnd = Calendar.getInstance().apply { 
                            timeInMillis = currentStart
                            set(Calendar.MINUTE, 59)
                            set(Calendar.SECOND, 59)
                            set(Calendar.MILLISECOND, 999)
                        }
                        val endOfHour = cEnd.timeInMillis
                        
                        val segmentEnd = kotlin.math.min(timeStamp, endOfHour)
                        val segmentDuration = segmentEnd - currentStart
                        
                        if (segmentDuration > 0) {
                            hourlyUsageMap[currentHour] = hourlyUsageMap.getOrDefault(currentHour, 0L) + segmentDuration
                        }
                        
                        currentStart = segmentEnd + 1
                    }
                }
            }
        }

        // Handle apps still in foreground (clamp to endTime)
        startMap.forEach { (packageName, start) ->
             if (start < endTime) {
                 val duration = endTime - start
                 if (duration > 0) {
                    sessions.add(UsageSession(packageName, start, endTime))
                    
                    val c = Calendar.getInstance()
                    c.timeInMillis = endTime
                    val hour = c.get(Calendar.HOUR_OF_DAY)
                    hourlyUsageMap[hour] = hourlyUsageMap.getOrDefault(hour, 0L) + duration
                 }
             }
        }

        // ── Normalize hourly breakdown to match authoritative total ──────
        // Events-based hourly aggregation can diverge from queryAndAggregateUsageStats
        // (overlapping sessions, background processes, etc.). Scale proportionally.
        val rawHourlyTotal = hourlyUsageMap.values.sum()
        val normalizedHourlyMap = if (rawHourlyTotal > 0 && authoritativeTotalScreenTime > 0) {
            val scale = authoritativeTotalScreenTime.toDouble() / rawHourlyTotal.toDouble()
            hourlyUsageMap.mapValues { (_, v) -> (v * scale).toLong() }
        } else {
            hourlyUsageMap.toMap()
        }

        return DailyAnalytics(
            authoritativeTotalScreenTime,
            totalUnlocks,
            authoritativeAppUsageMap, 
            normalizedHourlyMap,
            appLaunchMap,
            hourlyLaunchMap,
            hourlyUnlockMap,
            sessions.sortedByDescending { it.startTime }
        )
    }
}
