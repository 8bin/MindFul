package com.mindfulscrolling.app.data.datasource

import android.app.usage.UsageEvents
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import com.mindfulscrolling.app.domain.model.DailyAnalytics
import com.mindfulscrolling.app.domain.model.UsageSession
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject

/**
 * Wraps Android's UsageStatsManager to provide accurate usage data.
 *
 * KEY DESIGN DECISIONS:
 *
 * 1. Uses totalTimeVisible (API 29+) instead of totalTimeInForeground.
 *    - totalTimeInForeground counts time an app is in "foreground" state even when
 *      hidden behind overlays, popups, or split-screen windows — NOT what the user sees.
 *    - totalTimeVisible counts only time the app is ACTUALLY VISIBLE — this is what
 *      Android's Digital Wellbeing uses and what users expect.
 *    - Falls back to totalTimeInForeground on API 26-28 (rare).
 *
 * 2. Uses INTERVAL_DAILY + lastTimeUsed filtering to get exactly ONE day's data.
 *    - Without lastTimeUsed filter, INTERVAL_DAILY can return adjacent day buckets,
 *      causing data from yesterday to appear in today's totals.
 *
 * 3. Uses ACTIVITY_RESUMED/ACTIVITY_PAUSED events (API 29+) for hourly breakdown.
 *    - More accurate than MOVE_TO_FOREGROUND/MOVE_TO_BACKGROUND which fire for
 *      background services and overlays, not just actual user-visible activity.
 *    - Falls back to MOVE_TO_FOREGROUND/MOVE_TO_BACKGROUND on API < 29.
 *
 * 4. Normalizes hourly breakdown to match the authoritative daily total.
 *    - Events-based calculation can diverge from daily totals due to edge cases.
 *    - Scale hourly values proportionally so the graph matches the total exactly.
 */
class UsageStatsDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    private val packageManager = context.packageManager

    /**
     * Extract the correct "visible time" from a UsageStats entry.
     * API 29+: totalTimeVisible (matches Digital Wellbeing)
     * API <29: totalTimeInForeground (best available)
     */
    private fun UsageStats.getVisibleTime(): Long {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            totalTimeVisible
        } else {
            totalTimeInForeground
        }
    }

    /**
     * Get per-app screen time for a day.
     * Returns accurate visible time per package, filtered to only the requested day.
     */
    fun getUsageStats(startTime: Long, endTime: Long): Map<String, Long> {
        val statsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY, startTime, endTime
        )
        return statsList
            .filter { it.getVisibleTime() > 0 }
            .filter { it.lastTimeUsed >= startTime }
            .groupBy { it.packageName }
            .mapValues { (_, stats) -> stats.minOf { it.getVisibleTime() } }
    }

    /**
     * Get full analytics for a day: total time, per-app breakdown, hourly usage,
     * launches, unlocks, and session timeline.
     *
     * @param startTime midnight timestamp of the target day
     * @param endTime   end of the target day (or System.currentTimeMillis() for today)
     */
    fun getDailyAnalytics(startTime: Long, endTime: Long): DailyAnalytics {
        // ═══════════════════════════════════════════════════════════════════
        // 1. AUTHORITATIVE PER-APP TOTALS
        //    Uses INTERVAL_DAILY + totalTimeVisible for accurate per-app times
        //    that match Android's Digital Wellbeing.
        // ═══════════════════════════════════════════════════════════════════
        val statsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY, startTime, endTime
        )
        
        val authoritativeAppUsageMap = statsList
            .filter { it.getVisibleTime() > 0 }
            .filter { it.lastTimeUsed >= startTime }
            .filter { isUserFacingApp(it.packageName) }
            .groupBy { it.packageName }
            .mapValues { (_, stats) -> stats.minOf { it.getVisibleTime() } }

        val authoritativeTotalScreenTime = authoritativeAppUsageMap.values.sum()
        val validPackages = authoritativeAppUsageMap.keys

        // ═══════════════════════════════════════════════════════════════════
        // 2. GRANULAR EVENT DATA (hourly charts, sessions, launches, unlocks)
        //    Uses queryEvents for timeline data. On API 29+, prefers
        //    ACTIVITY_RESUMED/PAUSED for accuracy.
        // ═══════════════════════════════════════════════════════════════════
        val events = usageStatsManager.queryEvents(startTime, endTime)
        
        val hourlyUsageMap = mutableMapOf<Int, Long>()
        val appLaunchMap = mutableMapOf<String, Int>()
        val hourlyLaunchMap = mutableMapOf<Int, Int>()
        val hourlyUnlockMap = mutableMapOf<Int, Int>()
        val sessions = mutableListOf<UsageSession>()
        
        var totalUnlocks = 0

        // Determine which event types to use
        val useActivityEvents = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        val fgEventType = if (useActivityEvents) UsageEvents.Event.ACTIVITY_RESUMED else UsageEvents.Event.MOVE_TO_FOREGROUND
        val bgEventType = if (useActivityEvents) UsageEvents.Event.ACTIVITY_PAUSED else UsageEvents.Event.MOVE_TO_BACKGROUND

        val startMap = mutableMapOf<String, Long>()
        val event = UsageEvents.Event()

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val packageName = event.packageName
            val timeStamp = event.timeStamp
            
            val c = Calendar.getInstance()
            c.timeInMillis = timeStamp
            val hour = c.get(Calendar.HOUR_OF_DAY)

            // Count screen unlocks
            if (event.eventType == UsageEvents.Event.KEYGUARD_HIDDEN) {
                totalUnlocks++
                hourlyUnlockMap[hour] = hourlyUnlockMap.getOrDefault(hour, 0) + 1
                continue
            }

            // Only process events for apps in our authoritative list
            if (!validPackages.contains(packageName)) continue

            if (event.eventType == fgEventType) {
                startMap[packageName] = timeStamp
                
                appLaunchMap[packageName] = appLaunchMap.getOrDefault(packageName, 0) + 1
                hourlyLaunchMap[hour] = hourlyLaunchMap.getOrDefault(hour, 0) + 1
                
            } else if (event.eventType == bgEventType) {
                val start = startMap.remove(packageName) ?: continue
                val duration = timeStamp - start
                
                if (duration > 0) {
                    sessions.add(UsageSession(packageName, start, timeStamp))
                    addDurationToHourlyMap(hourlyUsageMap, start, timeStamp)
                }
            }
        }

        // Handle apps still in foreground (clamp to endTime)
        startMap.forEach { (packageName, start) ->
             if (start < endTime) {
                 val duration = endTime - start
                 if (duration > 0) {
                    sessions.add(UsageSession(packageName, start, endTime))
                    addDurationToHourlyMap(hourlyUsageMap, start, endTime)
                 }
             }
        }

        // ═══════════════════════════════════════════════════════════════════
        // 3. NORMALIZE hourly breakdown to match authoritative total
        //    Events can diverge from daily totals. Scale proportionally
        //    so the hourly graph accurately represents the reported total.
        // ═══════════════════════════════════════════════════════════════════
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

    /**
     * Split a foreground session duration across hourly buckets.
     * A session from 10:45 to 11:15 contributes 15min to hour-10 and 15min to hour-11.
     */
    private fun addDurationToHourlyMap(hourlyMap: MutableMap<Int, Long>, start: Long, end: Long) {
        var currentStart = start
        while (currentStart < end) {
            val cStart = Calendar.getInstance().apply { timeInMillis = currentStart }
            val currentHour = cStart.get(Calendar.HOUR_OF_DAY)
            
            val endOfHour = Calendar.getInstance().apply {
                timeInMillis = currentStart
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis
            
            val segmentEnd = kotlin.math.min(end, endOfHour)
            val segmentDuration = segmentEnd - currentStart
            
            if (segmentDuration > 0) {
                hourlyMap[currentHour] = hourlyMap.getOrDefault(currentHour, 0L) + segmentDuration
            }
            
            currentStart = segmentEnd + 1
        }
    }

    /**
     * Check if a package is a user-facing app (has a launcher icon).
     * Filters out system services, background processes, and overlays.
     */
    private fun isUserFacingApp(packageName: String): Boolean {
        return try {
            packageManager.getLaunchIntentForPackage(packageName) != null
        } catch (_: Exception) {
            false
        }
    }
}
