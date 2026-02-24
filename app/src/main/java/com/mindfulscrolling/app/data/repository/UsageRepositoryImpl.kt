package com.mindfulscrolling.app.data.repository

import com.mindfulscrolling.app.data.local.dao.UsageLogDao
import com.mindfulscrolling.app.data.local.entity.UsageLogEntity
import com.mindfulscrolling.app.domain.repository.UsageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UsageRepositoryImpl @Inject constructor(
    private val usageLogDao: UsageLogDao,
    private val usageStatsDataSource: com.mindfulscrolling.app.data.datasource.UsageStatsDataSource
) : UsageRepository {

    override fun getUsageForDate(date: Long): Flow<List<UsageLogEntity>> {
        return usageLogDao.getUsageForDate(date)
    }

    override fun getUsageHistory(): Flow<List<com.mindfulscrolling.app.data.local.model.DailyUsageSummary>> {
        return usageLogDao.getDailyTotals()
    }

    override suspend fun logUsage(packageName: String, duration: Long, date: Long) {
        val currentLog = usageLogDao.getUsageForAppAndDate(packageName, date)
        val newDuration = (currentLog?.durationMillis ?: 0) + duration
        val log = UsageLogEntity(
            id = currentLog?.id ?: 0,
            packageName = packageName,
            date = date,
            durationMillis = newDuration,
            lastUpdated = System.currentTimeMillis()
        )
        usageLogDao.insertOrUpdateUsage(log)
    }

    override suspend fun getUsageForApp(packageName: String, date: Long): UsageLogEntity? {
        return usageLogDao.getUsageForAppAndDate(packageName, date)
    }

    override suspend fun syncUsage(usageStats: Map<String, Long>, date: Long) {
        usageStats.forEach { (packageName, systemDuration) ->
            if (systemDuration > 0) {
                val currentLog = usageLogDao.getUsageForAppAndDate(packageName, date)
                val localDuration = currentLog?.durationMillis ?: 0
                
                // Trust system stats if they are larger OR if local data is corrupt (> 24h)
                if (systemDuration > localDuration || localDuration > 86400000L) {
                    val log = UsageLogEntity(
                        id = currentLog?.id ?: 0,
                        packageName = packageName,
                        date = date,
                        durationMillis = systemDuration,
                        lastUpdated = System.currentTimeMillis()
                    )
                    usageLogDao.insertOrUpdateUsage(log)
                }
            }
        }
    }

    override suspend fun getDailyAnalytics(date: Long): com.mindfulscrolling.app.domain.model.DailyAnalytics {
        val start = date
        val endOfDay = date + 24 * 60 * 60 * 1000 - 1
        // Clamp to current time for today — don't query into the future
        val end = minOf(endOfDay, System.currentTimeMillis())
        return usageStatsDataSource.getDailyAnalytics(start, end)
    }
}
