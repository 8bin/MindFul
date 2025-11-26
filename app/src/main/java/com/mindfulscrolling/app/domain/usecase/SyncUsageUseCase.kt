package com.mindfulscrolling.app.domain.usecase

import com.mindfulscrolling.app.data.datasource.UsageStatsDataSource
import com.mindfulscrolling.app.domain.repository.UsageRepository
import java.util.Calendar
import javax.inject.Inject

class SyncUsageUseCase @Inject constructor(
    private val usageStatsDataSource: UsageStatsDataSource,
    private val usageRepository: UsageRepository
) {
    suspend operator fun invoke() {
        val (startTime, endTime) = getTodayRange()
        val stats = usageStatsDataSource.getUsageStats(startTime, endTime)
        usageRepository.syncUsage(stats, startTime)
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
