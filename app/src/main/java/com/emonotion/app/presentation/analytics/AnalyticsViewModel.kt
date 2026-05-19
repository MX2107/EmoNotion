package com.emonotion.app.presentation.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emonotion.app.domain.model.Analytics
import com.emonotion.app.domain.model.AnalyticsPeriod
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
    private val getAnalyticsUseCase: GetAnalyticsUseCase
) : ViewModel() {
    
    private val _analyticsData = MutableStateFlow<Analytics?>(null)
    val analyticsData: StateFlow<Analytics?> = _analyticsData.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val _selectedPeriod = MutableStateFlow(AnalyticsPeriod.WEEK)
    val selectedPeriod: StateFlow<AnalyticsPeriod> = _selectedPeriod.asStateFlow()
    
    fun loadAnalyticsData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val analytics = getAnalyticsUseCase(_selectedPeriod.value)
                _analyticsData.value = analytics
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка загрузки аналитики: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun setPeriod(period: AnalyticsPeriod) {
        _selectedPeriod.value = period
        loadAnalyticsData()
    }
    
    fun exportAnalytics() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                // Экспорт аналитики будет реализован через соответствующий use case
                _errorMessage.value = "Экспорт аналитики в разработке"
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка экспорта: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
