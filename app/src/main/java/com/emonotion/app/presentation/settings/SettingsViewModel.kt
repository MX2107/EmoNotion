package com.emonotion.app.presentation.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.emonotion.app.R
import com.emonotion.app.domain.model.AppSettings
import com.emonotion.app.domain.model.ThemeMode
import com.emonotion.app.domain.repository.DataLocationRepository
import com.emonotion.app.domain.repository.GmailSyncRepository
import com.emonotion.app.domain.usecase.settings.AutoBackupUseCase
import com.emonotion.app.domain.usecase.settings.ChangeDataLocationUseCase
import com.emonotion.app.domain.usecase.settings.EnableAutoSyncUseCase
import com.emonotion.app.domain.usecase.settings.ExportDataUseCase
import com.emonotion.app.domain.usecase.settings.GetSettingsUseCase
import com.emonotion.app.domain.usecase.settings.GmailSyncUseCase
import com.emonotion.app.domain.usecase.settings.ImportDataUseCase
import com.emonotion.app.domain.usecase.settings.UpdateSettingsUseCase
import com.emonotion.app.presentation.common.BaseViewModel
import com.emonotion.app.utils.ThemeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel для экрана настроек
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getSettingsUseCase: GetSettingsUseCase,
    private val updateSettingsUseCase: UpdateSettingsUseCase,
    private val exportDataUseCase: ExportDataUseCase,
    private val importDataUseCase: ImportDataUseCase,
    private val gmailSyncUseCase: GmailSyncUseCase,
    private val enableAutoSyncUseCase: EnableAutoSyncUseCase,
    private val autoBackupUseCase: AutoBackupUseCase,
    private val changeDataLocationUseCase: ChangeDataLocationUseCase,
    private val dataLocationRepository: DataLocationRepository,
    private val gmailSyncRepository: GmailSyncRepository,
    @ApplicationContext private val appContext: Context
) : BaseViewModel() {

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

    private val _dataDisplayPath = MutableStateFlow("")
    val dataDisplayPath: StateFlow<String> = _dataDisplayPath.asStateFlow()

    private val _lastSyncDisplay = MutableStateFlow("")
    val lastSyncDisplay: StateFlow<String> = _lastSyncDisplay.asStateFlow()

    private val _gmailAutoSyncEnabled = MutableStateFlow(false)
    val gmailAutoSyncEnabled: StateFlow<Boolean> = _gmailAutoSyncEnabled.asStateFlow()

    private val _busyExportImport = MutableStateFlow(false)
    val busyExportImport: StateFlow<Boolean> = _busyExportImport.asStateFlow()

    private val _shouldRecreateActivity = MutableStateFlow(false)
    val shouldRecreateActivity: StateFlow<Boolean> = _shouldRecreateActivity.asStateFlow()

    fun loadSettings() {
        viewModelScope.launch {
            getSettingsUseCase().collect { s ->
                if (s == null) {
                    val def = AppSettings()
                    updateSettingsUseCase(def).fold(
                        onSuccess = {
                            _settings.value = def
                            applyFromSettings(def)
                        },
                        onFailure = { e ->
                            _errorMessage.value = e.message ?: appContext.getString(R.string.error_generic)
                        }
                    )
                } else {
                    _settings.value = s
                    applyFromSettings(s)
                }
                refreshAuxiliaryUi()
            }
        }
    }

    private fun applyFromSettings(s: AppSettings) {
        _theme.value = s.theme
        _notificationsEnabled.value = s.notificationsEnabled
        _autoBackupEnabled.value = s.autoBackupEnabled
        _reminderTime.value = s.reminderTime
        _language.value = s.language
        _moodReminderEnabled.value = s.moodReminderEnabled
    }

    fun refreshAuxiliaryUi() {
        _dataDisplayPath.value = dataLocationRepository.getDisplayPath()
        _lastSyncDisplay.value = gmailSyncRepository.getLastSyncDisplay()
        _gmailAutoSyncEnabled.value = gmailSyncRepository.isAutoSyncEnabled()
    }

    fun consumeRecreateRequest() {
        _shouldRecreateActivity.value = false
    }

    private fun persistCurrentFields() {
        val base = _settings.value ?: return
        viewModelScope.launch {
            val updated = base.copy(
                theme = _theme.value,
                notificationsEnabled = _notificationsEnabled.value,
                autoBackupEnabled = _autoBackupEnabled.value,
                language = _language.value,
                reminderTime = _reminderTime.value,
                moodReminderEnabled = _moodReminderEnabled.value
            )
            updateSettingsUseCase(updated).fold(
                onSuccess = { _settings.value = updated },
                onFailure = { e ->
                    _errorMessage.value = e.message ?: appContext.getString(R.string.error_generic)
                }
            )
        }
    }

    fun onThemeSelected(mode: ThemeMode) {
        if (_theme.value == mode) return
        _theme.value = mode
        ThemeManager.persist(appContext, mode)
        persistCurrentFields()
        _shouldRecreateActivity.value = true
    }

    fun toggleNotifications(enabled: Boolean) {
        _notificationsEnabled.value = enabled
        persistCurrentFields()
    }

    fun toggleMoodReminder(enabled: Boolean) {
        _moodReminderEnabled.value = enabled
        persistCurrentFields()
    }

    fun setReminderTime(time: String) {
        _reminderTime.value = time
        persistCurrentFields()
    }

    fun setLanguage(language: String) {
        _language.value = language
        persistCurrentFields()
    }

    fun onGmailAutoSyncSwitch(checked: Boolean, frequencyOrdinal: Int) {
        viewModelScope.launch {
            enableAutoSyncUseCase(checked, frequencyOrdinal).fold(
                onSuccess = {
                    _gmailAutoSyncEnabled.value = checked
                    refreshAuxiliaryUi()
                },
                onFailure = { e ->
                    _errorMessage.value = e.message ?: appContext.getString(R.string.error_generic)
                }
            )
        }
    }

    fun onGmailFrequencyChanged(frequencyOrdinal: Int) {
        if (!_gmailAutoSyncEnabled.value) return
        viewModelScope.launch {
            enableAutoSyncUseCase(true, frequencyOrdinal)
            refreshAuxiliaryUi()
        }
    }

    fun syncNow() {
        executeWithResult(
            operation = { gmailSyncUseCase.syncNow() },
            onSuccess = {
                refreshAuxiliaryUi()
                _successMessage.value = appContext.getString(R.string.sync_done_local)
            }
        )
    }

    fun onAutoBackupSwitch(checked: Boolean, backupFrequencyPosition: Int) {
        _autoBackupEnabled.value = checked
        if (checked) {
            autoBackupUseCase.schedule(freqToHours(backupFrequencyPosition))
        } else {
            autoBackupUseCase.cancel()
        }
        persistCurrentFields()
    }

    fun onBackupFrequencyChanged(position: Int) {
        if (!_autoBackupEnabled.value) return
        autoBackupUseCase.schedule(freqToHours(position))
    }

    private fun freqToHours(position: Int): Long = when (position.coerceIn(0, 2)) {
        0 -> 24L
        1 -> 24L * 7
        else -> 24L * 30
    }

    fun exportAllData() {
        viewModelScope.launch {
            _busyExportImport.value = true
            exportDataUseCase()
                .onSuccess { _successMessage.value = appContext.getString(R.string.export_data_done) }
                .onFailure { e -> _errorMessage.value = e.message ?: appContext.getString(R.string.error_generic) }
            _busyExportImport.value = false
        }
    }

    fun importAllData(json: String, replaceExisting: Boolean) {
        viewModelScope.launch {
            _busyExportImport.value = true
            importDataUseCase(json, replaceExisting)
                .onSuccess { _successMessage.value = appContext.getString(R.string.import_done) }
                .onFailure { e -> _errorMessage.value = e.message ?: appContext.getString(R.string.error_generic) }
            _busyExportImport.value = false
        }
    }

    fun applyPreferredDataTreeUri(uri: Uri?) {
        changeDataLocationUseCase(uri?.toString())
        refreshAuxiliaryUi()
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            val def = AppSettings()
            updateSettingsUseCase(def).fold(
                onSuccess = {
                    ThemeManager.persist(appContext, def.theme)
                    applyFromSettings(def)
                    _settings.value = def
                    autoBackupUseCase.cancel()
                    _shouldRecreateActivity.value = true
                    _successMessage.value = appContext.getString(R.string.reset_done)
                },
                onFailure = { e ->
                    _errorMessage.value = e.message ?: appContext.getString(R.string.error_generic)
                }
            )
        }
    }

    fun getAvailableLanguages(): List<String> = listOf("ru", "en")

    fun getLanguageDisplayName(languageCode: String): String = when (languageCode) {
        "ru" -> "Русский"
        "en" -> "English"
        else -> languageCode
    }

    fun gmailSyncFrequencyOrdinal(): Int = gmailSyncRepository.getSyncFrequencyOrdinal()
}
