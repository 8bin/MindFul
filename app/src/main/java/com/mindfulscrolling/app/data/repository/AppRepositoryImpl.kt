package com.mindfulscrolling.app.data.repository

import com.mindfulscrolling.app.data.local.dao.AppGroupDao
import com.mindfulscrolling.app.data.local.dao.AppLimitDao
import com.mindfulscrolling.app.data.local.dao.OverrideLogDao
import com.mindfulscrolling.app.data.local.dao.UsageLogDao
import com.mindfulscrolling.app.data.local.entity.AppGroupEntity
import com.mindfulscrolling.app.data.local.entity.AppLimitEntity
import com.mindfulscrolling.app.data.local.entity.OverrideLogEntity
import com.mindfulscrolling.app.data.local.entity.UsageLogEntity
import com.mindfulscrolling.app.domain.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AppRepositoryImpl @Inject constructor(
    private val appLimitDao: AppLimitDao,
    private val appGroupDao: AppGroupDao,
    private val usageLogDao: UsageLogDao,
    private val overrideLogDao: OverrideLogDao
) : AppRepository {

    override fun getAllLimits(): Flow<List<AppLimitEntity>> = appLimitDao.getAllLimits()
    override suspend fun getLimitForApp(packageName: String): AppLimitEntity? = appLimitDao.getLimitForApp(packageName)
    override suspend fun insertLimit(limit: AppLimitEntity) = appLimitDao.insertLimit(limit)
    override suspend fun deleteLimit(packageName: String) = appLimitDao.deleteLimit(packageName)

    override fun getAllGroups(): Flow<List<AppGroupEntity>> = appGroupDao.getAllGroups()
    override suspend fun insertGroup(group: AppGroupEntity) = appGroupDao.insertGroup(group)
    override suspend fun deleteGroup(groupName: String) = appGroupDao.deleteGroup(groupName)

    override fun getUsageForDate(date: Long): Flow<List<UsageLogEntity>> = usageLogDao.getUsageForDate(date)
    override suspend fun getUsageForAppAndDate(packageName: String, date: Long): UsageLogEntity? = usageLogDao.getUsageForAppAndDate(packageName, date)
    override suspend fun insertOrUpdateUsage(usage: UsageLogEntity) = usageLogDao.insertOrUpdateUsage(usage)

    override suspend fun logOverride(override: OverrideLogEntity) = overrideLogDao.insert(override)
    override fun getAllOverrides(): Flow<List<OverrideLogEntity>> = overrideLogDao.getAllOverrides()
}
