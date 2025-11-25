package com.mindfulscrolling.app.domain.repository

import com.mindfulscrolling.app.data.local.entity.AppLimitEntity
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getAllLimits(): Flow<List<AppLimitEntity>>
    suspend fun setLimit(packageName: String, limitMinutes: Int)
    suspend fun removeLimit(packageName: String)
    suspend fun getLimit(packageName: String): AppLimitEntity?
}
