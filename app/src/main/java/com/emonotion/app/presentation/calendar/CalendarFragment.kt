package com.emonotion.app.presentation.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.emonotion.app.databinding.FragmentCalendarBinding
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
        setupUI()
        observeViewModel()
        viewModel.loadMoodsForMonth()
    }
    
    private fun setupUI() {
        binding.apply {
            // Настройка кнопок навигации по месяцам
            previousMonthButton.setOnClickListener {
                viewModel.previousMonth()
            }
            
            nextMonthButton.setOnClickListener {
                viewModel.nextMonth()
            }
            
            // Кнопка назад будет добавлена позже
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentMonth.collect { month ->
                updateCalendarDisplay()
            }
        }
        
        // TODO: Обработать обновление календаря
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selectedDate.collect { date ->
                updateSelectedDateDisplay(date)
            }
        }
    }
    
    private fun updateCalendarDisplay() {
        binding.apply {
            // Обновляем заголовок месяца и года
            currentMonthText.text = viewModel.getMonthYearDisplay()
        }
    }
    
    private fun updateCalendarGrid(moods: List<com.emonotion.app.domain.model.MoodEntry>) {
        // TODO: Реализовать отображение календаря с RecyclerView
    }
    
    private fun updateSelectedDateDisplay(date: String) {
        // TODO: Реализовать отображение выбранной даты
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
