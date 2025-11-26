package com.mindfulscrolling.app.domain.usecase

import com.mindfulscrolling.app.data.local.entity.UsageLogEntity
import com.mindfulscrolling.app.domain.repository.AppRepository
import com.mindfulscrolling.app.domain.repository.UsageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject

class GetDailyUsageUseCase @Inject constructor(
    private val usageRepository: UsageRepository,
    private val appRepository: AppRepository
) {
    operator fun invoke(): Flow<List<UsageLogEntity>> {
        val today = getStartOfDay()
        return usageRepository.getUsageForDate(today).map { logs ->
            logs.sortedByDescending { it.durationMillis }
        }
    }

    private fun getStartOfDay(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
