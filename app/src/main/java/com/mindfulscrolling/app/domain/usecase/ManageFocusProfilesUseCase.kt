package com.mindfulscrolling.app.domain.usecase

import com.mindfulscrolling.app.data.local.entity.FocusProfileEntity
import com.mindfulscrolling.app.data.local.entity.ProfileAppCrossRef
import com.mindfulscrolling.app.domain.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ManageFocusProfilesUseCase @Inject constructor(
    private val repository: AppRepository
) {
    fun getAllProfiles(): Flow<List<FocusProfileEntity>> = repository.getAllProfiles()

    suspend fun getProfileById(id: Long): FocusProfileEntity? = repository.getProfileById(id)

    fun getActiveProfiles(): Flow<List<FocusProfileEntity>> = repository.getActiveProfiles()

    suspend fun createProfile(name: String, icon: String): Long {
        return repository.insertProfile(FocusProfileEntity(name = name, icon = icon))
    }

    suspend fun updateProfile(profile: FocusProfileEntity) {
        repository.updateProfile(profile)
    }

    suspend fun deleteProfile(profile: FocusProfileEntity) {
        repository.deleteProfile(profile)
    }

    suspend fun activateProfile(profileId: Long) {
        repository.activateProfile(profileId)
    }

    suspend fun deactivateProfile(profileId: Long) {
        repository.deactivateProfile(profileId)
    }

    suspend fun deactivateAllProfiles() {
        repository.deactivateAllProfiles()
    }

    fun getProfileApps(profileId: Long): Flow<List<ProfileAppCrossRef>> = repository.getProfileApps(profileId)

    suspend fun updateProfileApps(profileId: Long, apps: List<ProfileAppCrossRef>) {
        repository.updateProfileApps(profileId, apps)
    }
}
