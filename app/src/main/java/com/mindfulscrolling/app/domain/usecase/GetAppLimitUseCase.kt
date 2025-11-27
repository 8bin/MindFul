package com.mindfulscrolling.app.domain.usecase

import com.mindfulscrolling.app.data.local.entity.AppLimitEntity
import com.mindfulscrolling.app.domain.repository.AppRepository
import javax.inject.Inject

class GetAppLimitUseCase @Inject constructor(
    private val appRepository: AppRepository
) {
    suspend operator fun invoke(packageName: String): AppLimitEntity? {
        val profileLimits = appRepository.getLimitsForAppInActiveProfiles(packageName)
        
        // 1. Blocked (0)
        if (profileLimits.any { it.limitDurationMinutes == 0L }) {
             return AppLimitEntity(packageName, 0, false)
        }

        // 2. Limited (>0) - Pick strictest
        val strictLimit = profileLimits
            .filter { it.limitDurationMinutes > 0 }
            .minByOrNull { it.limitDurationMinutes }
            
        if (strictLimit != null) {
            return AppLimitEntity(packageName, strictLimit.limitDurationMinutes.toInt(), false)
        }
        
        // 3. Allowed (-1) - If present and no other restrictions, return null (Unlimited)
        if (profileLimits.any { it.limitDurationMinutes == -1L }) {
            return null
        }
        return appRepository.getLimitForApp(packageName)
    }
}
