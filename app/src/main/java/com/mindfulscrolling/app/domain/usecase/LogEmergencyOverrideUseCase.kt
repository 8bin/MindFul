package com.mindfulscrolling.app.domain.usecase

import com.mindfulscrolling.app.data.local.entity.OverrideLogEntity
import com.mindfulscrolling.app.domain.repository.AppRepository
import javax.inject.Inject

class LogEmergencyOverrideUseCase @Inject constructor(
    private val appRepository: AppRepository
) {
    suspend operator fun invoke(packageName: String, reason: String, durationMinutes: Int) {
        val override = OverrideLogEntity(
            packageName = packageName,
            timestamp = System.currentTimeMillis(),
            reason = reason,
            durationMinutes = durationMinutes
        )
        appRepository.logOverride(override)
    }
}
