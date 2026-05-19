package com.emonotion.app.presentation.analytics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.emonotion.app.R
import com.emonotion.app.databinding.FragmentAnalyticsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Экран аналитики и статистики
 */
@AndroidEntryPoint
class AnalyticsFragment : Fragment() {
    
    private var _binding: FragmentAnalyticsBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: AnalyticsViewModel by viewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalyticsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
        viewModel.loadAnalyticsData()
    }
    
    private fun setupUI() {
        binding.apply {
            // Кнопка экспорта статистики
            exportButton.setOnClickListener {
                viewModel.exportAnalytics()
            }
            
            // Обработка выбора периода
            val timeRangeGroup = binding.root.findViewById<android.widget.RadioGroup>(R.id.time_range_group)
            timeRangeGroup?.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    R.id.radio_7_days -> viewModel.setPeriod(com.emonotion.app.domain.model.AnalyticsPeriod.WEEK)
                    R.id.radio_14_days -> viewModel.setPeriod(com.emonotion.app.domain.model.AnalyticsPeriod.TWO_WEEKS)
                    R.id.radio_30_days -> viewModel.setPeriod(com.emonotion.app.domain.model.AnalyticsPeriod.MONTH)
                }
            }
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.analyticsData.collect { analytics ->
                analytics?.let {
                    updateAnalyticsDisplay(it)
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                updateLoadingState(isLoading)
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.errorMessage.collect { error ->
                error?.let {
                    android.widget.Toast.makeText(requireContext(), it, android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun updateAnalyticsDisplay(analytics: com.emonotion.app.domain.model.Analytics) {
        binding.apply {
            // Обновляем общую статистику
            val totalEntriesText = binding.root.findViewById<android.widget.TextView>(R.id.total_entries_text)
            val averageMoodText = binding.root.findViewById<android.widget.TextView>(R.id.average_mood_text)
            val currentStreakText = binding.root.findViewById<android.widget.TextView>(R.id.current_streak_text)
            val mostFrequentDayText = binding.root.findViewById<android.widget.TextView>(R.id.most_frequent_day_text)
            
            totalEntriesText?.text = analytics.totalEntries.toString()
            averageMoodText?.text = String.format("%.1f/5", analytics.averageMood)
            currentStreakText?.text = "${analytics.currentStreak} дней"
            
            // Находим самый частый день недели
            val mostFrequentDay = getMostFrequentDayOfWeek()
            mostFrequentDayText?.text = mostFrequentDay
            
            // Обновляем график настроения
            updateMoodChart(analytics.moodDistribution)
            
            // Обновляем статистику эмоций
            updateEmotionChart(analytics.moodDistribution)
            
            // Обновляем статистику активностей
            updateActivityChart(analytics.mostCommonActivities)
        }
    }
    
    private fun getMostFrequentDayOfWeek(): String {
        val days = listOf("Воскресенье", "Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота")
        val calendar = java.util.Calendar.getInstance()
        return days[calendar.get(java.util.Calendar.DAY_OF_WEEK) - 1]
    }
    
    private fun updateMoodChart(moodDistribution: Map<com.emonotion.app.domain.model.MoodType, Int>) {
        val moodChartContainer = binding.root.findViewById<android.widget.LinearLayout>(R.id.mood_chart_container)
        moodChartContainer?.removeAllViews()
        
        if (moodDistribution.isEmpty()) {
            moodChartContainer?.addView(createTextView("Нет данных"))
            return
        }
        
        val moodNames = mapOf(
            com.emonotion.app.domain.model.MoodType.GREAT to "Отлично",
            com.emonotion.app.domain.model.MoodType.GOOD to "Хорошо",
            com.emonotion.app.domain.model.MoodType.NEUTRAL to "Нейтрально",
            com.emonotion.app.domain.model.MoodType.BAD to "Плохо",
            com.emonotion.app.domain.model.MoodType.TERRIBLE to "Ужасно"
        )
        
        val moodColors = mapOf(
            com.emonotion.app.domain.model.MoodType.GREAT to android.graphics.Color.parseColor("#4CAF50"),
            com.emonotion.app.domain.model.MoodType.GOOD to android.graphics.Color.parseColor("#8BC34A"),
            com.emonotion.app.domain.model.MoodType.NEUTRAL to android.graphics.Color.parseColor("#FFC107"),
            com.emonotion.app.domain.model.MoodType.BAD to android.graphics.Color.parseColor("#FF9800"),
            com.emonotion.app.domain.model.MoodType.TERRIBLE to android.graphics.Color.parseColor("#F44336")
        )
        
        val total = moodDistribution.values.sum()
        if (total == 0) {
            moodChartContainer?.addView(createTextView("Нет данных"))
            return
        }
        
        for ((mood, count) in moodDistribution) {
            val percentage = (count.toFloat() / total * 100).toInt()
            val moodName = moodNames[mood] ?: mood.name
            val color = moodColors[mood] ?: android.graphics.Color.GRAY
            
            val row = android.widget.LinearLayout(requireContext()).apply {
                orientation = android.widget.LinearLayout.HORIZONTAL
                setPadding(0, 8, 0, 8)
            }
            
            val nameText = android.widget.TextView(requireContext()).apply {
                text = moodName
                textSize = 14f
                setTextColor(android.graphics.Color.parseColor("#666666"))
                layoutParams = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            
            val barContainer = android.widget.LinearLayout(requireContext()).apply {
                orientation = android.widget.LinearLayout.HORIZONTAL
                layoutParams = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 2f)
                gravity = android.view.Gravity.CENTER_VERTICAL
            }
            
            val bar = android.view.View(requireContext()).apply {
                setBackgroundColor(color)
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    percentage * 2,
                    24
                )
            }
            
            val countText = android.widget.TextView(requireContext()).apply {
                text = "$count ($percentage%)"
                textSize = 14f
                setTextColor(android.graphics.Color.parseColor("#666666"))
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(16, 0, 0, 0)
            }
            
            row.addView(nameText)
            barContainer.addView(bar)
            barContainer.addView(countText)
            row.addView(barContainer)
            moodChartContainer?.addView(row)
        }
    }
    
    private fun updateEmotionChart(moodDistribution: Map<com.emonotion.app.domain.model.MoodType, Int>) {
        val emotionChartContainer = binding.root.findViewById<android.widget.LinearLayout>(R.id.emotion_chart_container)
        emotionChartContainer?.removeAllViews()
        
        if (moodDistribution.isEmpty()) {
            emotionChartContainer?.addView(createTextView("Нет данных"))
            return
        }
        
        val total = moodDistribution.values.sum()
        if (total == 0) {
            emotionChartContainer?.addView(createTextView("Нет данных"))
            return
        }
        
        val mostCommon = moodDistribution.maxByOrNull { it.value }
        val text = "Самое частое настроение: ${mostCommon?.key?.name ?: "Нет данных"} (${mostCommon?.value ?: 0} записей)"
        emotionChartContainer?.addView(createTextView(text))
    }
    
    private fun updateActivityChart(activities: List<String>) {
        val activityChartContainer = binding.root.findViewById<android.widget.LinearLayout>(R.id.activity_chart_container)
        activityChartContainer?.removeAllViews()
        
        if (activities.isEmpty()) {
            activityChartContainer?.addView(createTextView("Нет данных"))
            return
        }
        
        activityChartContainer?.addView(createTextView("Топ активностей:"))
        
        for (activity in activities.take(5)) {
            activityChartContainer?.addView(createTextView("- $activity"))
        }
    }
    
    private fun createTextView(text: String): android.widget.TextView {
        return android.widget.TextView(requireContext()).apply {
            this.text = text
            textSize = 14f
            setTextColor(android.graphics.Color.parseColor("#666666"))
            setPadding(0, 4, 0, 4)
        }
    }
    
    private fun updateLoadingState(isLoading: Boolean) {
        binding.apply {
            // Показываем/скрываем контент в зависимости от состояния загрузки
            val contentContainer = binding.root.findViewById<android.widget.LinearLayout>(R.id.content_container)
            if (contentContainer != null) {
                contentContainer.alpha = if (isLoading) 0.5f else 1.0f
                contentContainer.isEnabled = !isLoading
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
