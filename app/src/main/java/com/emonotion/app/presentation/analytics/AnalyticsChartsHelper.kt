package com.emonotion.app.presentation.analytics

import androidx.core.content.ContextCompat
import com.emonotion.app.R
import com.emonotion.app.databinding.FragmentAnalyticsBinding
import com.emonotion.app.domain.model.Analytics
import com.emonotion.app.domain.model.AnalyticsExportFormat
import com.emonotion.app.domain.model.AnalyticsPeriod
import com.emonotion.app.domain.model.MoodEmoji
import com.emonotion.app.domain.model.MoodLevel
import com.emonotion.app.domain.model.MoodTrendPoint
import com.emonotion.app.domain.model.MoodType
import com.emonotion.app.domain.model.NamedCount
import com.emonotion.app.domain.model.StabilityPeriod
import com.emonotion.app.domain.model.TrendDirection
import com.emonotion.app.domain.model.TrendPeriod
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Настройка MPAndroidChart для экрана аналитики.
 */
object AnalyticsChartsHelper {

    private val shortDate = SimpleDateFormat("dd.MM", Locale.getDefault())
    private val isoDate = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    private val MOOD_ORDER = listOf(
        MoodType.GREAT,
        MoodType.GOOD,
        MoodType.NEUTRAL,
        MoodType.BAD,
        MoodType.TERRIBLE
    )

    private const val VISIBLE_BARS = 4f
    private const val VISIBLE_LINE_POINTS = 50f

    fun bindLineChart(chart: LineChart, trend: List<MoodTrendPoint>) {
        chart.description.isEnabled = false
        chart.setTouchEnabled(true)
        chart.setScaleEnabled(true)
        chart.setPinchZoom(true)
        chart.isDoubleTapToZoomEnabled = true
        chart.isDragEnabled = true
        chart.isDragXEnabled = true
        chart.isDragYEnabled = false
        chart.legend.isEnabled = false
        chart.axisRight.isEnabled = false
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.xAxis.setDrawGridLines(false)
        chart.xAxis.granularity = 1f
        chart.axisLeft.axisMinimum = 0.15f
        chart.axisLeft.axisMaximum = 5.75f
        chart.axisLeft.setDrawGridLines(true)
        chart.axisLeft.granularity = 1f
        chart.axisLeft.setLabelCount(5, false)
        chart.axisLeft.textSize = 20f
        chart.axisLeft.valueFormatter = emojiAxisFormatter()
        chart.setExtraOffsets(8f, 12f, 8f, 8f)

        if (trend.isEmpty()) {
            chart.clear()
            chart.setNoDataText(chart.context.getString(R.string.chart_no_data))
            return
        }

        val entries = trend.mapIndexed { index, p -> Entry(index.toFloat(), p.averageScore) }
        val labels = trend.map { p ->
            runCatching { shortDate.format(isoDate.parse(p.date)!!) }.getOrDefault(p.date)
        }

        val dataSet = LineDataSet(entries, "").apply {
            color = ContextCompat.getColor(chart.context, R.color.primary)
            setDrawCircles(true)
            circleRadius = 4f
            lineWidth = 2f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillColor = ContextCompat.getColor(chart.context, R.color.accent_light)
            setDrawValues(false)
        }

        chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        chart.data = LineData(dataSet)

        // Настраиваем видимый диапазон для scroll
        // Если данных мало (до 30), показываем все во всю ширину
        // Если данных много, ограничиваем до 50 точек с прокруткой
        if (trend.size <= 30) {
            // Не ограничиваем - график займет всю ширину
        } else {
            chart.setVisibleXRangeMaximum(VISIBLE_LINE_POINTS)
            chart.setVisibleXRangeMinimum(VISIBLE_LINE_POINTS)
        }

        chart.invalidate()
        chart.moveViewToX(0f) // Выравниваем по левому краю
    }

