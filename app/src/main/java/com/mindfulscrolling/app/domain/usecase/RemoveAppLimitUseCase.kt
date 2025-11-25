package com.mindfulscrolling.app.domain.usecase

import com.mindfulscrolling.app.domain.repository.AppRepository
import javax.inject.Inject

class RemoveAppLimitUseCase @Inject constructor(
    private val appRepository: AppRepository
) {
    suspend operator fun invoke(packageName: String) {
        appRepository.deleteLimit(packageName)
    }
}
