package com.mindfulscrolling.app.data.repository

import com.mindfulscrolling.app.data.local.dao.AppLimitDao
import com.mindfulscrolling.app.data.local.entity.AppLimitEntity
import com.mindfulscrolling.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val appLimitDao: AppLimitDao
) : SettingsRepository {

    override fun getAllLimits(): Flow<List<AppLimitEntity>> {
        return appLimitDao.getAllLimits()
    }

    override suspend fun setLimit(packageName: String, limitMinutes: Int) {
        appLimitDao.insertLimit(
            AppLimitEntity(
                packageName = packageName,
                limitDurationMinutes = limitMinutes
            )
        )
    }

    override suspend fun removeLimit(packageName: String) {
        appLimitDao.deleteLimit(packageName)
    }

    override suspend fun getLimit(packageName: String): AppLimitEntity? {
        return appLimitDao.getLimitForApp(packageName)
    }
}