    fun bindPieChart(chart: PieChart, distribution: Map<MoodType, Int>) {
        chart.description.isEnabled = false
        chart.setUsePercentValues(false)
        chart.setDrawEntryLabels(false)
        chart.setHoleRadius(42f)
        chart.setTransparentCircleRadius(46f)
        chart.setExtraOffsets(8f, 8f, 8f, 8f)

        val filtered = MOOD_ORDER.mapNotNull { mood ->
            val count = distribution[mood] ?: 0
            if (count > 0) mood to count else null
        }
        if (filtered.isEmpty()) {
            chart.clear()
            chart.legend.isEnabled = false
            chart.setNoDataText(chart.context.getString(R.string.chart_no_data))
            return
        }

        val total = filtered.sumOf { it.second }.toFloat()
        val entries = filtered.map { (mood, count) ->
            PieEntry(count.toFloat(), labelRu(chart, mood))
        }
        val colors = ArrayList(filtered.map { (mood, _) -> colorForMood(chart, mood) })

        val set = PieDataSet(entries, "").apply {
            this.colors = colors
            valueTextSize = 12f
            valueTextColor = android.graphics.Color.BLACK
            valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val count = value.toInt()
                    val percent = if (total > 0) (value / total * 100).roundToInt() else 0
                    return "$count ($percent%)"
                }
            }
            sliceSpace = 2f
            selectionShift = 4f
        }

        chart.legend.isEnabled = true
        chart.legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        chart.legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        chart.legend.orientation = Legend.LegendOrientation.HORIZONTAL
        chart.legend.textSize = 11f
        chart.legend.form = Legend.LegendForm.CIRCLE
        chart.legend.xEntrySpace = 12f

        chart.data = PieData(set)
        chart.invalidate()
    }

    fun bindBarChart(chart: BarChart, counts: List<NamedCount>) {
        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.axisRight.isEnabled = false
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.xAxis.setDrawGridLines(false)
        chart.xAxis.granularity = 1f
        chart.setFitBars(false)
        chart.setScaleEnabled(true)
        chart.setScaleXEnabled(true)
        chart.setScaleYEnabled(false)
        chart.setPinchZoom(false)
        chart.isDoubleTapToZoomEnabled = false
        chart.isDragEnabled = true
        chart.isDragXEnabled = true
        chart.isDragYEnabled = false

        if (counts.isEmpty()) {
            chart.clear()
            chart.setNoDataText(chart.context.getString(R.string.chart_no_data))
            return
        }

        val maxCount = counts.maxOf { it.count }.coerceAtLeast(1)
        chart.axisLeft.axisMinimum = 0f
        chart.axisLeft.axisMaximum = (maxCount * 1.1f).coerceAtLeast(5f)
        chart.axisLeft.granularity = if (maxCount > 10) 2f else 1f
        chart.axisLeft.setLabelCount(3, false)
        chart.axisLeft.valueFormatter = integerAxisFormatter(0, maxCount)

        val entries = counts.mapIndexed { index, nc -> BarEntry(index.toFloat(), nc.count.toFloat()) }
        val labels = counts.map { it.label }
        val set = BarDataSet(entries, "").apply {
            color = ContextCompat.getColor(chart.context, R.color.secondary)
            setDrawValues(true)
            valueTextSize = 10f
            valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                override fun getFormattedValue(value: Float): String = value.toInt().toString()
            }
        }
        chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        chart.xAxis.labelRotationAngle = if (counts.size > 4) -35f else 0f

        chart.setExtraOffsets(16f, 8f, if (counts.size > VISIBLE_BARS.toInt()) 16f else 8f, if (counts.size > 4) 20f else 8f)
        chart.data = BarData(set).apply { barWidth = 0.55f }
        
        // Настраиваем видимый диапазон для scroll
        if (counts.size > VISIBLE_BARS.toInt()) {
            chart.setVisibleXRangeMaximum(VISIBLE_BARS)
            chart.setVisibleXRangeMinimum(VISIBLE_BARS)
        }
        
        chart.invalidate()
        chart.moveViewToX(0f)
    }

    fun needsHorizontalScroll(counts: List<NamedCount>): Boolean =
        counts.size > VISIBLE_BARS.toInt()

    private fun integerAxisFormatter(min: Int, max: Int) =
        object : com.github.mikephil.charting.formatter.ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val intVal = value.roundToInt()
                return if (intVal in min..max) intVal.toString() else ""
            }
        }

    private fun emojiAxisFormatter() =
        object : com.github.mikephil.charting.formatter.ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val intVal = value.roundToInt()
                return when (intVal) {
                    1 -> MoodEmoji.TERRIBLE
                    2 -> MoodEmoji.BAD
                    3 -> MoodEmoji.NEUTRAL
                    4 -> MoodEmoji.GOOD
                    5 -> MoodEmoji.GREAT
                    else -> ""
                }
            }
        }

    private fun labelRu(chart: PieChart, mood: MoodType): String = when (mood) {
        MoodType.GREAT -> chart.context.getString(R.string.mood_great)
        MoodType.GOOD -> chart.context.getString(R.string.mood_good)
        MoodType.NEUTRAL -> chart.context.getString(R.string.mood_neutral)
        MoodType.BAD -> chart.context.getString(R.string.mood_bad)
        MoodType.TERRIBLE -> chart.context.getString(R.string.mood_terrible)
        else -> mood.displayName
    }

    private fun colorForMood(chart: PieChart, mood: MoodType): Int = when (mood) {
        MoodType.GREAT -> ContextCompat.getColor(chart.context, R.color.mood_great)
        MoodType.GOOD -> ContextCompat.getColor(chart.context, R.color.mood_good)
        MoodType.NEUTRAL -> ContextCompat.getColor(chart.context, R.color.mood_neutral)
        MoodType.BAD -> ContextCompat.getColor(chart.context, R.color.mood_bad)
        MoodType.TERRIBLE -> ContextCompat.getColor(chart.context, R.color.mood_terrible)
        else -> ContextCompat.getColor(chart.context, R.color.foreground)
    }
}
