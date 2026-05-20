package com.emonotion.app.data.repository

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.emonotion.app.domain.model.Analytics
import com.emonotion.app.domain.model.AnalyticsPeriod
import com.emonotion.app.domain.model.MoodTrendPoint
import com.emonotion.app.domain.model.MoodType
import com.emonotion.app.domain.model.NamedCount
import com.emonotion.app.domain.model.TrendDirection
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Формирование PDF-отчёта аналитики в стиле экрана приложения.
 */
internal object AnalyticsPdfExporter {

    private const val PAGE_W = 595
    private const val PAGE_H = 842
    private const val MARGIN = 40f
    private const val CONTENT_W = PAGE_W - MARGIN * 2

    private val COLOR_PRIMARY = 0xFFFF6B6B.toInt()
    private val COLOR_SECONDARY = 0xFF00D2FF.toInt()
    private val COLOR_CARD = 0xFFFFFFFF.toInt()
    private val COLOR_TEXT = 0xFF2D2D2D.toInt()
    private val COLOR_TEXT_MUTED = 0xFF757575.toInt()
    private val COLOR_BORDER = 0xFFE8E0D5.toInt()
    private val COLOR_GRID = 0xFFE0E0E0.toInt()

    private val moodColors = mapOf(
        MoodType.GREAT to 0xFF4CAF50.toInt(),
        MoodType.GOOD to 0xFF8BC34A.toInt(),
        MoodType.NEUTRAL to 0xFFFFC107.toInt(),
        MoodType.BAD to 0xFFFF9800.toInt(),
        MoodType.TERRIBLE to 0xFFF44336.toInt()
    )

    fun write(analytics: Analytics, out: OutputStream) {
        val doc = PdfDocument()

        val page1Info = PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, 1).create()
        val page1 = doc.startPage(page1Info)
        var y = drawPageHeader(page1.canvas, analytics)

        y = drawSectionTitle(page1.canvas, "Тренд настроения", y)
        y = drawTrendBlock(page1.canvas, analytics.improvementTrend, y) + 12f

        y = drawSectionTitle(page1.canvas, "Общая статистика", y)
        y = drawStatsBlock(page1.canvas, analytics, y) + 16f

        y = drawSectionTitle(page1.canvas, "Динамика настроения", y)
        if (analytics.moodTrendByDate.isNotEmpty()) {
            y = drawChartFrame(page1.canvas, y, 170f) { c, top, h ->
                drawLineChart(c, analytics.moodTrendByDate, MARGIN + 36f, top, CONTENT_W - 36f, h - 16f)
            }
        } else {
            y = drawEmptyChart(page1.canvas, y, 60f)
        }
        drawPageFooter(page1.canvas, 1)
        doc.finishPage(page1)

