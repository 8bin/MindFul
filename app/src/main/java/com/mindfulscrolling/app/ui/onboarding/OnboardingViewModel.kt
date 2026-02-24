package com.mindfulscrolling.app.ui.onboarding

import androidx.lifecycle.ViewModel
import com.mindfulscrolling.app.domain.manager.PermissionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val permissionManager: PermissionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        refreshPermissions()
    }

    fun refreshPermissions() {
        _uiState.value = OnboardingUiState(
            hasUsageAccess = permissionManager.hasUsageStatsPermission(),
            hasOverlayPermission = permissionManager.hasOverlayPermission(),
            hasAccessibilityService = permissionManager.isAccessibilityServiceEnabled()
        )
    }
}

data class OnboardingUiState(
    val hasUsageAccess: Boolean = false,
    val hasOverlayPermission: Boolean = false,
    val hasAccessibilityService: Boolean = false
)
