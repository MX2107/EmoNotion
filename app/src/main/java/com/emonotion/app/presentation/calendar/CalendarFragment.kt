package com.emonotion.app.presentation.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.emonotion.app.R
import com.emonotion.app.databinding.FragmentCalendarBinding
import com.emonotion.app.utils.StreakUiHelper
import com.emonotion.app.domain.model.MoodType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * Экран календаря настроений
 */
@AndroidEntryPoint
class CalendarFragment : Fragment() {
    
    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: CalendarViewModel by viewModels()
    private lateinit var calendarAdapter: CalendarAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Сначала загружаем данные, затем настраиваем UI
        viewModel.loadMoodsForMonth()
        setupUI()
        observeViewModel()
    }
    
    override fun onResume() {
        super.onResume()
        // Обновляем данные календаря при возвращении на экран
        viewModel.refreshCalendarData()
    }
    
    private fun setupUI() {
        binding.apply {
            // Настройка RecyclerView для календаря
            calendarAdapter = CalendarAdapter { date ->
                openDailyEntry(date)
            }
            
            calendarGrid.layoutManager = GridLayoutManager(context, 7)
            calendarGrid.adapter = calendarAdapter
            
            // Настройка кнопок навигации по месяцам
            previousMonthButton.setOnClickListener {
                viewModel.previousMonth()
            }
            
            nextMonthButton.setOnClickListener {
                viewModel.nextMonth()
            }
            
            // Инициализация календаря текущими данными
            updateCalendarGrid(viewModel.moods.value)
            updateCalendarDisplay()
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentMonth.collect { _ ->
                updateCalendarDisplay()
                // Обновляем календарь при изменении месяца
                updateCalendarGrid(viewModel.moods.value)
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.moods.collect { moods ->
                updateCalendarGrid(moods)
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selectedDate.collect { date ->
                updateSelectedDateDisplay(date)
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.userStats.collect { stats ->
                updateStreakDisplay(stats)
            }
        }
        
        // Инициализируем календарь при запуске
        updateCalendarGrid(viewModel.moods.value)
    }
    
    private fun updateCalendarDisplay() {
        binding.apply {
            // Обновляем заголовок месяца и года
            currentMonthText.text = viewModel.getMonthYearDisplay()
        }
    }
    
    private fun updateCalendarGrid(moods: List<com.emonotion.app.domain.model.MoodEntry>) {
        val calendar = Calendar.getInstance()
        calendar.set(viewModel.currentYear.value, viewModel.currentMonth.value, 1)
        
        val days = calendarAdapter.generateCalendarDays(
            viewModel.currentYear.value,
            viewModel.currentMonth.value
        )
        
        android.util.Log.d("CalendarFragment", "Обновление сетки календаря: ${viewModel.currentYear.value}-${viewModel.currentMonth.value}")
        android.util.Log.d("CalendarFragment", "Сгенерировано дней: ${days.size}, получено настроений: ${moods.size}")
        
        calendarAdapter.updateData(days, moods)
    }
    
    private fun updateSelectedDateDisplay(date: String) {
        // Обновляем выбранную дату в UI - можно выделить выбранную дату в календаре
        // Убрали всплывающее уведомление по требованию пользователя
        // Визуальное выделение выбранной даты будет реализовано в будущей версии
        android.util.Log.d("CalendarFragment", "Выбрана дата: $date")
    }
    
    private fun updateStreakDisplay(stats: com.emonotion.app.domain.model.UserStats) {
        android.util.Log.d(
            "CalendarFragment",
            "updateStreakDisplay: streak=${stats.currentStreak}, hasEntryToday=${stats.hasEntryToday}"
        )
        val streakCard = binding.root.findViewById<android.widget.LinearLayout>(R.id.streak_card)
        val streakIconContainer = binding.root.findViewById<android.widget.LinearLayout>(R.id.streak_icon_container)
        val streakLabelText = binding.root.findViewById<android.widget.TextView>(R.id.streak_label_text)
        val streakCountText = binding.root.findViewById<android.widget.TextView>(R.id.streak_count_text)
        val streakMessageText = binding.root.findViewById<android.widget.TextView>(R.id.streak_message_text)
        val streakFlame = binding.root.findViewById<android.widget.ImageView>(R.id.streak_flame_icon)

        if (!StreakUiHelper.shouldShowStreakBadge(stats)) {
            streakCard?.visibility = View.GONE
            return
        }

        streakCard?.visibility = View.VISIBLE
        val streak = stats.currentStreak
        val atRisk = StreakUiHelper.isStreakAtRisk(stats)
        streakCountText?.text = "${streak} ${StreakUiHelper.dayWord(requireContext(), streak)}"
        streakMessageText?.text = StreakUiHelper.streakMessage(requireContext(), streak, atRisk)
        val activeToday = StreakUiHelper.isStreakActiveToday(stats)
        StreakUiHelper.applyCalendarStreak(
            streakCard,
            streakIconContainer,
            streakFlame,
            streakLabelText,
            streakCountText,
            streakMessageText,
            activeToday
        )
    }
    
    private fun openDailyEntry(date: String) {
        // Открываем страницу дневника с выбранной датой
        val bundle = Bundle().apply {
            putString("date", date)
        }
        findNavController().navigate(R.id.action_calendarFragment_to_dailyEntryFragment, bundle)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
