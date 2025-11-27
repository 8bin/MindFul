package com.mindfulscrolling.app.ui.profiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindfulscrolling.app.data.local.entity.FocusProfileEntity
import com.mindfulscrolling.app.domain.usecase.ManageFocusProfilesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FocusProfilesViewModel @Inject constructor(
    private val manageFocusProfilesUseCase: ManageFocusProfilesUseCase
) : ViewModel() {

    val profiles: StateFlow<List<FocusProfileEntity>> = manageFocusProfilesUseCase.getAllProfiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeProfiles: StateFlow<List<FocusProfileEntity>> = manageFocusProfilesUseCase.getActiveProfiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleProfileActivation(profile: FocusProfileEntity) {
        viewModelScope.launch {
            if (profile.isActive) {
                manageFocusProfilesUseCase.deactivateProfile(profile.id)
            } else {
                manageFocusProfilesUseCase.activateProfile(profile.id)
            }
        }
    }

    fun deleteProfile(profile: FocusProfileEntity) {
        viewModelScope.launch {
            manageFocusProfilesUseCase.deleteProfile(profile)
        }
    }

    fun createProfile(name: String, icon: String) {
        viewModelScope.launch {
            manageFocusProfilesUseCase.createProfile(name, icon)
        }
    }
}
