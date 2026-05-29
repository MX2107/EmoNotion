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
import com.emonotion.app.domain.usecase.draft.SaveDraftUseCase
import com.emonotion.app.domain.usecase.draft.GetDraftByDateUseCase
import com.emonotion.app.domain.usecase.draft.DeleteDraftUseCase
import com.emonotion.app.presentation.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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
    private val getCustomActivitiesUseCase: GetCustomActivitiesUseCase,
    private val saveDraftUseCase: SaveDraftUseCase,
    private val getDraftByDateUseCase: GetDraftByDateUseCase,
    private val deleteDraftUseCase: DeleteDraftUseCase
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
    
    private val _hasDraft = MutableStateFlow(false)
    val hasDraft: StateFlow<Boolean> = _hasDraft.asStateFlow()
    
    private var draftSaveJob: kotlinx.coroutines.Job? = null
    
    init {
        // Автоматическое сохранение черновика при изменении данных
        viewModelScope.launch {
            combine(
                _selectedMoodType,
                _selectedIntensity,
                _emotions,
                _activities,
                _notes
            ) { mood, intensity, emotions, activities, notes ->
                // Сохраняем только если есть изменения
                Triple(mood, Triple(intensity, emotions, activities), notes)
            }.collect { (mood, data, notes) ->
                // Сохраняем только если есть данные (не пустой черновик)
                if (mood != null || data.second.isNotEmpty() || data.third.isNotEmpty() || notes.isNotBlank()) {
                    saveDraft()
                }
            }
        }
    }
    
    /**
     * Устанавливает дату для записи
     */
    fun setDate(date: String) {
        _selectedDate.value = date
        loadCustomElements()
        // Загружаем черновик и сохраненную запись
        viewModelScope.launch {
            // Сначала загружаем черновик
            loadDraftForDate(date)
            // Загружаем сохраненную запись для отображения кнопки удаления
            // Но не перезаписываем данные черновика
            loadMoodForDateWithoutOverwriting(date)
        }
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
            } catch (e: kotlinx.coroutines.CancellationException) {
                android.util.Log.d("DailyEntryViewModel", "Загрузка пользовательских эмоций отменена")
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
            } catch (e: kotlinx.coroutines.CancellationException) {
                android.util.Log.d("DailyEntryViewModel", "Загрузка пользовательских активностей отменена")
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
     * Загружает черновик для указанной даты
     */
    private suspend fun loadDraftForDate(date: String) {
        try {
            val draft = getDraftByDateUseCase(date)
            _hasDraft.value = draft != null
            if (draft != null) {
                // Загружаем данные из черновика
                android.util.Log.d("DailyEntryViewModel", "Загрузка черновика для даты: $date, mood: ${draft.mood}, emotions: ${draft.emotions}, activities: ${draft.activities}, notes: ${draft.notes}")
                _selectedMoodType.value = draft.mood
                _selectedIntensity.value = draft.intensity
                _emotions.value = draft.emotions
                _activities.value = draft.activities
                _notes.value = draft.notes ?: ""
                android.util.Log.d("DailyEntryViewModel", "Загружен черновик для даты: $date")
            } else {
                android.util.Log.d("DailyEntryViewModel", "Черновик не найден для даты: $date")
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            android.util.Log.d("DailyEntryViewModel", "Загрузка черновика отменена")
        } catch (e: Exception) {
            android.util.Log.e("DailyEntryViewModel", "Ошибка загрузки черновика", e)
        }
    }
    
    /**
     * Загружает настроение для указанной даты
     */
    private fun loadMoodForDate(date: String) {
        executeWithLoading {
            viewModelScope.launch {
                try {
                    getMoodByDateUseCase(date).collect { mood ->
                        if (mood != null) {
                            // Всегда устанавливаем currentMood для отображения кнопки удаления
                            _currentMood.value = mood
                            _isEditing.value = true
                            // Если нет черновика, загружаем данные из сохраненной записи
                            if (!_hasDraft.value) {
                                _selectedMoodType.value = mood.mood
                                _selectedIntensity.value = mood.intensity
                                _emotions.value = mood.emotions
                                _activities.value = mood.activities
                                _notes.value = mood.notes ?: ""
                            }
                        } else {
                            _currentMood.value = null
                            // Если нет черновика, сбрасываем форму
                            if (!_hasDraft.value) {
                                _selectedMoodType.value = null
                                _selectedIntensity.value = 3
                                _emotions.value = emptyList()
                                _activities.value = emptyList()
                                _notes.value = ""
                            }
                            // Если есть черновик, загружаем его данные
                            loadDraftForDate(date)
                            _isEditing.value = _hasDraft.value
                        }
                    }
                } catch (e: kotlinx.coroutines.CancellationException) {
                    android.util.Log.d("DailyEntryViewModel", "Загрузка настроения отменена")
                } catch (e: Exception) {
                    android.util.Log.e("DailyEntryViewModel", "Ошибка загрузки настроения", e)
                }
            }
        }
    }

    /**
     * Загружает настроение для указанной даты без перезаписи данных черновика
     */
    private fun loadMoodForDateWithoutOverwriting(date: String) {
        executeWithLoading {
            viewModelScope.launch {
                try {
                    getMoodByDateUseCase(date).collect { mood ->
                        if (mood != null) {
                            // Устанавливаем currentMood для отображения кнопки удаления
                            _currentMood.value = mood
                            _isEditing.value = true
                            // НЕ перезаписываем данные черновика
                        } else {
                            _currentMood.value = null
                            // НЕ перезаписываем данные черновика
                            _isEditing.value = _hasDraft.value
                        }
                    }
                } catch (e: kotlinx.coroutines.CancellationException) {
                    android.util.Log.d("DailyEntryViewModel", "Загрузка настроения отменена")
                } catch (e: Exception) {
                    android.util.Log.e("DailyEntryViewModel", "Ошибка загрузки настроения", e)
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
        
        // Валидация даты - нельзя создавать записи на будущие даты
        val selectedDate = _selectedDate.value
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        if (selectedDate > today) {
            _errorMessage.value = "Нельзя создавать записи на будущие даты"
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
                    date = selectedDate
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
                // Удаляем черновик после успешного сохранения
                viewModelScope.launch {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO + kotlinx.coroutines.NonCancellable) {
                        try {
                            deleteDraftUseCase(selectedDate)
                            _hasDraft.value = false
                            android.util.Log.d("DailyEntryViewModel", "Черновик удален после сохранения записи для даты: $selectedDate")
                        } catch (e: Exception) {
                            android.util.Log.e("DailyEntryViewModel", "Ошибка удаления черновика после сохранения", e)
                        }
                    }
                }
            }
        )
    }
    
    /**
     * Сохраняет текущее состояние как черновик
     */
    fun saveDraft() {
        val moodType = _selectedMoodType.value
        // Сохраняем черновик при любом изменении
        viewModelScope.launch {
            // Используем NonCancellable чтобы гарантировать сохранение даже при отмене coroutine
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO + kotlinx.coroutines.NonCancellable) {
                try {
                    // Сначала удаляем старый черновик для этой даты
                    deleteDraftUseCase(_selectedDate.value)
                    // Создаем новый черновик
                    val draftEntry = MoodEntry(
                        id = UUID.randomUUID().toString(),
                        mood = moodType,
                        intensity = _selectedIntensity.value,
                        emotions = _emotions.value,
                        activities = _activities.value,
                        notes = _notes.value.takeIf { it.isNotBlank() },
                        timestamp = System.currentTimeMillis(),
                        date = _selectedDate.value
                    )
                    android.util.Log.d("DailyEntryViewModel", "Сохранение черновика для даты: ${_selectedDate.value}, mood: $moodType, emotions: ${_emotions.value}, activities: ${_activities.value}, notes: ${_notes.value}")
                    saveDraftUseCase(draftEntry)
                    _hasDraft.value = true
                    android.util.Log.d("DailyEntryViewModel", "Черновик сохранен для даты: ${_selectedDate.value}")
                } catch (e: Exception) {
                    android.util.Log.e("DailyEntryViewModel", "Ошибка сохранения черновика", e)
                }
            }
        }
    }
    
    /**
     * Удаляет черновик для текущей даты
     */
    fun deleteDraft() {
        viewModelScope.launch {
            try {
                deleteDraftUseCase(_selectedDate.value)
                _hasDraft.value = false
                android.util.Log.d("DailyEntryViewModel", "Черновик удален для даты: ${_selectedDate.value}")
            } catch (e: Exception) {
                android.util.Log.e("DailyEntryViewModel", "Ошибка удаления черновика", e)
            }
        }
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
                // Удаляем черновик после успешного удаления записи
                viewModelScope.launch {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO + kotlinx.coroutines.NonCancellable) {
                        try {
                            deleteDraftUseCase(_selectedDate.value)
                            _hasDraft.value = false
                            android.util.Log.d("DailyEntryViewModel", "Черновик удален после удаления записи для даты: ${_selectedDate.value}")
                        } catch (e: Exception) {
                            android.util.Log.e("DailyEntryViewModel", "Ошибка удаления черновика после удаления записи", e)
                        }
                    }
                }
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
