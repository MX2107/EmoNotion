package com.emonotion.app.ui.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.emonotion.app.R
import java.text.SimpleDateFormat
import java.util.*

class CalendarFragment : Fragment() {
    
    private lateinit var streakCard: LinearLayout
    private lateinit var streakCountText: TextView
    private lateinit var currentMonthText: TextView
    private lateinit var previousMonthButton: ImageButton
    private lateinit var nextMonthButton: ImageButton
    private lateinit var calendarGrid: RecyclerView
    
    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    // В реальном приложении данные будут загружаться из базы данных
    private val moodEntries = emptyMap<String, String>()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupClickListeners()
        updateCalendar()
        updateStreak()
    }
    
    private fun initViews(view: View) {
        try {
            streakCard = view.findViewById(R.id.streak_card)
            streakCountText = view.findViewById(R.id.streak_count_text)
            currentMonthText = view.findViewById(R.id.current_month_text)
            previousMonthButton = view.findViewById(R.id.previous_month_button)
            nextMonthButton = view.findViewById(R.id.next_month_button)
            calendarGrid = view.findViewById(R.id.calendar_grid)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        // Настройка RecyclerView для календаря
        calendarGrid.layoutManager = GridLayoutManager(context, 7)
    }
    
    private fun setupClickListeners() {
        previousMonthButton.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            updateCalendar()
        }
        
        nextMonthButton.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            updateCalendar()
        }
    }
    
    private fun updateCalendar() {
        currentMonthText.text = dateFormat.format(calendar.time)
        
        val days = generateCalendarDays()
        val adapter = CalendarAdapter(days) { day ->
            if (day != null) {
                onDaySelected(day)
            }
        }
        calendarGrid.adapter = adapter
    }
    
    private fun generateCalendarDays(): List<Int?> {
        val days = mutableListOf<Int?>()
        
        // Устанавливаем календарь на начало месяца
        val tempCalendar = calendar.clone() as Calendar
        tempCalendar.set(Calendar.DAY_OF_MONTH, 1)
        
        val firstDayOfWeek = tempCalendar.get(Calendar.DAY_OF_WEEK)
        val daysInMonth = tempCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        // Добавляем пустые ячейки для дней до начала месяца
        for (i in 1 until firstDayOfWeek) {
            days.add(null)
        }
        
        // Добавляем дни месяца
        for (day in 1..daysInMonth) {
            days.add(day)
        }
        
        return days
    }
    
    private fun onDaySelected(day: Int) {
        val tempCalendar = calendar.clone() as Calendar
        tempCalendar.set(Calendar.DAY_OF_MONTH, day)
        val dateStr = dayFormat.format(tempCalendar.time)
        
        val mood = moodEntries[dateStr]
        if (mood != null) {
            // Здесь можно показать детали настроения для выбранного дня
            // Например, открыть диалог или другой фрагмент
        }
    }
    
    private fun updateStreak() {
        val streak = calculateStreak()
        if (streak > 0) {
            streakCard.visibility = View.VISIBLE
            streakCountText.text = "$streak ${getStreakLabel(streak)}${getString(R.string.days_in_a_row)}"
        } else {
            streakCard.visibility = View.GONE
        }
    }
    
    private fun calculateStreak(): Int {
        // Простой расчет серии - в реальном приложении будет более сложная логика
        var streak = 0
        val today = Calendar.getInstance()
        
        for (i in 0..30) { // Проверяем последние 30 дней
            val checkDate = today.clone() as Calendar
            checkDate.add(Calendar.DAY_OF_MONTH, -i)
            val dateStr = dayFormat.format(checkDate.time)
            
            if (moodEntries.containsKey(dateStr)) {
                streak++
            } else if (i > 0) { // Пропускаем сегодняшний день, если нет записи
                break
            }
        }
        
        return streak
    }
    
    private fun getStreakLabel(streak: Int): String {
        return when {
            streak == 1 -> "1 "
            streak < 5 -> "$streak "
            else -> "$streak "
        }
    }
    
    inner class CalendarAdapter(
        private val days: List<Int?>,
        private val onDayClick: (Int?) -> Unit
    ) : RecyclerView.Adapter<CalendarAdapter.ViewHolder>() {
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_calendar_day, parent, false)
            return ViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val day = days[position]
            holder.bind(day)
        }
        
        override fun getItemCount(): Int = days.size
        
        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val dayText: TextView = itemView.findViewById(R.id.day_text)
            
            fun bind(day: Int?) {
                if (day == null) {
                    dayText.text = ""
                    dayText.background = null
                    dayText.setOnClickListener(null)
                } else {
                    dayText.text = day.toString()
                    
                    // Проверяем, есть ли запись для этого дня
                    val tempCalendar = calendar.clone() as Calendar
                    tempCalendar.set(Calendar.DAY_OF_MONTH, day)
                    val dateStr = dayFormat.format(tempCalendar.time)
                    
                    val mood = moodEntries[dateStr]
                    if (mood != null) {
                        // Устанавливаем цвет настроения
                        val moodColor = getMoodColor(mood)
                        dayText.setBackgroundColor(resources.getColor(moodColor, null))
                        dayText.setTextColor(resources.getColor(android.R.color.white, null))
                        
                        // Проверяем, является ли сегодня днем
                        val today = Calendar.getInstance()
                        val isToday = today.get(Calendar.DAY_OF_MONTH) == day &&
                                today.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
                                today.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
                        
                        if (isToday) {
                            dayText.background = resources.getDrawable(R.drawable.today_background, null)
                        }
                    } else {
                        dayText.setBackgroundColor(resources.getColor(R.color.muted, null))
                        dayText.setTextColor(resources.getColor(R.color.foreground, null))
                    }
                    
                    dayText.setOnClickListener {
                        onDayClick(day)
                    }
                }
            }
            
            private fun getMoodColor(mood: String): Int {
                return when (mood) {
                    "great" -> R.color.mood_great
                    "good" -> R.color.mood_good
                    "neutral" -> R.color.mood_neutral
                    "bad" -> R.color.mood_bad
                    "terrible" -> R.color.mood_terrible
                    else -> R.color.muted
                }
            }
        }
    }
}
