package com.emonotion.app.presentation.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Базовый ViewModel класс для всех ViewModel'ов в приложении
 */
abstract class BaseViewModel : ViewModel() {
    
    /**
     * Состояние загрузки
     */
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    /**
     * Состояние ошибки
     */
    protected val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    /**
     * Состояние успешного выполнения
     */
    protected val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()
    
    /**
     * Выполняет асинхронную операцию с обработкой ошибок и состоянием загрузки
     */
    protected fun executeWithLoading(
        operation: suspend () -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                _successMessage.value = null
                operation()
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Произошла ошибка"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Выполняет асинхронную операцию с возвратом результата
     */
    protected fun <T> executeWithResult(
        operation: suspend () -> Result<T>,
        onSuccess: (T) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                _successMessage.value = null
                
                val result = operation()
                result.fold(
                    onSuccess = { success ->
                        onSuccess(success)
                    },
                    onFailure = { error ->
                        val message = error.message ?: "Произошла ошибка"
                        _errorMessage.value = message
                        onError(message)
                    }
                )
            } catch (e: Exception) {
                val message = e.message ?: "Произошла ошибка"
                _errorMessage.value = message
                onError(message)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Очищает сообщение об ошибке
     */
    fun clearError() {
        _errorMessage.value = null
    }
    
    /**
     * Очищает сообщение об успешном выполнении
     */
    fun clearSuccess() {
        _successMessage.value = null
    }
}
