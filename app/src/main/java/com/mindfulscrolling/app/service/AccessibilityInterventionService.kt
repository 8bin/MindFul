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

    @Inject
    lateinit var usageNotificationManager: UsageNotificationManager

    @Inject
    lateinit var appRepository: com.mindfulscrolling.app.domain.repository.AppRepository

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var monitoringJob: Job? = null
    private var currentPackageName: String? = null
    
    // Track cumulative usage per app this session
    private val sessionUsageMap = mutableMapOf<String, Long>()

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return
            if (packageName == "com.mindfulscrolling.app") return // Ignore own overlay
            
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
                val now = System.currentTimeMillis()
                val delta = now - lastCheckTime
                lastCheckTime = now

                val today = getStartOfDay()
                
                // Update usage
                updateUsageUseCase(packageName, delta, today)
                
                // Track cumulative usage for notifications
                sessionUsageMap[packageName] = (sessionUsageMap[packageName] ?: 0L) + delta
                
                // Look up per-app notification interval from limits
                val perAppInterval = try {
                    val limit = getAppLimitUseCase(packageName)
                    limit?.notificationIntervalMinutes
                } catch (_: Exception) { null }
                
                // Check and fire usage notification
                val appName = try {
                    val pm = applicationContext.packageManager
                    val appInfo = pm.getApplicationInfo(packageName, 0)
                    pm.getApplicationLabel(appInfo).toString()
                } catch (_: Exception) {
                    packageName
                }
                usageNotificationManager.checkAndNotify(
                    packageName = packageName,
                    totalUsageMillis = sessionUsageMap[packageName] ?: 0L,
                    appName = appName,
                    perAppIntervalMinutes = perAppInterval
                )

                var shouldBlock = false
                var isBreakMode = false
                var remaining = 0L
                var endTime = 0L
                var whitelistApps = emptyList<String>()

                // 1. Check Take a Break Mode
                val isBreakActive = manageBreakUseCase.isBreakActive.first()
                if (isBreakActive) {
                    remaining = manageBreakUseCase.getRemainingTimeMillis()
                    endTime = manageBreakUseCase.breakEndTime.first()
                    if (remaining > 0) {
                        if (!manageBreakUseCase.isAppWhitelisted(packageName)) {
                             shouldBlock = true
                             isBreakMode = true
                             whitelistApps = manageBreakUseCase.breakWhitelist.first().toList()
                        }
                    } else {
                        manageBreakUseCase.stopBreak()
                    }
                }

                // 2. Check App Limits (if not already blocked by break)
                if (!shouldBlock) {
                    val isExceeded = checkLimitExceededUseCase(packageName, today)
                    if (isExceeded) {
                        shouldBlock = true
                    }
                }

                if (shouldBlock) {
                    overlayManager.showOverlay(
                        packageName = packageName,
                        isBreakMode = isBreakMode,
                        remainingTime = remaining,
                        breakEndTime = endTime,
                        whitelistedApps = whitelistApps
                    ) {
                        performGlobalAction(GLOBAL_ACTION_HOME)
                    }
                } else {
                    overlayManager.removeOverlay()
                }
                
                delay(100) // Check every 100ms
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
