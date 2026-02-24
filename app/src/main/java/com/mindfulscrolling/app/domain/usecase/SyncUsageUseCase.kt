package com.mindfulscrolling.app.domain.usecase

import com.mindfulscrolling.app.data.datasource.UsageStatsDataSource
import com.mindfulscrolling.app.domain.repository.UsageRepository
import java.util.Calendar
import javax.inject.Inject

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext

class SyncUsageUseCase @Inject constructor(
    private val usageStatsDataSource: UsageStatsDataSource,
    private val usageRepository: UsageRepository,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke() {
        val (todayStart, todayEnd) = getTodayRange()
        
        // Get Install Time
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val installTime = packageInfo.firstInstallTime
        
        // Use the later of TodayMidnight or InstallTime
        val startTime = kotlin.math.max(todayStart, installTime)
        
        // If we are still before "now" (i.e. app installed in past or today), sync.
        // If app installed tomorrow (impossible), don't sync.
        if (startTime < todayEnd) {
             val stats = usageStatsDataSource.getUsageStats(startTime, todayEnd)
             usageRepository.syncUsage(stats, todayStart)
        }
    }

    private fun getTodayRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val end = calendar.timeInMillis

        return Pair(start, end)
    }
}
