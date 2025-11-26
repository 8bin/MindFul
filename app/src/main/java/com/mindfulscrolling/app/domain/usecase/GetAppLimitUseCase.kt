package com.mindfulscrolling.app.domain.usecase

import com.mindfulscrolling.app.data.local.entity.AppLimitEntity
import com.mindfulscrolling.app.domain.repository.AppRepository
import javax.inject.Inject

class GetAppLimitUseCase @Inject constructor(
    private val appRepository: AppRepository
) {
    suspend operator fun invoke(packageName: String): AppLimitEntity? {
        val profileLimit = appRepository.getLimitForAppInActiveProfile(packageName)
        if (profileLimit != null) {
            // -1 means unlimited (whitelist behavior for this app in this profile)
            if (profileLimit.limitDurationMinutes == -1L) {
                return null
            }
            return AppLimitEntity(
                packageName = packageName,
                limitDurationMinutes = profileLimit.limitDurationMinutes.toInt(),
                isGroupLimit = false
            )
        }
        return appRepository.getLimitForApp(packageName)
    }
}
