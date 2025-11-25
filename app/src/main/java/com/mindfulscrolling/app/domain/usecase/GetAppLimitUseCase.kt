package com.mindfulscrolling.app.domain.usecase

import com.mindfulscrolling.app.data.local.entity.AppLimitEntity
import com.mindfulscrolling.app.domain.repository.AppRepository
import javax.inject.Inject

class GetAppLimitUseCase @Inject constructor(
    private val appRepository: AppRepository
) {
    suspend operator fun invoke(packageName: String): AppLimitEntity? {
        return appRepository.getLimitForApp(packageName)
    }
}
