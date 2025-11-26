package com.mindfulscrolling.app.data.repository

import com.mindfulscrolling.app.data.local.dao.UsageLogDao
import com.mindfulscrolling.app.data.local.entity.UsageLogEntity
import com.mindfulscrolling.app.domain.repository.UsageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UsageRepositoryImpl @Inject constructor(
    private val usageLogDao: UsageLogDao
) : UsageRepository {

    override fun getUsageForDate(date: Long): Flow<List<UsageLogEntity>> {
        return usageLogDao.getUsageForDate(date)
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
                
                // If system stats show more usage, trust system stats (it's the source of truth)
                // But we also need to be careful not to overwrite if we have more granular data.
                // For now, let's take the max.
                if (systemDuration > localDuration) {
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
}
