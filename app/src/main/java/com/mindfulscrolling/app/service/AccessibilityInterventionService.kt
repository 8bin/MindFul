package com.mindfulscrolling.app.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.mindfulscrolling.app.domain.usecase.CheckLimitExceededUseCase
import com.mindfulscrolling.app.domain.usecase.GetAppLimitUseCase
import kotlinx.coroutines.flow.first
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import com.mindfulscrolling.app.domain.usecase.UpdateUsageUseCase
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import javax.inject.Inject

@AndroidEntryPoint
class AccessibilityInterventionService : AccessibilityService() {

    @Inject
    lateinit var checkLimitExceededUseCase: CheckLimitExceededUseCase

    @Inject
    lateinit var getAppLimitUseCase: GetAppLimitUseCase

    @Inject
    lateinit var updateUsageUseCase: UpdateUsageUseCase

    @Inject
    lateinit var overlayManager: com.mindfulscrolling.app.ui.overlay.OverlayManager

    @Inject
    lateinit var manageBreakUseCase: com.mindfulscrolling.app.domain.usecase.ManageBreakUseCase

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var monitoringJob: Job? = null
    private var currentPackageName: String? = null

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return
            if (packageName != currentPackageName) {
                stopMonitoring()
                startMonitoring(packageName)
            }
        }
    }

    private fun startMonitoring(packageName: String) {
        currentPackageName = packageName
        monitoringJob = serviceScope.launch {
            var lastCheckTime = System.currentTimeMillis()
            while (isActive) {
                delay(200) // Check every 200ms for faster response
                val now = System.currentTimeMillis()
                val delta = now - lastCheckTime
                lastCheckTime = now

                val today = getStartOfDay()
                
                // Update usage
                updateUsageUseCase(packageName, delta, today)

                // 1. Check Take a Break Mode
                val isBreakActive = manageBreakUseCase.isBreakActive.first()
                if (isBreakActive) {
                    val remaining = manageBreakUseCase.getRemainingTimeMillis()
                    if (remaining > 0) {
                        if (!manageBreakUseCase.isAppWhitelisted(packageName)) {
                             overlayManager.showOverlay(
                                 packageName = packageName,
                                 isBreakMode = true,
                                 remainingTime = remaining
                             ) {
                                 performGlobalAction(GLOBAL_ACTION_HOME)
                             }
                             continue // Skip other checks if blocked by break
                        }
                    } else {
                        manageBreakUseCase.stopBreak()
                    }
                }

                // 2. Check App Limits
                val isExceeded = checkLimitExceededUseCase(packageName, today)
                if (isExceeded) {
                    overlayManager.showOverlay(packageName) {
                        // On Dismiss (Close App), go Home
                        performGlobalAction(GLOBAL_ACTION_HOME)
                    }
                }
            }
        }
    }

    private fun stopMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = null
        currentPackageName = null
    }

    private fun getStartOfDay(): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    override fun onInterrupt() {
        stopMonitoring()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
