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

import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import android.content.pm.PackageManager

class AppRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appLimitDao: AppLimitDao,
    private val appGroupDao: AppGroupDao,
    private val usageLogDao: UsageLogDao,
    private val overrideLogDao: OverrideLogDao,
    private val focusProfileDao: com.mindfulscrolling.app.data.local.dao.FocusProfileDao,
    private val breakPreferences: com.mindfulscrolling.app.data.local.BreakPreferences
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
    override suspend fun getLastOverrideForApp(packageName: String): OverrideLogEntity? = overrideLogDao.getLastOverrideForApp(packageName)

    override suspend fun getInstalledApps(): List<com.mindfulscrolling.app.domain.model.AppInfo> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val packageManager = context.packageManager
        packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { appInfo ->
                packageManager.getLaunchIntentForPackage(appInfo.packageName) != null
            }
            .map { appInfo ->
                com.mindfulscrolling.app.domain.model.AppInfo(
                    name = packageManager.getApplicationLabel(appInfo).toString(),
                    packageName = appInfo.packageName
                )
            }
            .sortedBy { it.name }
    }

    // Focus Profiles
    override fun getAllProfiles(): Flow<List<com.mindfulscrolling.app.data.local.entity.FocusProfileEntity>> = focusProfileDao.getAllProfiles()
    override suspend fun getProfileById(id: Long): com.mindfulscrolling.app.data.local.entity.FocusProfileEntity? = focusProfileDao.getProfileById(id)
    override fun getActiveProfiles(): Flow<List<com.mindfulscrolling.app.data.local.entity.FocusProfileEntity>> = focusProfileDao.getActiveProfiles()
    override suspend fun insertProfile(profile: com.mindfulscrolling.app.data.local.entity.FocusProfileEntity): Long = focusProfileDao.insertProfile(profile)
    override suspend fun updateProfile(profile: com.mindfulscrolling.app.data.local.entity.FocusProfileEntity) = focusProfileDao.updateProfile(profile)
    override suspend fun deleteProfile(profile: com.mindfulscrolling.app.data.local.entity.FocusProfileEntity) = focusProfileDao.deleteProfile(profile)
    override suspend fun activateProfile(profileId: Long) = focusProfileDao.activateProfile(profileId)
    override suspend fun deactivateProfile(profileId: Long) = focusProfileDao.setProfileActive(profileId, false)
    override suspend fun deactivateAllProfiles() = focusProfileDao.deactivateAllProfiles()

    // Profile Apps
    override fun getProfileApps(profileId: Long): Flow<List<com.mindfulscrolling.app.data.local.entity.ProfileAppCrossRef>> = focusProfileDao.getProfileApps(profileId)
    override suspend fun updateProfileApps(profileId: Long, apps: List<com.mindfulscrolling.app.data.local.entity.ProfileAppCrossRef>) = focusProfileDao.updateProfileApps(profileId, apps)
    override fun getActiveProfileApps(): Flow<List<com.mindfulscrolling.app.data.local.entity.ProfileAppCrossRef>> = focusProfileDao.getActiveProfileApps()
    override suspend fun getLimitsForAppInActiveProfiles(packageName: String): List<com.mindfulscrolling.app.data.local.entity.ProfileAppCrossRef> = focusProfileDao.getLimitsForAppInActiveProfiles(packageName)
    override suspend fun getProfileLimitsForApp(packageName: String): List<com.mindfulscrolling.app.data.local.dao.ProfileWithLimit> = focusProfileDao.getProfileLimitsForApp(packageName)

    // Take a Break
    override fun isBreakActive(): Flow<Boolean> = breakPreferences.isBreakActive
    override fun getBreakEndTime(): Flow<Long> = breakPreferences.breakEndTime
    override fun getBreakWhitelist(): Flow<Set<String>> = breakPreferences.breakWhitelist
    override suspend fun setBreakActive(active: Boolean, endTime: Long) = breakPreferences.setBreakActive(active, endTime)
    override suspend fun updateBreakWhitelist(whitelist: Set<String>) = breakPreferences.updateWhitelist(whitelist)
}
