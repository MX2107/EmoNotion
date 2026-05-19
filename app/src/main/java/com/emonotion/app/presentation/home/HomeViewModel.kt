package com.emonotion.app.presentation.home

import androidx.lifecycle.viewModelScope
import com.emonotion.app.domain.model.MoodEntry
import com.emonotion.app.domain.model.Note
import com.emonotion.app.domain.model.Task
import com.emonotion.app.domain.model.UserStats
import com.emonotion.app.domain.usecase.analytics.GetUserStatsUseCase
import com.emonotion.app.domain.usecase.mood.GetTodayMoodUseCase
import com.emonotion.app.domain.usecase.mood.GetMoodByDateUseCase
import com.emonotion.app.domain.usecase.note.DeleteNoteUseCase
import com.emonotion.app.domain.usecase.note.GetNotesUseCase
import com.emonotion.app.domain.usecase.task.GetTasksUseCase
import com.emonotion.app.presentation.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
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
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val getTasksUseCase: GetTasksUseCase,
    private val getUserStatsUseCase: GetUserStatsUseCase
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

    private val _userStats = MutableStateFlow<UserStats>(UserStats())
    val userStats: StateFlow<UserStats> = _userStats.asStateFlow()

    private var homeDataCollectionJob: kotlinx.coroutines.Job? = null

    init {
        // Предварительная загрузка данных при создании ViewModel
        loadHomeData()
        loadUserStats()
    }
    
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
        // Отменяем предыдущую подписку если есть
        homeDataCollectionJob?.cancel()

        homeDataCollectionJob = viewModelScope.launch {
            // Сначала загружаем данные из базы для кэша
            val mood = getTodayMoodUseCase().first()
            val notes = getNotesUseCase().first()
            val tasks = getTasksUseCase(false).first()

            _todayMood.value = mood
            _todayNotes.value = notes.sortedByDescending { note -> note.timestamp }
            _todayTasks.value = tasks.filter { task -> task.date == today }
            _incompleteTasksCount.value = tasks.size

            // Затем подписываемся на обновления
            combine(
                getTodayMoodUseCase(),
                getNotesUseCase(),
                getTasksUseCase(false)
            ) { todayMood, todayNotes, todayTasks ->
                Triple(todayMood, todayNotes, todayTasks)
            }.collect { (mood, notes, tasks) ->
                _todayMood.value = mood
                _todayNotes.value = notes.sortedByDescending { note -> note.timestamp }
                _todayTasks.value = tasks.filter { task -> task.date == today }
                _incompleteTasksCount.value = tasks.size
            }

            // Загружаем запись для выбранной даты
            loadMoodForSelectedDate()
        }
    }
    
    /**
     * Обновляет данные
     */
    fun refreshData() {
        loadHomeData()
    }
    
    /**
     * Удаляет заметку
     */
    fun deleteNote(noteId: String) {
        executeWithResult(
            operation = {
                deleteNoteUseCase(noteId)
            },
            onSuccess = {
                loadHomeData()
            }
        )
    }
    
    /**
     * Загружает статистику пользователя
     */
    private fun loadUserStats() {
        viewModelScope.launch {
            getUserStatsUseCase().collect { stats ->
                android.util.Log.d("HomeViewModel", "loadUserStats: currentStreak=${stats.currentStreak}, longestStreak=${stats.longestStreak}")
                _userStats.value = stats
            }
        }
    }
}
