package com.mindfulscrolling.app.domain.usecase

import com.mindfulscrolling.app.domain.repository.AppRepository
import javax.inject.Inject

class CheckLimitExceededUseCase @Inject constructor(
    private val appRepository: AppRepository
) {
    suspend operator fun invoke(packageName: String, date: Long): Boolean {
        // 0. Check for Active Override
        val lastOverride = appRepository.getLastOverrideForApp(packageName)
        if (lastOverride != null) {
            val endTime = lastOverride.timestamp + (lastOverride.durationMinutes * 60 * 1000)
            if (System.currentTimeMillis() < endTime) {
                return false // Override active, do not block
            }
        }

        // 1. Check Active Profile Limits
        val profileLimits = appRepository.getLimitsForAppInActiveProfiles(packageName)
        
        // If any profile blocks it (limit 0), it's blocked
        if (profileLimits.any { it.limitDurationMinutes == 0L }) {
            return true
        }

        // Find the strictest limit (smallest positive number)
        val strictLimit = profileLimits
            .filter { it.limitDurationMinutes > 0 }
            .minByOrNull { it.limitDurationMinutes }

        if (strictLimit != null) {
             // Check usage against profile limit
            val usage = appRepository.getUsageForAppAndDate(packageName, date)
            val usageMinutes = (usage?.durationMillis ?: 0) / 1000 / 60
            return usageMinutes >= strictLimit.limitDurationMinutes
        }
        
        // If explicitly allowed (-1) in one profile but not blocked/limited in others, it's allowed?
        // Current logic: If present in ANY active profile, we respect that profile's rule.
        // If multiple profiles conflict: Block > Limit > Allow.
        // The above logic handles Block and Limit. 
        // If we are here, it means no profile blocks it, and no profile limits it.
        // If it is in profileLimits (meaning it's -1/Allow), we should probably allow it (skip global limits)?
        // OR should global limits still apply? 
        // User request implies profiles are "Focus" modes, so they likely override global settings.
        // If I explicitly "Allow" an app in a Focus Profile, it should probably bypass global limits.
        
        if (profileLimits.any { it.limitDurationMinutes == -1L }) {
            return false
        }

        // 2. Fallback to Global Limits
        val limit = appRepository.getLimitForApp(packageName) ?: return false
        val usage = appRepository.getUsageForAppAndDate(packageName, date) ?: return false
        
        val usageMinutes = usage.durationMillis / 1000 / 60
        return usageMinutes >= limit.limitDurationMinutes
    }
}
