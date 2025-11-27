package com.mindfulscrolling.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindfulscrolling.app.data.local.model.DailyUsageSummary
import com.mindfulscrolling.app.domain.usecase.GetUsageHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UsageHistoryViewModel @Inject constructor(
    private val getUsageHistoryUseCase: GetUsageHistoryUseCase
) : ViewModel() {

    private val _history = MutableStateFlow<List<DailyUsageSummary>>(emptyList())
    val history: StateFlow<List<DailyUsageSummary>> = _history.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            getUsageHistoryUseCase().collect {
                _history.value = it
            }
        }
    }
}
