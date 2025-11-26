package com.mindfulscrolling.app.ui.profiles

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindfulscrolling.app.data.local.entity.FocusProfileEntity
import com.mindfulscrolling.app.data.local.entity.ProfileAppCrossRef
import com.mindfulscrolling.app.domain.model.AppInfo
import com.mindfulscrolling.app.domain.repository.AppRepository
import com.mindfulscrolling.app.domain.usecase.ManageFocusProfilesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val manageFocusProfilesUseCase: ManageFocusProfilesUseCase,
    private val appRepository: AppRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val profileId: Long = checkNotNull(savedStateHandle["profileId"])

    private val _profile = MutableStateFlow<FocusProfileEntity?>(null)
    val profile: StateFlow<FocusProfileEntity?> = _profile

    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    
    // Apps currently in the profile
    private val _profileApps = MutableStateFlow<List<ProfileAppCrossRef>>(emptyList())

    // Combined list of apps with their status in the profile
    val appListState = combine(_installedApps, _profileApps) { installed, profileApps ->
        installed.map { app ->
            val profileApp = profileApps.find { it.packageName == app.packageName }
            AppProfileState(
                appInfo = app,
                isSelected = profileApp != null,
                limitMinutes = profileApp?.limitDurationMinutes ?: 0 // Default to 0 (Blocked) if selected
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadProfile()
        loadApps()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _profile.value = manageFocusProfilesUseCase.getProfileById(profileId)
            manageFocusProfilesUseCase.getProfileApps(profileId).collect {
                _profileApps.value = it
            }
        }
    }

    private fun loadApps() {
        viewModelScope.launch {
            _installedApps.value = appRepository.getInstalledApps()
        }
    }

    fun updateProfileName(name: String) {
        val current = _profile.value ?: return
        viewModelScope.launch {
            manageFocusProfilesUseCase.updateProfile(current.copy(name = name))
            _profile.value = current.copy(name = name)
        }
    }

    fun toggleAppSelection(appInfo: AppInfo) {
        val currentApps = _profileApps.value.toMutableList()
        val existing = currentApps.find { it.packageName == appInfo.packageName }
        
        if (existing != null) {
            currentApps.remove(existing)
        } else {
            currentApps.add(ProfileAppCrossRef(profileId, appInfo.packageName, 0)) // Default block
        }
        
        updateProfileApps(currentApps)
    }

    fun updateAppLimit(packageName: String, limitMinutes: Long) {
        val currentApps = _profileApps.value.toMutableList()
        val index = currentApps.indexOfFirst { it.packageName == packageName }
        if (index != -1) {
            currentApps[index] = currentApps[index].copy(limitDurationMinutes = limitMinutes)
            updateProfileApps(currentApps)
        }
    }

    private fun updateProfileApps(apps: List<ProfileAppCrossRef>) {
        _profileApps.value = apps
        viewModelScope.launch {
            manageFocusProfilesUseCase.updateProfileApps(profileId, apps)
        }
    }
}

data class AppProfileState(
    val appInfo: AppInfo,
    val isSelected: Boolean,
    val limitMinutes: Long
)
