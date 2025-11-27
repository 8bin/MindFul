package com.mindfulscrolling.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mindfulscrolling.app.data.local.entity.UsageLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsageLogDao {
    @Query("SELECT * FROM usage_logs WHERE date = :date")
    fun getUsageForDate(date: Long): Flow<List<UsageLogEntity>>

    @Query("SELECT * FROM usage_logs WHERE packageName = :packageName AND date = :date")
    suspend fun getUsageForAppAndDate(packageName: String, date: Long): UsageLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUsage(usage: UsageLogEntity)

    @Query("SELECT date, SUM(durationMillis) as totalDuration FROM usage_logs GROUP BY date ORDER BY date DESC")
    fun getDailyTotals(): Flow<List<com.mindfulscrolling.app.data.local.model.DailyUsageSummary>>
}
