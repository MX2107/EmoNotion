package com.emonotion.app.presentation.calendar

import androidx.lifecycle.viewModelScope
import com.emonotion.app.domain.model.MoodEntry
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
    private val getMoodsByDateRangeUseCase: GetMoodsByDateRangeUseCase
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
     * Загружает записи о настроении за текущий месяц
     */
    fun loadMoodsForMonth() {
        executeWithLoading {
            viewModelScope.launch {
                val calendar = Calendar.getInstance()
                calendar.set(_currentYear.value, _currentMonth.value, 1)
                
                val firstDay = calendar.timeInMillis
                
                calendar.add(Calendar.MONTH, 1)
                calendar.add(Calendar.DAY_OF_MONTH, -1)
                val lastDay = calendar.timeInMillis
                
                getMoodsByDateRangeUseCase(firstDay, lastDay).collect { moodList ->
                    _moods.value = moodList
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
}
