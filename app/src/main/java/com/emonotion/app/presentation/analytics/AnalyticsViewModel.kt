package com.emonotion.app.presentation.analytics

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emonotion.app.domain.model.Analytics
import com.emonotion.app.domain.model.AnalyticsExportFormat
import com.emonotion.app.domain.model.AnalyticsPeriod
import com.emonotion.app.domain.usecase.analytics.ExportAnalyticsUseCase
import com.emonotion.app.domain.usecase.analytics.GetAnalyticsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel для экрана аналитики
 */
@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val getAnalyticsUseCase: GetAnalyticsUseCase,
    private val exportAnalyticsUseCase: ExportAnalyticsUseCase
) : ViewModel() {

    private val _analyticsData = MutableStateFlow<Analytics?>(null)
    val analyticsData: StateFlow<Analytics?> = _analyticsData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _exportFinishedUri = MutableStateFlow<Uri?>(null)
    val exportFinishedUri: StateFlow<Uri?> = _exportFinishedUri.asStateFlow()

    private val _selectedPeriod = MutableStateFlow(AnalyticsPeriod.WEEK)
    val selectedPeriod: StateFlow<AnalyticsPeriod> = _selectedPeriod.asStateFlow()

    fun loadAnalyticsData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                val period = _selectedPeriod.value
                android.util.Log.d("AnalyticsViewModel", "loadAnalyticsData: period=$period")
                val analytics = getAnalyticsUseCase(period)
                android.util.Log.d("AnalyticsViewModel", "loadAnalyticsData: analytics.totalEntries=${analytics.totalEntries}, moodDistribution=${analytics.moodDistribution}")
                _analyticsData.value = analytics
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка загрузки аналитики: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setPeriod(period: AnalyticsPeriod) {
        android.util.Log.d("AnalyticsViewModel", "setPeriod: oldPeriod=${_selectedPeriod.value}, newPeriod=$period")
        _selectedPeriod.value = period
        loadAnalyticsData()
    }

    fun exportAnalytics(format: AnalyticsExportFormat) {
        viewModelScope.launch {
            try {
                _isExporting.value = true
                _errorMessage.value = null
                exportAnalyticsUseCase(_selectedPeriod.value, format)
                    .onSuccess { uri -> _exportFinishedUri.value = uri }
                    .onFailure { e ->
                        _errorMessage.value = e.message ?: "Ошибка экспорта"
                    }
            } finally {
                _isExporting.value = false
            }
        }
    }

    fun consumeExportUri() {
        _exportFinishedUri.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
