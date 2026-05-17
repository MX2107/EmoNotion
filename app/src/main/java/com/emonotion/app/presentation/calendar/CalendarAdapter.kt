package com.emonotion.app.presentation.calendar

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.emonotion.app.R
import com.emonotion.app.domain.model.MoodEntry
import com.emonotion.app.domain.model.MoodType
import java.text.SimpleDateFormat
import java.util.*

/**
 * Адаптер для отображения дней календаря с настроениями
 */
class CalendarAdapter(
    private val onDateClick: (String) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.CalendarDayViewHolder>() {
    
    private var days: List<CalendarDay> = emptyList()
    private var moods: List<MoodEntry> = emptyList()
    
    data class CalendarDay(
        val date: String,
        val dayNumber: Int,
        val isCurrentMonth: Boolean,
        val isToday: Boolean,
        val isFuture: Boolean = false
    )
    
    class CalendarDayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayText: TextView = itemView.findViewById(R.id.day_text)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarDayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)
        return CalendarDayViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: CalendarDayViewHolder, position: Int) {
        val day = days[position]
        
        holder.dayText.text = day.dayNumber.toString()
        
        // Определяем настроение для этой даты
        val mood = moods.find { it.date == day.date }
        
        // Устанавливаем цвет фона в зависимости от настроения
        when (mood?.mood) {
            MoodType.GREAT -> {
                holder.dayText.setBackgroundResource(R.drawable.mood_indicator_great)
            }
            MoodType.GOOD -> {
                holder.dayText.setBackgroundResource(R.drawable.mood_indicator_good)
            }
            MoodType.NEUTRAL -> {
                holder.dayText.setBackgroundResource(R.drawable.mood_indicator_neutral)
            }
            MoodType.BAD -> {
                holder.dayText.setBackgroundResource(R.drawable.mood_indicator_bad)
            }
            MoodType.TERRIBLE -> {
                holder.dayText.setBackgroundResource(R.drawable.mood_indicator_terrible)
            }
            MoodType.HAPPY -> {
                holder.dayText.setBackgroundResource(R.drawable.mood_indicator_great)
            }
            MoodType.SAD -> {
                holder.dayText.setBackgroundResource(R.drawable.mood_indicator_bad)
            }
            MoodType.ANGRY -> {
                holder.dayText.setBackgroundResource(R.drawable.mood_indicator_terrible)
            }
            MoodType.ANXIOUS -> {
                holder.dayText.setBackgroundResource(R.drawable.mood_indicator_neutral)
            }
            else -> {
                holder.dayText.background = null
            }
        }
        
        // Устанавливаем прозрачность для дней не текущего месяца и будущих дней
        holder.dayText.alpha = when {
            !day.isCurrentMonth -> 0.3f
            day.isFuture -> 0.5f
            else -> 1.0f
        }

        // Обработчик клика
        holder.itemView.setOnClickListener {
            if (day.isCurrentMonth && !day.isFuture) {
                onDateClick(day.date)
            }
        }
    }
    
    override fun getItemCount(): Int = days.size
    
    fun updateData(newDays: List<CalendarDay>, newMoods: List<MoodEntry>) {
        days = newDays
        moods = newMoods
        Log.d("CalendarAdapter", "Обновление данных: дней=${newDays.size}, настроений=${newMoods.size}")
        notifyDataSetChanged()
    }
    
    /**
     * Генерирует дни для календаря на указанный месяц и год
     */
    fun generateCalendarDays(year: Int, month: Int): List<CalendarDay> {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, 1)
        
        val firstDayOfMonth = calendar.get(Calendar.DAY_OF_WEEK)
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        val today = Calendar.getInstance()
        val todayYear = today.get(Calendar.YEAR)
        val todayMonth = today.get(Calendar.MONTH)
        val todayDay = today.get(Calendar.DAY_OF_MONTH)
        
        val days = mutableListOf<CalendarDay>()
        
        // Добавляем дни предыдущего месяца
        val prevMonthCalendar = Calendar.getInstance()
        prevMonthCalendar.set(year, month - 1, 1)
        val prevMonthDays = prevMonthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        // Calendar.DAY_OF_WEEK: 1=Воскресенье, 2=Понедельник, ..., 7=Суббота
        // Для отображения с понедельника нужно сдвинуть на 1
        val adjustedFirstDay = if (firstDayOfMonth == Calendar.SUNDAY) 7 else firstDayOfMonth - 1
        
        for (i in adjustedFirstDay - 1 downTo 1) {
            val day = prevMonthDays - i + 1
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
                Calendar.getInstance().apply {
                    set(year, month - 1, day)
                }.time
            )
            days.add(CalendarDay(date, day, false, false))
        }
        
        // Добавляем дни текущего месяца
        for (day in 1..daysInMonth) {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
                Calendar.getInstance().apply {
                    set(year, month, day)
                }.time
            )
            val isToday = year == todayYear && month == todayMonth && day == todayDay
            val isFuture = year > todayYear || (year == todayYear && month > todayMonth) || (year == todayYear && month == todayMonth && day > todayDay)
            days.add(CalendarDay(date, day, true, isToday, isFuture))
        }
        
        // Добавляем дни следующего месяца для заполнения сетки до 42 дней (6 недель)
        val remainingDays = 42 - days.size
        for (day in 1..remainingDays) {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
                Calendar.getInstance().apply {
                    set(year, month + 1, day)
                }.time
            )
            val isFuture = true // Дни следующего месяца всегда будущие
            days.add(CalendarDay(date, day, false, false, isFuture))
        }
        
        Log.d("CalendarAdapter", "Сгенерировано дней для календаря $year-${month+1}: ${days.size}")
        return days
    }
}
