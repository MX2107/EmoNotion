package com.emonotion.app.presentation.dailyentry

import androidx.lifecycle.viewModelScope
import com.emonotion.app.domain.model.MoodEntry
import com.emonotion.app.domain.model.MoodType
import com.emonotion.app.domain.usecase.mood.AddMoodUseCase
import com.emonotion.app.domain.usecase.mood.GetTodayMoodUseCase
import com.emonotion.app.presentation.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * ViewModel для экрана дневной записи
 */
@HiltViewModel
class DailyEntryViewModel @Inject constructor(
    private val getTodayMoodUseCase: GetTodayMoodUseCase,
    private val addMoodUseCase: AddMoodUseCase
) : BaseViewModel() {
    
    private val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    
    // Состояния UI
    private val _selectedDate = MutableStateFlow(today)
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()
    
    private val _currentMood = MutableStateFlow<MoodEntry?>(null)
    val currentMood: StateFlow<MoodEntry?> = _currentMood.asStateFlow()
    
    private val _selectedMoodType = MutableStateFlow<MoodType?>(null)
    val selectedMoodType: StateFlow<MoodType?> = _selectedMoodType.asStateFlow()
    
    private val _selectedIntensity = MutableStateFlow(3)
    val selectedIntensity: StateFlow<Int> = _selectedIntensity.asStateFlow()
    
    private val _activities = MutableStateFlow<List<String>>(emptyList())
    val activities: StateFlow<List<String>> = _activities.asStateFlow()
    
    private val _notes = MutableStateFlow("")
    val notes: StateFlow<String> = _notes.asStateFlow()
    
    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()
    
    /**
     * Устанавливает дату для записи
     */
    fun setDate(date: String) {
        _selectedDate.value = date
        loadMoodForDate(date)
    }
    
    /**
     * Выбирает тип настроения
     */
    fun selectMoodType(moodType: MoodType) {
        _selectedMoodType.value = moodType
    }
    
    /**
     * Устанавливает интенсивность настроения
     */
    fun setIntensity(intensity: Int) {
        _selectedIntensity.value = intensity.coerceIn(1, 5)
    }
    
    /**
     * Добавляет активность
     */
    fun addActivity(activity: String) {
        if (activity.isNotBlank()) {
            _activities.value = _activities.value + activity.trim()
        }
    }
    
    /**
     * Удаляет активность
     */
    fun removeActivity(activity: String) {
        _activities.value = _activities.value - activity
    }
    
    /**
     * Устанавливает заметки
     */
    fun setNotes(notes: String) {
        _notes.value = notes
    }
    
    /**
     * Загружает настроение для указанной даты
     */
    private fun loadMoodForDate(date: String) {
        executeWithLoading {
            viewModelScope.launch {
                getTodayMoodUseCase().collect { mood ->
                    if (mood?.date == date) {
                        _currentMood.value = mood
                        _selectedMoodType.value = mood.mood
                        _selectedIntensity.value = mood.intensity
                        _activities.value = mood.activities
                        _notes.value = mood.notes ?: ""
                        _isEditing.value = true
                    } else {
                        _currentMood.value = null
                        _selectedMoodType.value = null
                        _selectedIntensity.value = 3
                        _activities.value = emptyList()
                        _notes.value = ""
                        _isEditing.value = false
                    }
                }
            }
        }
    }
    
    /**
     * Сохраняет или обновляет запись о настроении
     */
    fun saveMoodEntry() {
        val moodType = _selectedMoodType.value
        if (moodType == null) {
            _errorMessage.value = "Выберите тип настроения"
            return
        }
        
        executeWithResult(
            operation = {
                val moodEntry = MoodEntry(
                    id = _currentMood.value?.id ?: UUID.randomUUID().toString(),
                    mood = moodType,
                    intensity = _selectedIntensity.value,
                    activities = _activities.value,
                    notes = _notes.value.takeIf { it.isNotBlank() },
                    timestamp = System.currentTimeMillis(),
                    date = _selectedDate.value
                )
                addMoodUseCase(moodEntry)
            },
            onSuccess = {
                _isEditing.value = true
                // Можно добавить навигацию назад или сообщение об успехе
            }
        )
    }
    
    /**
     * Сбрасывает форму
     */
    fun resetForm() {
        _selectedMoodType.value = null
        _selectedIntensity.value = 3
        _activities.value = emptyList()
        _notes.value = ""
        _isEditing.value = false
    }
    
    /**
     * Получает доступные типы настроений
     */
    fun getMoodTypes(): List<MoodType> {
        return MoodType.values().toList()
    }
}
