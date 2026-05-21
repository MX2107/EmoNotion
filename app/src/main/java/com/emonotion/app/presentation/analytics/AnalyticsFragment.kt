package com.emonotion.app.presentation.analytics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.emonotion.app.R
import com.emonotion.app.databinding.FragmentAnalyticsBinding
import com.emonotion.app.domain.model.Analytics
import com.emonotion.app.domain.model.AnalyticsExportFormat
import com.emonotion.app.domain.model.AnalyticsPeriod
import com.emonotion.app.domain.model.MoodLevel
import com.emonotion.app.domain.model.MoodTrendPoint
import com.emonotion.app.domain.model.MoodType
import com.emonotion.app.domain.model.NamedCount
import com.emonotion.app.domain.model.StabilityPeriod
import com.emonotion.app.domain.model.TrendDirection
import com.emonotion.app.domain.model.TrendPeriod
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Экран аналитики и статистики
 */
@AndroidEntryPoint
class AnalyticsFragment : Fragment() {

    private var _binding: FragmentAnalyticsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AnalyticsViewModel by viewModels()

    private var lastTrend: List<MoodTrendPoint> = emptyList()
    private var lastActivityCounts: List<NamedCount> = emptyList()

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
        setupChartListeners()
        setupUI()
        observeViewModel()
        viewModel.loadAnalyticsData()
    }

    private fun setupChartListeners() {
        binding.moodLineChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                val idx = e?.x?.toInt() ?: return
                val point = lastTrend.getOrNull(idx) ?: return
                val moodText = moodScoreToText(e.y)
                Toast.makeText(
                    requireContext(),
                    "${point.date}: $moodText",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onNothingSelected() = Unit
        })

        binding.moodPieChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                val pe = e as? PieEntry ?: return
                val total = lastPieTotal.coerceAtLeast(1f)
                val percent = (pe.value / total * 100).toInt()
                Toast.makeText(
                    requireContext(),
                    getString(R.string.chart_point_pie_detail, pe.label, pe.value.toInt(), percent),
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onNothingSelected() = Unit
        })

        binding.activityBarChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                val be = e as? BarEntry ?: return
                val idx = be.x.toInt()
                val label = lastActivityCounts.getOrNull(idx)?.label ?: return
                Toast.makeText(
                    requireContext(),
                    getString(R.string.chart_point_activity, label, be.y),
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onNothingSelected() = Unit
        })
    }

    private var lastPieTotal: Float = 0f

    private fun setupUI() {
        binding.exportButton.setOnClickListener {
            showExportFormatDialog()
        }

        // Устанавливаем начальное состояние RadioGroup в соответствии с текущим периодом
        val initialPeriod = viewModel.selectedPeriod.value
        val initialCheckedId = when (initialPeriod) {
            AnalyticsPeriod.WEEK -> R.id.radio_7_days
            AnalyticsPeriod.TWO_WEEKS -> R.id.radio_14_days
            AnalyticsPeriod.MONTH -> R.id.radio_30_days
            AnalyticsPeriod.ALL_TIME -> R.id.radio_all_time
        }
        binding.timeRangeGroup.check(initialCheckedId)

        binding.timeRangeGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radio_7_days -> viewModel.setPeriod(AnalyticsPeriod.WEEK)
                R.id.radio_14_days -> viewModel.setPeriod(AnalyticsPeriod.TWO_WEEKS)
                R.id.radio_30_days -> viewModel.setPeriod(AnalyticsPeriod.MONTH)
                R.id.radio_all_time -> viewModel.setPeriod(AnalyticsPeriod.ALL_TIME)
            }
        }
    }

    private fun showExportFormatDialog() {
        val items = arrayOf(
            getString(R.string.export_format_csv),
            getString(R.string.export_format_pdf)
        )
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.export_analytics_title)
            .setItems(items) { _, which ->
                val format = if (which == 0) AnalyticsExportFormat.CSV else AnalyticsExportFormat.PDF
                viewModel.exportAnalytics(format)
            }
            .show()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.analyticsData.collect { analytics ->
                analytics?.let { updateAnalyticsDisplay(it) }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                updateLoadingState(isLoading)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isExporting.collect { exporting ->
                binding.exportProgress.visibility = if (exporting) View.VISIBLE else View.GONE
                binding.exportButton.isEnabled = !exporting
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.errorMessage.collect { error ->
                error?.let {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                    viewModel.clearError()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.exportFinishedUri.collect { uri ->
                if (uri != null) {
                    Toast.makeText(requireContext(), R.string.export_saved, Toast.LENGTH_LONG).show()
                    viewModel.consumeExportUri()
                }
            }
        }
    }

    private fun updateAnalyticsDisplay(analytics: Analytics) {
        binding.totalEntriesText.text = analytics.totalEntries.toString()
        binding.averageMoodText.text = String.format("%.1f/5", analytics.averageMood)
        binding.currentStreakText.text = getString(R.string.streak_days_format, analytics.currentStreak)
        binding.goodDaysPercentageText.text = String.format("%.0f%%", analytics.goodDaysPercentage)

        binding.dominantMoodText.text = resolveDominantMoodLabel(analytics)

        updateTrendCard(analytics.improvementTrend, analytics.stabilityPeriodsList, analytics.trendPeriodsList)

        lastTrend = analytics.moodTrendByDate
        lastActivityCounts = analytics.activityCounts

        lastPieTotal = analytics.moodDistribution.values.sum().toFloat()

        AnalyticsChartsHelper.bindLineChart(binding.moodLineChart, analytics.moodTrendByDate)
        AnalyticsChartsHelper.bindPieChart(binding.moodPieChart, analytics.moodDistribution)

        val activityData = analytics.activityCounts.ifEmpty {
            analytics.mostCommonActivities.map { NamedCount(it, 1) }
        }
        AnalyticsChartsHelper.bindBarChart(binding.activityBarChart, activityData)

        val scrollable = AnalyticsChartsHelper.needsHorizontalScroll(activityData)
        binding.activityScrollHint.isVisible = scrollable
        binding.activityChartFade.isVisible = scrollable
        if (scrollable) {
            binding.activityBarChart.moveViewToX(0f)
        }

        val emotionData = analytics.emotionCounts
        AnalyticsChartsHelper.bindBarChart(binding.emotionBarChart, emotionData)

        val emotionScrollable = AnalyticsChartsHelper.needsHorizontalScroll(emotionData)
        binding.emotionScrollHint.isVisible = emotionScrollable
        binding.emotionChartFade.isVisible = emotionScrollable
        if (emotionScrollable) {
            binding.emotionBarChart.moveViewToX(0f)
        }
    }

    private fun resolveDominantMoodLabel(analytics: Analytics): String {
        val top = analytics.moodDistribution
            .filter { it.value > 0 }
            .maxByOrNull { it.value }
            ?.key
        return top?.let { moodTypeLabel(it) }
            ?: getString(R.string.stats_no_data)
    }

    private fun moodTypeLabel(mood: MoodType): String = when (mood) {
        MoodType.GREAT -> getString(R.string.mood_great)
        MoodType.GOOD -> getString(R.string.mood_good)
        MoodType.NEUTRAL -> getString(R.string.mood_neutral)
        MoodType.BAD -> getString(R.string.mood_bad)
        MoodType.TERRIBLE -> getString(R.string.mood_terrible)
        else -> mood.displayName
    }

    private fun moodLevelLabel(level: MoodLevel): String = when (level) {
        MoodLevel.HIGH -> "высокий"
        MoodLevel.MEDIUM -> "средний"
        MoodLevel.LOW -> "низкий"
    }

    private fun moodScoreToText(score: Float): String = when {
        score >= 4.5f -> "Отлично"
        score >= 3.5f -> "Хорошо"
        score >= 2.5f -> "Нейтрально"
        score >= 1.5f -> "Плохо"
        else -> "Ужасно"
    }

    private fun updateTrendCard(trend: TrendDirection, stabilityPeriods: List<StabilityPeriod>, trendPeriods: List<TrendPeriod>) {
        binding.trendDirectionText.text = when (trend) {
            TrendDirection.IMPROVING -> getString(R.string.trend_improving_short)
            TrendDirection.STABLE -> getString(R.string.trend_stable_short)
            TrendDirection.DECLINING -> getString(R.string.trend_declining_short)
            TrendDirection.VOLATILE -> getString(R.string.trend_volatile_short)
            TrendDirection.STABLE_POSITIVE -> "Стабильно (высокий)"
            TrendDirection.STABLE_NEGATIVE -> "Стабильно (низкий)"
            TrendDirection.RECOVERING -> "Восстановление"
            TrendDirection.FLUCTUATING -> "Колебания"
        }
        
        val baseDescription = when (trend) {
            TrendDirection.IMPROVING -> getString(R.string.trend_desc_improving)
            TrendDirection.STABLE -> getString(R.string.trend_desc_stable)
            TrendDirection.DECLINING -> getString(R.string.trend_desc_declining)
            TrendDirection.VOLATILE -> getString(R.string.trend_desc_volatile)
            TrendDirection.STABLE_POSITIVE -> "Настроение стабильно на высоком уровне"
            TrendDirection.STABLE_NEGATIVE -> "Настроение стабильно на низком уровне"
            TrendDirection.RECOVERING -> "Настроение восстанавливается после падения"
            TrendDirection.FLUCTUATING -> "Настроение колеблется без явного тренда"
        }
        
        // Функция для форматирования даты
        fun formatDate(date: String, sameYear: Boolean): String {
            return if (sameYear) {
                val month = date.substring(5, 7)
                val day = date.substring(8, 10)
                "$day.$month"
            } else {
                val year = date.substring(0, 4)
                val month = date.substring(5, 7)
                val day = date.substring(8, 10)
                "$day.$month.$year"
            }
        }
        
        val periodsInfo = mutableListOf<String>()
        
        // Периоды стабильности
        if (stabilityPeriods.isNotEmpty()) {
            val years = stabilityPeriods.flatMap { listOf(it.startDate.substring(0, 4), it.endDate.substring(0, 4)) }.distinct()
            val sameYear = years.size == 1
            
            val stabilityText = stabilityPeriods.joinToString("\n") { 
                val startDateFormatted = formatDate(it.startDate, sameYear)
                val endDateFormatted = formatDate(it.endDate, sameYear)
                "$startDateFormatted – $endDateFormatted (${it.duration} дн., ${moodLevelLabel(it.level)})" 
            }
            periodsInfo.add("Периоды стабильности:\n$stabilityText")
        }
        
        // Периоды улучшений и ухудшений
        if (trendPeriods.isNotEmpty()) {
            val years = trendPeriods.flatMap { listOf(it.startDate.substring(0, 4), it.endDate.substring(0, 4)) }.distinct()
            val sameYear = years.size == 1
            
            val improvements = trendPeriods.filter { it.direction == TrendDirection.IMPROVING }
            val declines = trendPeriods.filter { it.direction == TrendDirection.DECLINING }
            
            if (improvements.isNotEmpty()) {
                val improvementsText = improvements.joinToString("\n") {
                    val startDateFormatted = formatDate(it.startDate, sameYear)
                    val endDateFormatted = formatDate(it.endDate, sameYear)
                    "$startDateFormatted – $endDateFormatted (${it.duration} дн.)"
                }
                periodsInfo.add("Периоды улучшений:\n$improvementsText")
            }
            
            if (declines.isNotEmpty()) {
                val declinesText = declines.joinToString("\n") {
                    val startDateFormatted = formatDate(it.startDate, sameYear)
                    val endDateFormatted = formatDate(it.endDate, sameYear)
                    "$startDateFormatted – $endDateFormatted (${it.duration} дн.)"
                }
                periodsInfo.add("Периоды ухудшений:\n$declinesText")
            }
        }
        
        val fullDescription = if (periodsInfo.isNotEmpty()) {
            "$baseDescription\n\n${periodsInfo.joinToString("\n\n")}"
        } else {
            baseDescription
        }
        
        binding.trendDescriptionText.text = fullDescription
        binding.trendIcon.setImageResource(
            when (trend) {
                TrendDirection.IMPROVING -> R.drawable.ic_trend_improving
                TrendDirection.STABLE -> R.drawable.ic_trend_stable
                TrendDirection.DECLINING -> R.drawable.ic_trend_declining
                TrendDirection.VOLATILE -> R.drawable.ic_trend_volatile
                TrendDirection.STABLE_POSITIVE -> R.drawable.ic_trend_stable
                TrendDirection.STABLE_NEGATIVE -> R.drawable.ic_trend_stable
                TrendDirection.RECOVERING -> R.drawable.ic_trend_improving
                TrendDirection.FLUCTUATING -> R.drawable.ic_trend_volatile
            }
        )
    }

    private fun updateLoadingState(isLoading: Boolean) {
        binding.contentContainer.alpha = if (isLoading) 0.5f else 1.0f
        binding.contentContainer.isEnabled = !isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
