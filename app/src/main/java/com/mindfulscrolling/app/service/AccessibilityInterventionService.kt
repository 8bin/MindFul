package com.mindfulscrolling.app.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.mindfulscrolling.app.domain.usecase.CheckLimitExceededUseCase
import com.mindfulscrolling.app.domain.usecase.GetAppLimitUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AccessibilityInterventionService : AccessibilityService() {

    @Inject
    lateinit var checkLimitExceededUseCase: CheckLimitExceededUseCase

    @Inject
    lateinit var getAppLimitUseCase: GetAppLimitUseCase

    @Inject
    lateinit var overlayManager: com.mindfulscrolling.app.ui.overlay.OverlayManager

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var currentPackageName: String? = null

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return
            currentPackageName = packageName
            checkUsage(packageName)
        }
    }

    private fun checkUsage(packageName: String) {
        serviceScope.launch {
            // Check if app has a limit
            val limit = getAppLimitUseCase(packageName)
            if (limit != null) {
                // Check if limit is exceeded
                val isExceeded = checkLimitExceededUseCase(packageName, System.currentTimeMillis())
                if (isExceeded) {
                    overlayManager.showOverlay(packageName) {
                        // On Dismiss (Close App), go Home
                        performGlobalAction(GLOBAL_ACTION_HOME)
                    }
                }
            }
        }
    }

    override fun onInterrupt() {
        // Handle interruption
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancel scope if needed
    }
}