        val page2Info = PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, 2).create()
        val page2 = doc.startPage(page2Info)
        y = MARGIN + 8f

        y = drawSectionTitle(page2.canvas, "Распределение настроения", y)
        val moodFiltered = analytics.moodDistribution.filter { it.value > 0 }
        if (moodFiltered.isNotEmpty()) {
            y = drawChartFrame(page2.canvas, y, 200f) { c, top, h ->
                drawPieWithLegend(c, moodFiltered, MARGIN + 12f, top, h)
            }
        } else {
            y = drawEmptyChart(page2.canvas, y, 60f)
        }
        y += 12f

        y = drawSectionTitle(page2.canvas, "Активности", y)
        val activities = analytics.activityCounts
        if (activities.isNotEmpty()) {
            val chartH = (activities.size.coerceAtMost(8) * 22 + 48).toFloat().coerceIn(120f, 220f)
            y = drawChartFrame(page2.canvas, y, chartH) { c, top, h ->
                drawBarChart(c, activities.take(12), MARGIN + 8f, top, CONTENT_W - 16f, h - 12f)
            }
        } else {
            drawEmptyChart(page2.canvas, y, 60f)
        }
        drawPageFooter(page2.canvas, 2)
        doc.finishPage(page2)

        doc.writeTo(out)
        doc.close()
    }

    private fun drawPageHeader(canvas: Canvas, analytics: Analytics): Float {
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_PRIMARY
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, PAGE_W.toFloat(), 72f, headerPaint)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt()
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("EmoNotion", MARGIN, 32f, titlePaint)
        titlePaint.textSize = 13f
        titlePaint.typeface = Typeface.DEFAULT
        canvas.drawText("Отчёт аналитики", MARGIN, 52f, titlePaint)

        val metaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt()
            textSize = 11f
            textAlign = Paint.Align.RIGHT
        }
        val generated = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText("Период: ${periodLabelRu(analytics.period)}", PAGE_W - MARGIN, 36f, metaPaint)
        canvas.drawText(generated, PAGE_W - MARGIN, 54f, metaPaint)

        return 88f
    }

    private fun drawSectionTitle(canvas: Canvas, title: String, y: Float): Float {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_TEXT
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(title, MARGIN, y + 14f, paint)
        return y + 24f
    }

    private fun drawTrendBlock(canvas: Canvas, trend: TrendDirection, top: Float): Float {
        val cardRect = RectF(MARGIN, top, MARGIN + CONTENT_W, top + 52f)
        drawCard(canvas, cardRect)

        val accent = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_PRIMARY
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(MARGIN + 8f, top + 8f, MARGIN + 12f, top + 44f, 2f, 2f, accent)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_TEXT
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val descPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_TEXT_MUTED
            textSize = 11f
        }
        canvas.drawText(trendTitleRu(trend), MARGIN + 20f, top + 24f, titlePaint)
        canvas.drawText(trendDescRu(trend), MARGIN + 20f, top + 42f, descPaint)
        return top + 52f
    }

    private fun drawStatsBlock(canvas: Canvas, analytics: Analytics, top: Float): Float {
        val rows = listOf(
            "Всего записей" to analytics.totalEntries.toString(),
            "Среднее настроение" to "${"%.1f".format(Locale.US, analytics.averageMood)}/5",
            "Текущая серия" to "${analytics.currentStreak} дн.",
            "Преобладающее настроение" to dominantMoodLabel(analytics),
            "Процент хороших дней" to "${analytics.goodDaysPercentage.roundToInt()}%"
        )
        val rowH = 26f
        val cardH = rows.size * rowH + 16f
        val cardRect = RectF(MARGIN, top, MARGIN + CONTENT_W, top + cardH)
        drawCard(canvas, cardRect)

        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_TEXT_MUTED
            textSize = 11f
        }
        val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_TEXT
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        var rowY = top + 22f
        rows.forEach { (label, value) ->
            canvas.drawText(label, MARGIN + 12f, rowY, labelPaint)
            canvas.drawText(value, MARGIN + CONTENT_W - 12f, rowY, valuePaint)
            rowY += rowH
        }
        return top + cardH
    }

    private fun drawChartFrame(
        canvas: Canvas,
        top: Float,
        height: Float,
        drawContent: (Canvas, Float, Float) -> Unit
    ): Float {
        val rect = RectF(MARGIN, top, MARGIN + CONTENT_W, top + height)
        drawCard(canvas, rect)
        drawContent(canvas, top + 8f, height)
        return top + height + 8f
    }

    private fun drawEmptyChart(canvas: Canvas, top: Float, height: Float): Float {
        val rect = RectF(MARGIN, top, MARGIN + CONTENT_W, top + height)
        drawCard(canvas, rect)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_TEXT_MUTED
            textSize = 12f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Нет данных за период", MARGIN + CONTENT_W / 2f, top + height / 2f, paint)
        return top + height + 8f
    }

    private fun drawCard(canvas: Canvas, rect: RectF) {
        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_CARD
            style = Paint.Style.FILL
        }
        val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_BORDER
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        canvas.drawRoundRect(rect, 8f, 8f, fill)
        canvas.drawRoundRect(rect, 8f, 8f, border)
    }

    private fun drawPageFooter(canvas: Canvas, page: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_TEXT_MUTED
            textSize = 9f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("EmoNotion · стр. $page", PAGE_W / 2f, PAGE_H - 24f, paint)
    }

    private fun drawLineChart(
        canvas: Canvas,
        data: List<MoodTrendPoint>,
        left: Float,
        top: Float,
        width: Float,
        height: Float
    ) {
        if (data.isEmpty()) return

        val axisMin = 0.75f
        val axisMax = 5.25f
        fun scoreToY(score: Float): Float {
            val ratio = (score - axisMin) / (axisMax - axisMin)
            return top + height - ratio.coerceIn(0f, 1f) * height
        }

        val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_GRID
            strokeWidth = 0.5f
            style = Paint.Style.STROKE
        }
        val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_TEXT_MUTED
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_TEXT_MUTED
            textSize = 9f
            textAlign = Paint.Align.RIGHT
        }

        val plotLeft = left
        val plotBottom = top + height
        canvas.drawLine(plotLeft, top, plotLeft, plotBottom, axisPaint)
        canvas.drawLine(plotLeft, plotBottom, plotLeft + width, plotBottom, axisPaint)

        for (i in 1..5) {
            val y = scoreToY(i.toFloat())
            canvas.drawLine(plotLeft, y, plotLeft + width, y, gridPaint)
            canvas.drawText(i.toString(), plotLeft - 6f, y + 3f, labelPaint)
        }

        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_PRIMARY
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }
        val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_PRIMARY
            style = Paint.Style.FILL
        }
        val stepX = if (data.size > 1) width / (data.size - 1) else 0f

        if (data.size > 1) {
            for (i in 0 until data.size - 1) {
                canvas.drawLine(
                    plotLeft + i * stepX,
                    scoreToY(data[i].averageScore),
                    plotLeft + (i + 1) * stepX,
                    scoreToY(data[i + 1].averageScore),
                    linePaint
                )
            }
        }
        data.forEachIndexed { i, point ->
            val x = if (data.size > 1) plotLeft + i * stepX else plotLeft + width / 2f
            canvas.drawCircle(x, scoreToY(point.averageScore), 3.5f, dotPaint)
        }
    }

    private fun drawPieWithLegend(
        canvas: Canvas,
        data: Map<MoodType, Int>,
        left: Float,
        top: Float,
        frameHeight: Float
    ) {
        val total = data.values.sum().coerceAtLeast(1)
        val pieSize = (frameHeight - 24f).coerceAtMost(130f)
        val pieTop = top + (frameHeight - pieSize) / 2f
        val rect = RectF(left + 8f, pieTop, left + 8f + pieSize, pieTop + pieSize)

        var startAngle = -90f
        data.entries.sortedByDescending { it.value }.forEach { (mood, count) ->
            val sweep = count.toFloat() / total * 360f
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = moodColors[mood] ?: COLOR_TEXT_MUTED
                style = Paint.Style.FILL
            }
            canvas.drawArc(rect, startAngle, sweep, true, paint)
            startAngle += sweep
        }

        val legendLeft = left + pieSize + 28f
        var legendY = top + 20f
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_TEXT
            textSize = 10f
        }
        val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

        data.entries.sortedByDescending { it.value }.forEach { (mood, count) ->
            dotPaint.color = moodColors[mood] ?: COLOR_TEXT_MUTED
            canvas.drawCircle(legendLeft, legendY - 3f, 4f, dotPaint)
            val percent = count * 100 / total
            canvas.drawText(
                "${moodLabelRu(mood)}: $count ($percent%)",
                legendLeft + 10f,
                legendY,
                labelPaint
            )
            legendY += 16f
        }
    }

    private fun drawBarChart(
        canvas: Canvas,
        data: List<NamedCount>,
        left: Float,
        top: Float,
        width: Float,
        height: Float
    ) {
        if (data.isEmpty()) return

        val maxCount = data.maxOf { it.count }.coerceAtLeast(1)
        val axisWidth = 26f
        val plotLeft = left + axisWidth
        val plotWidth = width - axisWidth
        val plotBottom = top + height - 20f
        val plotHeight = plotBottom - top

        val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_GRID
            strokeWidth = 0.5f
            style = Paint.Style.STROKE
        }
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_TEXT_MUTED
            textSize = 9f
            textAlign = Paint.Align.RIGHT
        }
        val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_SECONDARY
            style = Paint.Style.FILL
        }
        val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_TEXT
            textSize = 8f
            textAlign = Paint.Align.CENTER
        }
        val xLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_TEXT_MUTED
            textSize = 8f
            textAlign = Paint.Align.CENTER
        }

        for (i in 0..maxCount) {
            val y = plotBottom - (i.toFloat() / maxCount) * plotHeight
            canvas.drawLine(plotLeft, y, plotLeft + plotWidth, y, gridPaint)
            canvas.drawText(i.toString(), plotLeft - 4f, y + 3f, labelPaint)
        }

        val slotWidth = plotWidth / data.size
        data.forEachIndexed { index, item ->
            val barH = (item.count.toFloat() / maxCount) * plotHeight
            val barW = slotWidth * 0.65f
            val x = plotLeft + index * slotWidth + (slotWidth - barW) / 2f
            val y = plotBottom - barH
            canvas.drawRect(x, y, x + barW, plotBottom, barPaint)
            canvas.drawText(item.count.toString(), x + barW / 2f, y - 4f, valuePaint)
            val label = if (item.label.length > 12) item.label.take(11) + "…" else item.label
            canvas.drawText(label, x + barW / 2f, plotBottom + 12f, xLabelPaint)
        }
    }

    private fun dominantMoodLabel(analytics: Analytics): String {
        val top = analytics.moodDistribution
            .filter { it.value > 0 }
            .maxByOrNull { it.value }
            ?.key
        return top?.let { moodLabelRu(it) } ?: "—"
    }

    private fun moodLabelRu(mood: MoodType): String = when (mood) {
        MoodType.GREAT -> "Отлично"
        MoodType.GOOD -> "Хорошо"
        MoodType.NEUTRAL -> "Нормально"
        MoodType.BAD -> "Плохо"
        MoodType.TERRIBLE -> "Ужасно"
        else -> mood.displayName
    }

    private fun periodLabelRu(period: AnalyticsPeriod): String = when (period) {
        AnalyticsPeriod.WEEK -> "7 дней"
        AnalyticsPeriod.TWO_WEEKS -> "14 дней"
        AnalyticsPeriod.MONTH -> "30 дней"
        AnalyticsPeriod.ALL_TIME -> "Всё время"
    }

    private fun trendTitleRu(trend: TrendDirection): String = when (trend) {
        TrendDirection.IMPROVING -> "Улучшение"
        TrendDirection.STABLE -> "Стабильно"
        TrendDirection.DECLINING -> "Снижение"
        TrendDirection.VOLATILE -> "Изменчиво"
        TrendDirection.STABLE_POSITIVE -> "Стабильно (высокий)"
        TrendDirection.STABLE_NEGATIVE -> "Стабильно (низкий)"
        TrendDirection.RECOVERING -> "Восстановление"
        TrendDirection.FLUCTUATING -> "Колебания"
    }

    private fun trendDescRu(trend: TrendDirection): String = when (trend) {
        TrendDirection.IMPROVING -> "Среднее настроение за период растёт"
        TrendDirection.STABLE -> "Настроение держится на одном уровне"
        TrendDirection.DECLINING -> "Среднее настроение за период снижается"
        TrendDirection.VOLATILE -> "Сильные перепады — стабильного тренда нет"
        TrendDirection.STABLE_POSITIVE -> "Настроение стабильно на высоком уровне"
        TrendDirection.STABLE_NEGATIVE -> "Настроение стабильно на низком уровне"
        TrendDirection.RECOVERING -> "Настроение восстанавливается после падения"
        TrendDirection.FLUCTUATING -> "Настроение колеблется без явного тренда"
    }
}
