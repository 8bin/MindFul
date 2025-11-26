package com.mindfulscrolling.app.domain.repository

import com.mindfulscrolling.app.data.local.entity.AppGroupEntity
import com.mindfulscrolling.app.data.local.entity.AppLimitEntity
import com.mindfulscrolling.app.data.local.entity.OverrideLogEntity
import com.mindfulscrolling.app.data.local.entity.UsageLogEntity
import kotlinx.coroutines.flow.Flow

interface AppRepository {
    // Limits
    fun getAllLimits(): Flow<List<AppLimitEntity>>
    suspend fun getLimitForApp(packageName: String): AppLimitEntity?
    suspend fun insertLimit(limit: AppLimitEntity)
    suspend fun deleteLimit(packageName: String)

    // Groups
    fun getAllGroups(): Flow<List<AppGroupEntity>>
    suspend fun insertGroup(group: AppGroupEntity)
    suspend fun deleteGroup(groupName: String)

    // Usage
    fun getUsageForDate(date: Long): Flow<List<UsageLogEntity>>
    suspend fun getUsageForAppAndDate(packageName: String, date: Long): UsageLogEntity?
    suspend fun insertOrUpdateUsage(usage: UsageLogEntity)

    // Overrides
    suspend fun logOverride(override: OverrideLogEntity)
    fun getAllOverrides(): Flow<List<OverrideLogEntity>>

    // System
    suspend fun getInstalledApps(): List<com.mindfulscrolling.app.domain.model.AppInfo>
}
