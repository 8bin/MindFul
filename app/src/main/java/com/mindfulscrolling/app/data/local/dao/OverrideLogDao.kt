package com.mindfulscrolling.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.mindfulscrolling.app.data.local.entity.OverrideLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OverrideLogDao {
    @Insert
    suspend fun insert(log: OverrideLogEntity)

    @Query("SELECT * FROM override_logs ORDER BY timestamp DESC")
    fun getAllOverrides(): Flow<List<OverrideLogEntity>>

    @Query("SELECT * FROM override_logs WHERE packageName = :packageName ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastOverrideForApp(packageName: String): OverrideLogEntity?
}
