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
}
