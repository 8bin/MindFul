package com.mindfulscrolling.app.domain.usecase

import com.mindfulscrolling.app.domain.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ManageBreakUseCase @Inject constructor(
    private val repository: AppRepository
) {
    val isBreakActive: Flow<Boolean> = repository.isBreakActive()
    val breakEndTime: Flow<Long> = repository.getBreakEndTime()
    val breakWhitelist: Flow<Set<String>> = repository.getBreakWhitelist()

    suspend fun startBreak(durationMinutes: Int, whitelist: Set<String> = emptySet()) {
        val endTime = System.currentTimeMillis() + (durationMinutes * 60 * 1000)
        repository.updateBreakWhitelist(whitelist)
        repository.setBreakActive(true, endTime)
    }

    suspend fun startBreakWithProfile(durationMinutes: Int, profileId: Long) {
        val profileApps = repository.getProfileApps(profileId).first()
        val whitelist = profileApps.map { it.packageName }.toSet()
        startBreak(durationMinutes, whitelist)
    }

    suspend fun stopBreak() {
        repository.setBreakActive(false)
    }

    suspend fun getRemainingTimeMillis(): Long {
        val endTime = repository.getBreakEndTime().first()
        val remaining = endTime - System.currentTimeMillis()
        return if (remaining > 0) remaining else 0
    }
    
    private val systemWhitelist = setOf(
        "com.android.settings",
        "com.android.phone",
        "com.google.android.dialer",
        "com.android.systemui",
        "com.google.android.inputmethod.latin", // Gboard
        "com.samsung.android.honeyboard", // Samsung Keyboard
        "com.mindfulscrolling.app" // Prevent self-blocking
    )

    suspend fun isAppWhitelisted(packageName: String): Boolean {
        if (systemWhitelist.contains(packageName)) return true
        val whitelist = repository.getBreakWhitelist().first()
        return whitelist.contains(packageName)
    }
}
