package com.mindfulscrolling.app.domain.usecase

import com.mindfulscrolling.app.data.local.entity.AppLimitEntity
import com.mindfulscrolling.app.domain.repository.AppRepository
import javax.inject.Inject

class GetAppLimitUseCase @Inject constructor(
    private val appRepository: AppRepository
) {
    suspend operator fun invoke(packageName: String): AppLimitEntity? {
        val profileLimits = appRepository.getProfileLimitsForApp(packageName)
        
        // Filter for effective profiles (Manual OR Scheduled)
        val activeLimits = profileLimits.filter { isProfileActive(it.profile) }.map { it.limit }
        
        // 1. Blocked (0)
        if (activeLimits.any { it.limitDurationMinutes == 0L }) {
             return AppLimitEntity(packageName, 0, false)
        }

        // 2. Limited (>0) - Pick strictest
        val strictLimit = activeLimits
            .filter { it.limitDurationMinutes > 0 }
            .minByOrNull { it.limitDurationMinutes }
            
        if (strictLimit != null) {
            return AppLimitEntity(packageName, strictLimit.limitDurationMinutes.toInt(), false)
        }
        
        // 3. Allowed (-1) - If present and no other restrictions, return null (Unlimited)
        if (activeLimits.any { it.limitDurationMinutes == -1L }) {
            return null
        }
        return appRepository.getLimitForApp(packageName)
    }

    private fun isProfileActive(profile: com.mindfulscrolling.app.data.local.entity.FocusProfileEntity): Boolean {
        if (profile.isActive) return true
        if (!profile.scheduleEnabled) return false
        
        val now = java.util.Calendar.getInstance()
        val day = now.get(java.util.Calendar.DAY_OF_WEEK) // 1=Sun, 7=Sat
        val minute = now.get(java.util.Calendar.HOUR_OF_DAY) * 60 + now.get(java.util.Calendar.MINUTE)
        
        val days = profile.daysOfWeek?.split(",")?.mapNotNull { it.toIntOrNull() } ?: emptyList()
        
        if (days.contains(day) && 
            profile.startTime != null && 
            profile.endTime != null) {
                return if (profile.startTime <= profile.endTime) {
                    minute >= profile.startTime && minute < profile.endTime
                } else {
                    // Overnight (e.g. 23:00 to 07:00)
                    minute >= profile.startTime || minute < profile.endTime
                }
        }
        return false
    }
}
