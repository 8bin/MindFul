package com.mindfulscrolling.app.domain.usecase

import com.mindfulscrolling.app.domain.repository.AppRepository
import javax.inject.Inject

class CheckLimitExceededUseCase @Inject constructor(
    private val appRepository: AppRepository
) {
    suspend operator fun invoke(packageName: String, date: Long): Boolean {
        val limit = appRepository.getLimitForApp(packageName) ?: return false
        val usage = appRepository.getUsageForAppAndDate(packageName, date) ?: return false
        
        val usageMinutes = usage.durationMillis / 1000 / 60
        return usageMinutes >= limit.limitDurationMinutes
    }
}
