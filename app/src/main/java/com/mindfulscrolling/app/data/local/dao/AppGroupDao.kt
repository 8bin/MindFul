package com.mindfulscrolling.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mindfulscrolling.app.data.local.entity.AppGroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppGroupDao {
    @Query("SELECT * FROM app_groups")
    fun getAllGroups(): Flow<List<AppGroupEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: AppGroupEntity)

    @Query("DELETE FROM app_groups WHERE groupName = :groupName")
    suspend fun deleteGroup(groupName: String)
}
