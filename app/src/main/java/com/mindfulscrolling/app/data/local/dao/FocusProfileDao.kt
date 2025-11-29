package com.mindfulscrolling.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.mindfulscrolling.app.data.local.entity.FocusProfileEntity
import com.mindfulscrolling.app.data.local.entity.ProfileAppCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusProfileDao {
    @Query("SELECT * FROM focus_profiles")
    fun getAllProfiles(): Flow<List<FocusProfileEntity>>

    @Query("SELECT * FROM focus_profiles WHERE id = :id")
    suspend fun getProfileById(id: Long): FocusProfileEntity?

    @Query("SELECT * FROM focus_profiles WHERE isActive = 1")
    fun getActiveProfiles(): Flow<List<FocusProfileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: FocusProfileEntity): Long

    @Update
    suspend fun updateProfile(profile: FocusProfileEntity)

    @Delete
    suspend fun deleteProfile(profile: FocusProfileEntity)

    // Profile App Relations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfileAppCrossRef(crossRef: ProfileAppCrossRef)

    @Query("DELETE FROM profile_app_cross_ref WHERE profileId = :profileId")
    suspend fun deleteProfileApps(profileId: Long)

    @Transaction
    suspend fun updateProfileApps(profileId: Long, apps: List<ProfileAppCrossRef>) {
        deleteProfileApps(profileId)
        apps.forEach { insertProfileAppCrossRef(it) }
    }

    @Query("SELECT * FROM profile_app_cross_ref WHERE profileId = :profileId")
    fun getProfileApps(profileId: Long): Flow<List<ProfileAppCrossRef>>
    
    @Query("SELECT * FROM profile_app_cross_ref WHERE profileId IN (SELECT id FROM focus_profiles WHERE isActive = 1)")
    fun getActiveProfileApps(): Flow<List<ProfileAppCrossRef>>

    @Query("UPDATE focus_profiles SET isActive = 0")
    suspend fun deactivateAllProfiles()

    @Transaction
    suspend fun activateProfile(profileId: Long) {
        // deactivateAllProfiles() // Removed to allow multiple profiles
        setProfileActive(profileId, true)
    }

    @Query("UPDATE focus_profiles SET isActive = :isActive WHERE id = :profileId")
    suspend fun setProfileActive(profileId: Long, isActive: Boolean)

    @Query("SELECT * FROM profile_app_cross_ref WHERE packageName = :packageName AND profileId IN (SELECT id FROM focus_profiles WHERE isActive = 1)")
    suspend fun getLimitsForAppInActiveProfiles(packageName: String): List<ProfileAppCrossRef>

    @Query("SELECT * FROM focus_profiles INNER JOIN profile_app_cross_ref ON focus_profiles.id = profile_app_cross_ref.profileId WHERE profile_app_cross_ref.packageName = :packageName")
    suspend fun getProfileLimitsForApp(packageName: String): List<ProfileWithLimit>
}

data class ProfileWithLimit(
    @androidx.room.Embedded val profile: FocusProfileEntity,
    @androidx.room.Embedded val limit: ProfileAppCrossRef
)
