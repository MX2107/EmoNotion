package com.emonotion.app.presentation.settings

import androidx.lifecycle.viewModelScope
import com.emonotion.app.domain.model.AppSettings
import com.emonotion.app.domain.model.ThemeMode
import com.emonotion.app.domain.usecase.settings.GetSettingsUseCase
import com.emonotion.app.domain.usecase.settings.UpdateSettingsUseCase
import com.emonotion.app.presentation.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel для экрана настроек
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getSettingsUseCase: GetSettingsUseCase,
    private val updateSettingsUseCase: UpdateSettingsUseCase
) : BaseViewModel() {
    
    // Состояния UI
    private val _settings = MutableStateFlow<AppSettings?>(null)
    val settings: StateFlow<AppSettings?> = _settings.asStateFlow()
    
    private val _theme = MutableStateFlow(ThemeMode.LIGHT)
    val theme: StateFlow<ThemeMode> = _theme.asStateFlow()
    
    private val _notificationsEnabled = MutableStateFlow(false)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()
    
    private val _autoBackupEnabled = MutableStateFlow(false)
    val autoBackupEnabled: StateFlow<Boolean> = _autoBackupEnabled.asStateFlow()
    
    private val _reminderTime = MutableStateFlow("09:00")
    val reminderTime: StateFlow<String> = _reminderTime.asStateFlow()
    
    private val _language = MutableStateFlow("ru")
    val language: StateFlow<String> = _language.asStateFlow()
    
    private val _moodReminderEnabled = MutableStateFlow(true)
    val moodReminderEnabled: StateFlow<Boolean> = _moodReminderEnabled.asStateFlow()
    
    /**
     * Загружает настройки приложения
     */
    fun loadSettings() {
        executeWithLoading {
            viewModelScope.launch {
                getSettingsUseCase().collect { settings ->
                    _settings.value = settings
                    settings?.let {
                        _theme.value = it.theme
                        _notificationsEnabled.value = it.notificationsEnabled
                        _autoBackupEnabled.value = it.autoBackupEnabled
                        _reminderTime.value = it.reminderTime
                        _language.value = it.language
                        _moodReminderEnabled.value = it.moodReminderEnabled
                    } ?: run {
                        // Создаем настройки по умолчанию
                        createDefaultSettings()
                    }
                }
            }
        }
    }
    
    /**
     * Создает настройки по умолчанию
     */
    private fun createDefaultSettings() {
        executeWithLoading {
            viewModelScope.launch {
                val defaultSettings = AppSettings(
                    theme = ThemeMode.LIGHT,
                    notificationsEnabled = true,
                    autoBackupEnabled = false,
                    language = "ru",
                    reminderTime = "09:00",
                    moodReminderEnabled = true
                )
                try {
                    updateSettingsUseCase(defaultSettings)
                    _settings.value = defaultSettings
                } catch (error: Exception) {
                    _errorMessage.value = "Ошибка создания настроек: ${error.message}"
                }
            }
        }
    }
    
    /**
     * Обновляет настройки
     */
    private fun updateSettings(settings: AppSettings) {
        executeWithLoading {
            viewModelScope.launch {
                val updatedSettings = settings.copy(
                    theme = _theme.value,
                    notificationsEnabled = _notificationsEnabled.value,
                    autoBackupEnabled = _autoBackupEnabled.value,
                    language = _language.value,
                    reminderTime = _reminderTime.value,
                    moodReminderEnabled = _moodReminderEnabled.value
                )
                try {
                    updateSettingsUseCase(updatedSettings)
                    _settings.value = updatedSettings
                } catch (error: Exception) {
                    _errorMessage.value = "Ошибка обновления настроек: ${error.message}"
                }
            }
        }
    }
    
    /**
     * Переключает темную тему
     */
    fun toggleTheme(theme: ThemeMode) {
        _theme.value = theme
        updateSettings(_settings.value ?: AppSettings())
    }
    
    fun toggleNotifications(enabled: Boolean) {
        _notificationsEnabled.value = enabled
        updateSettings()
    }
    
    fun toggleAutoBackup(enabled: Boolean) {
        _autoBackupEnabled.value = enabled
        updateSettings()
    }
    
    fun setReminderTime(time: String) {
        _reminderTime.value = time
        updateSettings()
    }
    
    fun setLanguage(language: String) {
        _language.value = language
        updateSettings()
    }
    
    fun toggleMoodReminder(enabled: Boolean) {
        _moodReminderEnabled.value = enabled
        updateSettings()
    }
    
    /**
     * Обновляет настройки
     */
    private fun updateSettings() {
        val currentSettings = _settings.value ?: return
        
        executeWithResult(
            operation = {
                val updatedSettings = currentSettings.copy(
                    theme = _theme.value,
                    notificationsEnabled = _notificationsEnabled.value,
                    reminderTime = _reminderTime.value,
                    language = _language.value,
                    autoBackupEnabled = _autoBackupEnabled.value,
                    moodReminderEnabled = _moodReminderEnabled.value
                )
                updateSettingsUseCase(updatedSettings)
            },
            onSuccess = {
                loadSettings() // Обновляем данные после сохранения
            }
        )
    }
    
    /**
     * Сбрасывает настройки к значениям по умолчанию
     */
    fun resetToDefaults() {
        createDefaultSettings()
    }
    
    /**
     * Получает доступные языки
     */
    fun getAvailableLanguages(): List<String> {
        return listOf("ru", "en")
    }
    
    /**
     * Получает отображаемое название языка
     */
    fun getLanguageDisplayName(languageCode: String): String {
        return when (languageCode) {
            "ru" -> "Русский"
            "en" -> "English"
            else -> languageCode
        }
    }
}
