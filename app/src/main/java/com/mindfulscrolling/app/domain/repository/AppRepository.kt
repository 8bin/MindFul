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

    // Focus Profiles
    fun getAllProfiles(): Flow<List<com.mindfulscrolling.app.data.local.entity.FocusProfileEntity>>
    suspend fun getProfileById(id: Long): com.mindfulscrolling.app.data.local.entity.FocusProfileEntity?
    fun getActiveProfile(): Flow<com.mindfulscrolling.app.data.local.entity.FocusProfileEntity?>
    suspend fun insertProfile(profile: com.mindfulscrolling.app.data.local.entity.FocusProfileEntity): Long
    suspend fun updateProfile(profile: com.mindfulscrolling.app.data.local.entity.FocusProfileEntity)
    suspend fun deleteProfile(profile: com.mindfulscrolling.app.data.local.entity.FocusProfileEntity)
    suspend fun activateProfile(profileId: Long)
    suspend fun deactivateAllProfiles()
    
    // Profile Apps
    fun getProfileApps(profileId: Long): Flow<List<com.mindfulscrolling.app.data.local.entity.ProfileAppCrossRef>>
    suspend fun updateProfileApps(profileId: Long, apps: List<com.mindfulscrolling.app.data.local.entity.ProfileAppCrossRef>)
    fun getActiveProfileApps(): Flow<List<com.mindfulscrolling.app.data.local.entity.ProfileAppCrossRef>>
    suspend fun getLimitForAppInActiveProfile(packageName: String): com.mindfulscrolling.app.data.local.entity.ProfileAppCrossRef?
}
