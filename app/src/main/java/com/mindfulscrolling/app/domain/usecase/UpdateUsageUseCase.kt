package com.mindfulscrolling.app.domain.usecase

import com.mindfulscrolling.app.domain.repository.UsageRepository
import javax.inject.Inject

class UpdateUsageUseCase @Inject constructor(
    private val usageRepository: UsageRepository
) {
    suspend operator fun invoke(packageName: String, duration: Long, date: Long) {
        usageRepository.logUsage(packageName, duration, date)
    }
}
