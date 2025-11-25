package com.mindfulscrolling.app.domain.usecase

import com.mindfulscrolling.app.data.local.entity.AppGroupEntity
import com.mindfulscrolling.app.domain.repository.AppRepository
import javax.inject.Inject

class CreateAppGroupUseCase @Inject constructor(
    private val appRepository: AppRepository
) {
    suspend operator fun invoke(groupName: String, limitMinutes: Int) {
        val group = AppGroupEntity(
            groupName = groupName,
            limitDurationMinutes = limitMinutes
        )
        appRepository.insertGroup(group)
    }
}
