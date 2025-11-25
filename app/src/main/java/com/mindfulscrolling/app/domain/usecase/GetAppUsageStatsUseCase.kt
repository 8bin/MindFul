package com.mindfulscrolling.app.domain.usecase

import com.mindfulscrolling.app.data.local.entity.UsageLogEntity
import com.mindfulscrolling.app.domain.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAppUsageStatsUseCase @Inject constructor(
    private val appRepository: AppRepository
) {
    operator fun invoke(date: Long): Flow<List<UsageLogEntity>> {
        return appRepository.getUsageForDate(date)
    }
}
