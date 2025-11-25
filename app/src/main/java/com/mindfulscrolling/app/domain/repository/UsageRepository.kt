package com.mindfulscrolling.app.domain.repository

import com.mindfulscrolling.app.data.local.entity.UsageLogEntity
import kotlinx.coroutines.flow.Flow

interface UsageRepository {
    fun getUsageForDate(date: Long): Flow<List<UsageLogEntity>>
    suspend fun logUsage(packageName: String, duration: Long, date: Long)
    suspend fun getUsageForApp(packageName: String, date: Long): UsageLogEntity?
}
