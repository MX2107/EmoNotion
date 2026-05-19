package com.emonotion.app.presentation.calendar

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.emonotion.app.domain.model.MoodEntry
import com.emonotion.app.domain.model.UserStats
import com.emonotion.app.domain.usecase.analytics.GetUserStatsUseCase
import com.emonotion.app.domain.usecase.mood.GetMoodsByDateRangeUseCase
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
 * ViewModel для календаря настроений
 */
@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val getMoodsByDateRangeUseCase: GetMoodsByDateRangeUseCase,
    private val getUserStatsUseCase: GetUserStatsUseCase
) : BaseViewModel() {
    
    // Состояния UI
    private val _selectedDate = MutableStateFlow(
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    )
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()
    
    private val _moods = MutableStateFlow<List<MoodEntry>>(emptyList())
    val moods: StateFlow<List<MoodEntry>> = _moods.asStateFlow()
    
    private val _currentMonth = MutableStateFlow(
        Calendar.getInstance().get(Calendar.MONTH)
    )
    val currentMonth: StateFlow<Int> = _currentMonth.asStateFlow()
    
    private val _currentYear = MutableStateFlow(
        Calendar.getInstance().get(Calendar.YEAR)
    )
    val currentYear: StateFlow<Int> = _currentYear.asStateFlow()
    
    private val _userStats = MutableStateFlow<UserStats>(UserStats())
    val userStats: StateFlow<UserStats> = _userStats.asStateFlow()
    
    // Кэш для загруженных данных
    private val cachedMoods = mutableMapOf<String, List<MoodEntry>>()
    
    init {
        // Инициализация при создании ViewModel
        loadMoodsForMonth()
        loadUserStats()
    }
    
    /**
     * Выбирает дату в календаре
     */
    fun selectDate(date: String) {
        _selectedDate.value = date
    }
    
    /**
     * Переключает на следующий месяц
     */
    fun nextMonth() {
        val calendar = Calendar.getInstance()
        calendar.set(_currentYear.value, _currentMonth.value, 1)
        calendar.add(Calendar.MONTH, 1)
        
        _currentMonth.value = calendar.get(Calendar.MONTH)
        _currentYear.value = calendar.get(Calendar.YEAR)
        
        loadMoodsForMonth()
    }
    
    /**
     * Обновляет данные календаря (можно вызывать извне)
     */
    fun refreshCalendarData() {
        // Очищаем кэш для текущего месяца при принудительном обновлении
        val cacheKey = "${_currentYear.value}-${_currentMonth.value}"
        cachedMoods.remove(cacheKey)
        loadMoodsForMonth()
    }
    
    /**
     * Переключает на предыдущий месяц
     */
    fun previousMonth() {
        val calendar = Calendar.getInstance()
        calendar.set(_currentYear.value, _currentMonth.value, 1)
        calendar.add(Calendar.MONTH, -1)
        
        _currentMonth.value = calendar.get(Calendar.MONTH)
        _currentYear.value = calendar.get(Calendar.YEAR)
        
        loadMoodsForMonth()
    }
    
    /**
     * Загружает записи о настроении за текущий месяц и соседние дни
     */
    fun loadMoodsForMonth() {
        val cacheKey = "${_currentYear.value}-${_currentMonth.value}"
        
        // Проверяем кэш сначала
        cachedMoods[cacheKey]?.let { cachedList ->
            _moods.value = cachedList
            Log.d("CalendarViewModel", "Использованы кэшированные данные за период: $cacheKey, записей: ${cachedList.size}")
            return
        }
        
        executeWithLoading {
            viewModelScope.launch {
                val calendar = Calendar.getInstance()
                calendar.set(_currentYear.value, _currentMonth.value, 1)
                
                // Определяем первый день месяца
                val firstDayOfMonth = calendar.get(Calendar.DAY_OF_WEEK)
                
                // Calendar.DAY_OF_WEEK: 1=Воскресенье, 2=Понедельник, ..., 7=Суббота
                // Для отображения с понедельника нужно сдвинуть на 1
                val adjustedFirstDay = if (firstDayOfMonth == Calendar.SUNDAY) 7 else firstDayOfMonth - 1
                
                // Откручиваем назад к первому дню предыдущего месяца, который отображается в календаре
                calendar.add(Calendar.DAY_OF_MONTH, -(adjustedFirstDay - 1))
                val firstDay = calendar.timeInMillis
                val firstDayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(firstDay))
                
                // Возвращаемся к первому дню текущего месяца
                calendar.set(_currentYear.value, _currentMonth.value, 1)
                
                // Переходим к последнему дню месяца
                calendar.add(Calendar.MONTH, 1)
                calendar.add(Calendar.DAY_OF_MONTH, -1)
                val lastDayOfMonth = calendar.timeInMillis
                
                // Добавляем дни до конца недели (до 42 дней = 6 недель)
                val daysInMonth = calendar.get(Calendar.DAY_OF_MONTH)
                val totalDaysShown = adjustedFirstDay - 1 + daysInMonth
                val remainingDays = 42 - totalDaysShown
                calendar.add(Calendar.DAY_OF_MONTH, remainingDays)
                
                val lastDay = calendar.timeInMillis
                val lastDayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(lastDay))
                
                Log.d("CalendarViewModel", "Загрузка данных за период: $firstDayStr - $lastDayStr")
                
                // Загружаем данные и кэшируем их
                try {
                    getMoodsByDateRangeUseCase(firstDayStr, lastDayStr).collect { moodList ->
                        _moods.value = moodList
                        cachedMoods[cacheKey] = moodList
                        Log.d("CalendarViewModel", "Загружено и закэшировано записей: ${moodList.size}")
                    }
                } catch (e: Exception) {
                    Log.e("CalendarViewModel", "Ошибка загрузки данных за период: $firstDayStr - $lastDayStr", e)
                    _moods.value = emptyList()
                }
            }
        }
    }
    
    /**
     * Получает настроение для конкретной даты
     */
    fun getMoodForDate(date: String): MoodEntry? {
        return _moods.value.find { it.date == date }
    }
    
    /**
     * Возвращает форматированное название месяца и года
     */
    fun getMonthYearDisplay(): String {
        val calendar = Calendar.getInstance()
        calendar.set(_currentYear.value, _currentMonth.value, 1)
        
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }
    
    /**
     * Загружает статистику пользователя
     */
    private fun loadUserStats() {
        viewModelScope.launch {
            getUserStatsUseCase().collect { stats ->
                android.util.Log.d("CalendarViewModel", "loadUserStats: currentStreak=${stats.currentStreak}, longestStreak=${stats.longestStreak}")
                _userStats.value = stats
            }
        }
    }
}
