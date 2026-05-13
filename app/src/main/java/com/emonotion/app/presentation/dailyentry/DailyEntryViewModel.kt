package com.emonotion.app.presentation.dailyentry

import androidx.lifecycle.viewModelScope
import com.emonotion.app.domain.model.MoodEntry
import com.emonotion.app.domain.model.MoodType
import com.emonotion.app.domain.usecase.mood.AddMoodUseCase
import com.emonotion.app.domain.usecase.mood.DeleteMoodUseCase
import com.emonotion.app.domain.usecase.mood.GetMoodByDateUseCase
import com.emonotion.app.domain.usecase.mood.GetTodayMoodUseCase
import com.emonotion.app.domain.usecase.custommood.AddCustomMoodUseCase
import com.emonotion.app.domain.usecase.customactivity.AddCustomActivityUseCase
import com.emonotion.app.domain.usecase.custommood.GetCustomMoodsUseCase
import com.emonotion.app.domain.usecase.customactivity.GetCustomActivitiesUseCase
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
    private val getMoodByDateUseCase: GetMoodByDateUseCase,
    private val addMoodUseCase: AddMoodUseCase,
    private val deleteMoodUseCase: DeleteMoodUseCase,
    private val addCustomMoodUseCase: AddCustomMoodUseCase,
    private val addCustomActivityUseCase: AddCustomActivityUseCase,
    private val getCustomMoodsUseCase: GetCustomMoodsUseCase,
    private val getCustomActivitiesUseCase: GetCustomActivitiesUseCase
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
    
    private val _emotions = MutableStateFlow<List<String>>(emptyList())
    val emotions: StateFlow<List<String>> = _emotions.asStateFlow()
    
    private val _activities = MutableStateFlow<List<String>>(emptyList())
    val activities: StateFlow<List<String>> = _activities.asStateFlow()
    
    // Списки пользовательских элементов
    private val _customEmotions = MutableStateFlow<List<String>>(emptyList())
    val customEmotions: StateFlow<List<String>> = _customEmotions.asStateFlow()
    
    private val _customActivities = MutableStateFlow<List<String>>(emptyList())
    val customActivities: StateFlow<List<String>> = _customActivities.asStateFlow()
    
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
        loadCustomElements()
    }
    
    /**
     * Загружает пользовательские элементы
     */
    private fun loadCustomElements() {
        // Загружаем пользовательские эмоции
        viewModelScope.launch {
            try {
                getCustomMoodsUseCase.getActiveCustomMoods().collect { moods ->
                    _customEmotions.value = moods.map { it.name }
                }
            } catch (e: Exception) {
                android.util.Log.e("DailyEntryViewModel", "Ошибка загрузки пользовательских эмоций", e)
            }
        }
        
        // Загружаем пользовательские активности
        viewModelScope.launch {
            try {
                getCustomActivitiesUseCase.getActiveCustomActivities().collect { activities ->
                    _customActivities.value = activities.map { it.name }
                }
            } catch (e: Exception) {
                android.util.Log.e("DailyEntryViewModel", "Ошибка загрузки пользовательских активностей", e)
            }
        }
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
     * Добавляет эмоцию
     */
    fun addEmotion(emotion: String) {
        if (emotion.isNotBlank()) {
            _emotions.value = _emotions.value + emotion.trim()
        }
    }
    
    /**
     * Удаляет эмоцию
     */
    fun removeEmotion(emotion: String) {
        _emotions.value = _emotions.value - emotion
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
                getMoodByDateUseCase(date).collect { mood ->
                    if (mood != null) {
                        _currentMood.value = mood
                        _selectedMoodType.value = mood.mood
                        _selectedIntensity.value = mood.intensity
                        _emotions.value = mood.emotions
                        _activities.value = mood.activities
                        _notes.value = mood.notes ?: ""
                        _isEditing.value = true
                    } else {
                        _currentMood.value = null
                        _selectedMoodType.value = null
                        _selectedIntensity.value = 3
                        _emotions.value = emptyList()
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
                    emotions = _emotions.value,
                    activities = _activities.value,
                    notes = _notes.value.takeIf { it.isNotBlank() },
                    timestamp = System.currentTimeMillis(),
                    date = _selectedDate.value
                )
                
                // Если существует запись - обновляем, иначе добавляем новую
                if (_currentMood.value != null) {
                    // Обновление существующей записи
                    addMoodUseCase(moodEntry) // REPLACE стратегия в DAO обновит запись
                } else {
                    // Добавление новой записи
                    addMoodUseCase(moodEntry)
                }
            },
            onSuccess = {
                _isEditing.value = true
                _successMessage.value = "Запись успешно сохранена"
            }
        )
    }
    
    /**
     * Сбрасывает форму
     */
    fun resetForm() {
        _selectedMoodType.value = null
        _selectedIntensity.value = 3
        _emotions.value = emptyList()
        _activities.value = emptyList()
        _notes.value = ""
        _isEditing.value = false
    }
    
    /**
     * Удаляет текущую запись о настроении
     */
    fun deleteCurrentMood() {
        executeWithResult(
            operation = {
                val currentMood = _currentMood.value
                if (currentMood != null) {
                    deleteMoodUseCase(currentMood.id)
                } else {
                    Result.failure(Exception("Нет записи для удаления"))
                }
            },
            onSuccess = {
                _successMessage.value = "Запись успешно удалена"
            },
            onError = { message ->
                _errorMessage.value = "Ошибка при удалении записи: $message"
            }
        )
    }
    
    /**
     * Добавляет пользовательскую эмоцию в базу данных
     */
    fun addCustomEmotion(name: String) {
        executeWithResult(
            operation = {
                val customMood = com.emonotion.app.domain.model.CustomMood(
                    id = UUID.randomUUID().toString(),
                    name = name.trim()
                )
                addCustomMoodUseCase(customMood)
            },
            onSuccess = {
                // Добавляем эмоцию в текущий список
                addEmotion(name)
                // Обновляем список пользовательских эмоций
                loadCustomElements()
                _successMessage.value = "Эмоция '$name' добавлена"
            },
            onError = { message ->
                _errorMessage.value = "Ошибка при добавлении эмоции: $message"
            }
        )
    }
    
    /**
     * Добавляет пользовательскую активность в базу данных
     */
    fun addCustomActivity(name: String) {
        executeWithResult(
            operation = {
                val customActivity = com.emonotion.app.domain.model.CustomActivity(
                    id = UUID.randomUUID().toString(),
                    name = name.trim()
                )
                addCustomActivityUseCase(customActivity)
            },
            onSuccess = {
                // Добавляем активность в текущий список
                addActivity(name)
                // Обновляем список пользовательских активностей
                loadCustomElements()
                _successMessage.value = "Активность '$name' добавлена"
            },
            onError = { message ->
                _errorMessage.value = "Ошибка при добавлении активности: $message"
            }
        )
    }
    
    /**
     * Получает доступные типы настроений
     */
    fun getMoodTypes(): List<MoodType> {
        return MoodType.values().toList()
    }
}
