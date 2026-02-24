package com.mindfulscrolling.app.domain.usecase

import com.mindfulscrolling.app.domain.model.DailyAnalytics
import com.mindfulscrolling.app.domain.repository.UsageRepository
import javax.inject.Inject

class GetAnalyticsDataUseCase @Inject constructor(
    private val usageRepository: UsageRepository
) {
    suspend operator fun invoke(date: Long): DailyAnalytics {
        return usageRepository.getDailyAnalytics(date)
    }
}
