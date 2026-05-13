package com.emonotion.app.presentation.home

import androidx.lifecycle.viewModelScope
import com.emonotion.app.domain.model.MoodEntry
import com.emonotion.app.domain.model.Note
import com.emonotion.app.domain.model.Task
import com.emonotion.app.domain.usecase.mood.GetTodayMoodUseCase
import com.emonotion.app.domain.usecase.mood.GetMoodByDateUseCase
import com.emonotion.app.domain.usecase.note.GetNotesUseCase
import com.emonotion.app.domain.usecase.task.GetTasksUseCase
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
 * ViewModel для главного экрана приложения
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTodayMoodUseCase: GetTodayMoodUseCase,
    private val getMoodByDateUseCase: GetMoodByDateUseCase,
    private val getNotesUseCase: GetNotesUseCase,
    private val getTasksUseCase: GetTasksUseCase
) : BaseViewModel() {
    
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    
    // Состояния UI
    private val _selectedDate = MutableStateFlow(today)
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()
    
    private val _todayMood = MutableStateFlow<MoodEntry?>(null)
    val todayMood: StateFlow<MoodEntry?> = _todayMood.asStateFlow()
    
    private val _selectedDateMood = MutableStateFlow<MoodEntry?>(null)
    val selectedDateMood: StateFlow<MoodEntry?> = _selectedDateMood.asStateFlow()
    
    private val _todayNotes = MutableStateFlow<List<Note>>(emptyList())
    val todayNotes: StateFlow<List<Note>> = _todayNotes.asStateFlow()
    
    private val _todayTasks = MutableStateFlow<List<Task>>(emptyList())
    val todayTasks: StateFlow<List<Task>> = _todayTasks.asStateFlow()
    
    private val _incompleteTasksCount = MutableStateFlow(0)
    val incompleteTasksCount: StateFlow<Int> = _incompleteTasksCount.asStateFlow()
    
    /**
     * Устанавливает выбранную дату
     */
    fun setSelectedDate(date: String) {
        _selectedDate.value = date
        loadMoodForSelectedDate()
    }
    
    /**
     * Загружает запись о настроении для выбранной даты
     */
    private fun loadMoodForSelectedDate() {
        executeWithLoading {
            viewModelScope.launch {
                getMoodByDateUseCase(_selectedDate.value).collect { mood ->
                    _selectedDateMood.value = mood
                }
            }
        }
    }
    
    /**
     * Загружает данные для главного экрана
     */
    fun loadHomeData() {
        executeWithLoading {
            viewModelScope.launch {
                // Загружаем данные параллельно
                combine(
                    getTodayMoodUseCase(),
                    getNotesUseCase.getNotesByDate(today),
                    getTasksUseCase(false) // незавершенные задачи
                ) { mood, notes, tasks ->
                    Triple(mood, notes, tasks)
                }.collect { (mood, notes, tasks) ->
                    _todayMood.value = mood
                    _todayNotes.value = notes
                    _todayTasks.value = tasks.filter { it.date == today }
                    _incompleteTasksCount.value = tasks.size
                }
                
                // Загружаем запись для выбранной даты
                loadMoodForSelectedDate()
            }
        }
    }
    
    /**
     * Обновляет данные
     */
    fun refreshData() {
        loadHomeData()
    }
}
