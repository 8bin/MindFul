package com.mindfulscrolling.app.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mindfulscrolling.app.domain.usecase.SyncUsageUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncUsageWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncUsageUseCase: SyncUsageUseCase
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            syncUsageUseCase()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
