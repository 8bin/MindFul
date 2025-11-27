package com.mindfulscrolling.app.domain.usecase

import com.mindfulscrolling.app.data.local.model.DailyUsageSummary
import com.mindfulscrolling.app.domain.repository.UsageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUsageHistoryUseCase @Inject constructor(
    private val usageRepository: UsageRepository
) {
    operator fun invoke(): Flow<List<DailyUsageSummary>> {
        return usageRepository.getUsageHistory()
    }
}
