package com.emonotion.app.data.repository

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.emonotion.app.data.local.dao.MoodDao
import com.emonotion.app.data.local.entities.MoodEntryEntity
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
import com.emonotion.app.domain.model.TrendStrength
import com.emonotion.app.domain.model.UserStats
import com.emonotion.app.domain.repository.AnalyticsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Реализация репозитория аналитики: агрегаты, тренды и экспорт отчётов.
 */
@Singleton
class AnalyticsRepositoryImpl @Inject constructor(
    private val moodDao: MoodDao,
    @ApplicationContext private val appContext: Context
) : AnalyticsRepository {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    private val weekdayNamesRu = listOf(
        "",
        "Воскресенье",
        "Понедельник",
        "Вторник",
        "Среда",
        "Четвер",
        "Пятница",
        "Суббота"
    )

    override suspend fun getAnalytics(period: AnalyticsPeriod): Analytics {
        val allMoods = moodDao.getAllMoodsList()
        val filtered = filterMoodsByPeriod(allMoods, period)
        return buildAnalytics(period, filtered)
    }

    override fun getAnalyticsFlow(): Flow<Analytics> {
        return moodDao.getAllMoods().map { moods ->
            val filtered = filterMoodsByPeriod(moods, AnalyticsPeriod.WEEK)
            buildAnalytics(AnalyticsPeriod.WEEK, filtered)
        }
    }

    override fun getUserStats(): Flow<UserStats> {
        return moodDao.getAllMoods().map { moods ->
            calculateUserStats(moods)
        }
    }

    override suspend fun exportAnalytics(
        period: AnalyticsPeriod,
        format: AnalyticsExportFormat
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val analytics = getAnalytics(period)
            val moods = filterMoodsByPeriod(moodDao.getAllMoodsList(), period)
            val fileName = when (format) {
                AnalyticsExportFormat.CSV -> "emonotion_analytics_${period.name.lowercase(Locale.US)}_${System.currentTimeMillis()}.csv"
                AnalyticsExportFormat.PDF -> "emonotion_analytics_${period.name.lowercase(Locale.US)}_${System.currentTimeMillis()}.pdf"
            }
            val mime = when (format) {
                AnalyticsExportFormat.CSV -> "text/csv"
                AnalyticsExportFormat.PDF -> "application/pdf"
            }
            val content = when (format) {
                AnalyticsExportFormat.CSV -> buildCsvReport(analytics, moods)
                AnalyticsExportFormat.PDF -> null
            }
            if (format == AnalyticsExportFormat.PDF) {
                // PDF export not implemented yet
                return@withContext Result.failure(IllegalStateException("PDF export not implemented"))
            }
            val uri = createDownloadsFile(fileName, mime) ?: return@withContext Result.failure(
                IllegalStateException("Не удалось создать файл в каталоге загрузок")
            )
            appContext.contentResolver.openOutputStream(uri, "w")?.use { stream ->
                OutputStreamWriter(stream, Charsets.UTF_8).use { it.write(content!!) }
            } ?: return@withContext Result.failure(IllegalStateException("Не удалось открыть поток записи"))
            Result.success(uri)
        } catch (e: Exception) {
            android.util.Log.e("AnalyticsRepository", "exportAnalytics", e)
            Result.failure(e)
        }
    }

    private fun createDownloadsFile(fileName: String, mimeType: String): Uri? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return null
        }
        val resolver = appContext.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_DOWNLOADS + "/EmoNotion/Analytics"
            )
        }
        return resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
    }

    private fun buildCsvReport(analytics: Analytics, moods: List<MoodEntryEntity>): String {
        val sb = StringBuilder()
        sb.appendLine("EmoNotion analytics export")
        sb.appendLine("period;${periodLabelRu(analytics.period)}")
        sb.appendLine("total_entries;${analytics.totalEntries}")
        sb.appendLine("average_mood;${analytics.averageMood}")
        sb.appendLine("current_streak;${analytics.currentStreak}")
        sb.appendLine("longest_streak;${analytics.longestStreak}")
        sb.appendLine("trend;${trendTitleRu(analytics.improvementTrend)}")
        sb.appendLine("trend_description;${trendDescRu(analytics.improvementTrend)}")
        sb.appendLine("dominant_mood;${dominantMoodLabel(analytics)}")
        sb.appendLine("good_days_percentage;${analytics.goodDaysPercentage}")
        sb.appendLine()
        sb.appendLine("mood_trend_date;average_score")
        analytics.moodTrendByDate.forEach { p ->
            sb.appendLine("${p.date};${p.averageScore}")
        }
        sb.appendLine()
        sb.appendLine("mood;count")
        analytics.moodDistribution.forEach { (k, v) ->
            sb.appendLine("${k.name};$v")
        }
        sb.appendLine()
        sb.appendLine("activity;count")
        analytics.activityCounts.forEach { sb.appendLine("${it.label};${it.count}") }
        sb.appendLine()
        sb.appendLine("raw_id;date;mood;intensity")
        moods.forEach { m ->
            sb.appendLine("${m.id};${m.date};${m.mood};${m.intensity}")
        }
        return sb.toString()
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

    private fun buildAnalytics(period: AnalyticsPeriod, moods: List<MoodEntryEntity>): Analytics {
        android.util.Log.d("AnalyticsRepository", "buildAnalytics: period=$period, moods.size=${moods.size}")
        val distribution = calculateMoodDistribution(moods)
        val activityCounts = computeActivityCounts(moods)
        android.util.Log.d("AnalyticsRepository", "buildAnalytics: distribution=$distribution, activityCounts=$activityCounts")
        
        val stabilityPeriodsList = calculateStabilityPeriods(moods)
        val trendPeriodsList = calculateTrendPeriods(moods)
        
        return Analytics(
            period = period,
            moodDistribution = distribution,
            averageIntensity = calculateAverageIntensity(moods),
            averageMood = calculateAverageMoodScore(moods),
            totalEntries = moods.size,
            currentStreak = calculateCurrentStreak(moods),
            longestStreak = calculateLongestStreak(moods),
            mostCommonActivities = getMostCommonActivities(moods),
            improvementTrend = calculateTrend(moods),
            moodTrendByDate = computeMoodTrendByDate(moods, period),
            activityCounts = activityCounts,
            goodDaysPercentage = calculateGoodDaysPercentage(moods),
            // Комплексный анализ тренда
            trendStrength = calculateTrendStrength(moods),
            stabilityPeriods = stabilityPeriodsList.size,
            stabilityPeriodsList = stabilityPeriodsList,
            trendPeriodsList = trendPeriodsList,
            volatilityIndex = calculateVolatilityIndex(moods),
            moodLevel = calculateMoodLevel(moods)
        )
    }

    private fun computeMoodTrendByDate(
        moods: List<MoodEntryEntity>,
        period: AnalyticsPeriod
    ): List<MoodTrendPoint> {
        if (moods.isEmpty()) return emptyList()
        val end = Calendar.getInstance()
        val start = Calendar.getInstance()
        val days = periodDays(period, moods)
        if (days == null) {
            val dates = moods.map { it.date }.sorted()
            if (dates.isEmpty()) return emptyList()
            start.time = dateFormat.parse(dates.first())!!
        } else {
            start.add(Calendar.DAY_OF_YEAR, -days)
        }
        val byDate = moods.groupBy { it.date }
        val result = mutableListOf<MoodTrendPoint>()
        val cursor = start.clone() as Calendar
        while (!cursor.after(end)) {
            val key = dateFormat.format(cursor.time)
            val list = byDate[key].orEmpty()
            if (list.isNotEmpty()) {
                val avg = list.map { moodScore(it) }.average().toFloat()
                result.add(MoodTrendPoint(key, avg))
            }
            cursor.add(Calendar.DAY_OF_YEAR, 1)
        }
        return result
    }

    private fun computeActivityCounts(moods: List<MoodEntryEntity>): List<NamedCount> {
        val all = moods.flatMap { m ->
            if (m.activities.isBlank()) emptyList()
            else m.activities.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        }
        if (all.isEmpty()) return emptyList()
        return all.groupingBy { it }.eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(12)
            .map { NamedCount(it.key, it.value) }
    }

    private fun moodScore(entity: MoodEntryEntity): Float {
        return when (parseMoodType(entity.mood)) {
            MoodType.GREAT -> 5f
            MoodType.GOOD -> 4f
            MoodType.NEUTRAL -> 3f
            MoodType.BAD -> 2f
            MoodType.TERRIBLE -> 1f
            else -> 3f
        }
    }

    private fun calculateUserStats(moods: List<MoodEntryEntity>): UserStats {
        val totalEntries = moods.size
        val uniqueDays = moods.map { it.date }.toSet().size
        val today = dateFormat.format(Calendar.getInstance().time)
        val hasEntryToday = moods.any { it.date == today }
        val currentStreak = calculateCurrentStreak(moods)
        val longestStreak = calculateLongestStreak(moods)
        val averageMood = calculateAverageMoodScore(moods)
        val mostFrequentMood = getMostFrequentMood(moods)

        return UserStats(
            totalEntries = totalEntries,
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            averageMood = averageMood,
            mostFrequentMood = mostFrequentMood,
            totalDaysTracked = uniqueDays,
            hasEntryToday = hasEntryToday
        )
    }

    private fun calculateCurrentStreak(moods: List<MoodEntryEntity>): Int {
        if (moods.isEmpty()) {
            android.util.Log.d("AnalyticsRepository", "calculateCurrentStreak: moods is empty")
            return 0
        }

        val calendar = Calendar.getInstance()
        val today = dateFormat.format(calendar.time)

        val validMoods = moods.filter { it.date <= today }
        val sortedDates = validMoods.map { it.date }.distinct().sortedDescending()

        android.util.Log.d("AnalyticsRepository", "calculateCurrentStreak: today=$today, sortedDates=$sortedDates")

        if (sortedDates.isEmpty()) return 0

        val checkCalendar = Calendar.getInstance()

        if (sortedDates[0] != today) {
            checkCalendar.add(Calendar.DAY_OF_YEAR, -1)
            val yesterday = dateFormat.format(checkCalendar.time)

            if (sortedDates[0] != yesterday) {
                android.util.Log.d("AnalyticsRepository", "calculateCurrentStreak: no entry today or yesterday, streak=0")
                return 0
            }
        }

        var streak = 0
        var currentDate = Calendar.getInstance()

        if (sortedDates[0] != today) {
            currentDate.add(Calendar.DAY_OF_YEAR, -1)
        }

        for (dateStr in sortedDates) {
            val dateStrFormatted = dateFormat.format(currentDate.time)
            if (dateStr == dateStrFormatted) {
                streak++
                currentDate.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }

        android.util.Log.d("AnalyticsRepository", "calculateCurrentStreak: result=$streak")
        return streak
    }

    private fun calculateLongestStreak(moods: List<MoodEntryEntity>): Int {
        if (moods.isEmpty()) {
            android.util.Log.d("AnalyticsRepository", "calculateLongestStreak: moods is empty")
            return 0
        }

        val calendar = Calendar.getInstance()
        val today = dateFormat.format(calendar.time)
        val validMoods = moods.filter { it.date <= today }

        val sortedDates = validMoods.map { it.date }.distinct().sorted()
        android.util.Log.d("AnalyticsRepository", "calculateLongestStreak: sortedDates=$sortedDates")

        if (sortedDates.size == 1) {
            android.util.Log.d("AnalyticsRepository", "calculateLongestStreak: only one date, streak=1")
            return 1
        }

        var longestStreak = 1
        var currentStreak = 1

        for (i in 1 until sortedDates.size) {
            val prevDate = Calendar.getInstance().apply { time = dateFormat.parse(sortedDates[i - 1])!! }
            val currDate = Calendar.getInstance().apply { time = dateFormat.parse(sortedDates[i])!! }

            val diffDays = ((currDate.timeInMillis - prevDate.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()

            if (diffDays == 1) {
                currentStreak++
                longestStreak = maxOf(longestStreak, currentStreak)
            } else {
                currentStreak = 1
            }
        }

        android.util.Log.d("AnalyticsRepository", "calculateLongestStreak: result=$longestStreak")
        return longestStreak
    }

    private fun calculateAverageMoodScore(moods: List<MoodEntryEntity>): Float {
        if (moods.isEmpty()) return 0f
        val moodScores = moods.map { moodScore(it) }
        return moodScores.average().toFloat()
    }

    private fun getMostFrequentMood(moods: List<MoodEntryEntity>): MoodType? {
        if (moods.isEmpty()) return null
        return moods
            .map { parseMoodType(it.mood) }
            .filter { it in TRACKED_MOOD_TYPES }
            .groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key
    }

    private fun parseMoodType(moodRaw: String): MoodType {
        return runCatching { MoodType.valueOf(moodRaw.trim().uppercase(Locale.US)) }
            .getOrElse {
                when (moodRaw.lowercase(Locale.US)) {
                    "great" -> MoodType.GREAT
                    "good" -> MoodType.GOOD
                    "neutral" -> MoodType.NEUTRAL
                    "bad" -> MoodType.BAD
                    "terrible" -> MoodType.TERRIBLE
                    else -> MoodType.NEUTRAL
                }
            }
    }

    private fun calculateMoodDistribution(moods: List<MoodEntryEntity>): Map<MoodType, Int> {
        return moods
            .groupingBy { parseMoodType(it.mood) }
            .eachCount()
            .filterKeys { it in TRACKED_MOOD_TYPES }
    }

    private val TRACKED_MOOD_TYPES = setOf(
        MoodType.GREAT,
        MoodType.GOOD,
        MoodType.NEUTRAL,
        MoodType.BAD,
        MoodType.TERRIBLE
    )

    private fun calculateAverageIntensity(moods: List<MoodEntryEntity>): Float {
        if (moods.isEmpty()) return 0f
        return moods.map { it.intensity }.average().toFloat()
    }

    private fun getMostCommonActivities(moods: List<MoodEntryEntity>): List<String> {
        return computeActivityCounts(moods).take(5).map { it.label }
    }

    private fun calculateTrend(moods: List<MoodEntryEntity>): TrendDirection {
        val dailyScores = moods
            .sortedWith(compareBy({ it.date }, { it.timestamp }))
            .groupBy { it.date }
            .values
            .map { day -> day.map { moodScore(it) }.average().toFloat() }
            .takeLast(14)

        if (dailyScores.size < 3) return TrendDirection.STABLE

        val stdDev = calculateStandardDeviation(dailyScores).toFloat()
        val range = (dailyScores.maxOrNull() ?: 0f) - (dailyScores.minOrNull() ?: 0f)
        val avgScore = dailyScores.average()

        // Проверка на стабильность на высоком/низком уровне
        val isHighStable = avgScore >= 4.0f && stdDev < 0.5f && 
                           dailyScores.all { it >= 3.5f }
        val isLowStable = avgScore <= 2.0f && stdDev < 0.5f && 
                          dailyScores.all { it <= 2.5f }

        // Проверяем стабильность на сегментах данных
        val windowSize = maxOf(3, dailyScores.size / 3)
        var highStableSegments = 0
        var lowStableSegments = 0
        
        for (i in 0..dailyScores.size - windowSize) {
            val segment = dailyScores.subList(i, i + windowSize)
            val segmentAvg = segment.average()
            val segmentStdDev = calculateStandardDeviation(segment).toFloat()
            
            if (segmentAvg >= 4.0f && segmentStdDev < 0.5f && segment.all { it >= 3.5f }) {
                highStableSegments++
            } else if (segmentAvg <= 2.0f && segmentStdDev < 0.5f && segment.all { it <= 2.5f }) {
                lowStableSegments++
            }
        }

        val totalSegments = dailyScores.size - windowSize + 1
        if (totalSegments > 0) {
            val highStableRatio = highStableSegments.toFloat() / totalSegments
            val lowStableRatio = lowStableSegments.toFloat() / totalSegments
            
            if (highStableRatio >= 0.5f) return TrendDirection.STABLE_POSITIVE
            if (lowStableRatio >= 0.5f) return TrendDirection.STABLE_NEGATIVE
        }

        if (isHighStable) return TrendDirection.STABLE_POSITIVE
        if (isLowStable) return TrendDirection.STABLE_NEGATIVE

        // Проверка на восстановление после падения
        val firstHalf = dailyScores.take(dailyScores.size / 2)
        val secondHalf = dailyScores.drop(dailyScores.size / 2)
        val firstHalfAvg = firstHalf.average()
        val secondHalfAvg = secondHalf.average()
        
        if (firstHalfAvg < 2.5f && secondHalfAvg > 3.5f) {
            return TrendDirection.RECOVERING
        }

        // Большой разброс — это не «стабильно»
        if (stdDev >= 1.0f || range >= 2.0f) {
            return TrendDirection.VOLATILE
        }

        // Проверка на колебания без явного тренда
        val improvements = dailyScores.count { it > 3.5f }
        val declines = dailyScores.count { it < 2.5f }
        val total = dailyScores.size
        val improvementRatio = improvements.toFloat() / total
        val declineRatio = declines.toFloat() / total
        
        if (improvementRatio > 0.3f && declineRatio > 0.3f) {
            return TrendDirection.FLUCTUATING
        }

        val n = dailyScores.size
        val nF = n.toFloat()
        val sumX = (0 until n).sum().toFloat()
        val sumY = dailyScores.sum()
        val sumXY = dailyScores.mapIndexed { index: Int, score: Float -> index * score }.sum()
        val sumX2 = (0 until n).map { it * it }.sum().toFloat()
        val denominator = nF * sumX2 - sumX * sumX
        if (denominator == 0f) return TrendDirection.STABLE

        val slope = (nF * sumXY - sumX * sumY.toFloat()) / denominator
        val directionThreshold = 0.1f

        return when {
            slope > directionThreshold -> TrendDirection.IMPROVING
            slope < -directionThreshold -> TrendDirection.DECLINING
            else -> TrendDirection.STABLE
        }
    }

    private fun calculateStandardDeviation(values: List<Float>): Double {
        if (values.isEmpty()) return 0.0
        val avg = values.average()
        val variance = values.map { (it - avg) * (it - avg) }.average()
        return kotlin.math.sqrt(variance)
    }

    private fun filterMoodsByPeriod(moods: List<MoodEntryEntity>, period: AnalyticsPeriod): List<MoodEntryEntity> {
        if (period == AnalyticsPeriod.ALL_TIME) return moods
        val calendar = Calendar.getInstance()
        val daysToSubtract = periodDays(period, moods) ?: return moods
        calendar.add(Calendar.DAY_OF_YEAR, -daysToSubtract)
        val cutoffDate = dateFormat.format(calendar.time)
        return moods.filter { it.date > cutoffDate }
    }

    private fun periodDays(period: AnalyticsPeriod, @Suppress("UNUSED_PARAMETER") moods: List<MoodEntryEntity>): Int? {
        return when (period) {
            AnalyticsPeriod.WEEK -> 7
            AnalyticsPeriod.TWO_WEEKS -> 14
            AnalyticsPeriod.MONTH -> 30
            AnalyticsPeriod.ALL_TIME -> null
        }
    }

    // === Комплексный анализ тренда ===

    private fun calculateTrendStrength(moods: List<MoodEntryEntity>): TrendStrength {
        if (moods.isEmpty()) return TrendStrength.MODERATE

        val dailyScores = moods
            .sortedWith(compareBy({ it.date }, { it.timestamp }))
            .groupBy { it.date }
            .values
            .map { day -> day.map { moodScore(it) }.average().toFloat() }

        if (dailyScores.size < 2) return TrendStrength.MODERATE

        val changes = mutableListOf<Float>()
        for (i in 1 until dailyScores.size) {
            changes.add(dailyScores[i] - dailyScores[i - 1])
        }

        val avgChange = changes.map { kotlin.math.abs(it) }.average().toFloat()
        
        return when {
            avgChange < 0.3f -> TrendStrength.WEAK
            avgChange < 0.7f -> TrendStrength.MODERATE
            else -> TrendStrength.STRONG
        }
    }

    private fun calculateStabilityPeriods(moods: List<MoodEntryEntity>): List<StabilityPeriod> {
        if (moods.isEmpty()) return emptyList()

        val dailyScores = moods
            .sortedWith(compareBy({ it.date }, { it.timestamp }))
            .groupBy { it.date }
            .values
            .map { day -> day.map { moodScore(it) }.average().toFloat() }

        if (dailyScores.size < 2) return emptyList()

        val dates = moods
            .sortedWith(compareBy({ it.date }, { it.timestamp }))
            .map { it.date }
            .distinct()

        val stabilityPeriods = mutableListOf<StabilityPeriod>()
        var currentStreak = 0
        var streakStartIndex = 0
        val stabilityThreshold = 0.3f

        for (i in 1 until dailyScores.size) {
            val change = kotlin.math.abs(dailyScores[i] - dailyScores[i - 1])
            if (change < stabilityThreshold) {
                if (currentStreak == 0) {
                    streakStartIndex = i - 1
                }
                currentStreak++
            } else {
                if (currentStreak >= 2) {
                    val segmentScores = dailyScores.subList(streakStartIndex, i)
                    val segmentAvg = segmentScores.average()
                    val level = when {
                        segmentAvg >= 4.0f -> MoodLevel.HIGH
                        segmentAvg <= 2.0f -> MoodLevel.LOW
                        else -> MoodLevel.MEDIUM
                    }
                    
                    stabilityPeriods.add(
                        StabilityPeriod(
                            startDate = dates[streakStartIndex],
                            endDate = dates[i - 1],
                            level = level,
                            duration = currentStreak
                        )
                    )
                }
                currentStreak = 0
            }
        }

        if (currentStreak >= 2) {
            val segmentScores = dailyScores.subList(streakStartIndex, dailyScores.size)
            val segmentAvg = segmentScores.average()
            val level = when {
                segmentAvg >= 4.0f -> MoodLevel.HIGH
                segmentAvg <= 2.0f -> MoodLevel.LOW
                else -> MoodLevel.MEDIUM
            }
            
            stabilityPeriods.add(
                StabilityPeriod(
                    startDate = dates[streakStartIndex],
                    endDate = dates.last(),
                    level = level,
                    duration = currentStreak
                )
            )
        }

        return stabilityPeriods
    }

    private fun calculateTrendPeriods(moods: List<MoodEntryEntity>): List<TrendPeriod> {
        if (moods.isEmpty()) return emptyList()

        val dailyScores = moods
            .sortedWith(compareBy({ it.date }, { it.timestamp }))
            .groupBy { it.date }
            .values
            .map { day -> day.map { moodScore(it) }.average().toFloat() }

        if (dailyScores.size < 2) return emptyList()

        val dates = moods
            .sortedWith(compareBy({ it.date }, { it.timestamp }))
            .map { it.date }
            .distinct()

        val trendPeriods = mutableListOf<TrendPeriod>()
        var currentImprovementStreak = 0
        var currentDeclineStreak = 0
        var improvementStartIndex = 0
        var declineStartIndex = 0
        val trendThreshold = 0.1f

        for (i in 1 until dailyScores.size) {
            val change = dailyScores[i] - dailyScores[i - 1]
            
            if (change > trendThreshold) {
                if (currentImprovementStreak == 0) {
                    improvementStartIndex = i - 1
                }
                currentImprovementStreak++
                if (currentDeclineStreak >= 2) {
                    trendPeriods.add(
                        TrendPeriod(
                            startDate = dates[declineStartIndex],
                            endDate = dates[i - 1],
                            direction = TrendDirection.DECLINING,
                            duration = currentDeclineStreak
                        )
                    )
                }
                currentDeclineStreak = 0
            } else if (change < -trendThreshold) {
                if (currentDeclineStreak == 0) {
                    declineStartIndex = i - 1
                }
                currentDeclineStreak++
                if (currentImprovementStreak >= 2) {
                    trendPeriods.add(
                        TrendPeriod(
                            startDate = dates[improvementStartIndex],
                            endDate = dates[i - 1],
                            direction = TrendDirection.IMPROVING,
                            duration = currentImprovementStreak
                        )
                    )
                }
                currentImprovementStreak = 0
            } else {
                if (currentImprovementStreak >= 2) {
                    trendPeriods.add(
                        TrendPeriod(
                            startDate = dates[improvementStartIndex],
                            endDate = dates[i - 1],
                            direction = TrendDirection.IMPROVING,
                            duration = currentImprovementStreak
                        )
                    )
                }
                if (currentDeclineStreak >= 2) {
                    trendPeriods.add(
                        TrendPeriod(
                            startDate = dates[declineStartIndex],
                            endDate = dates[i - 1],
                            direction = TrendDirection.DECLINING,
                            duration = currentDeclineStreak
                        )
                    )
                }
                currentImprovementStreak = 0
                currentDeclineStreak = 0
            }
        }

        if (currentImprovementStreak >= 2) {
            trendPeriods.add(
                TrendPeriod(
                    startDate = dates[improvementStartIndex],
                    endDate = dates.last(),
                    direction = TrendDirection.IMPROVING,
                    duration = currentImprovementStreak
                )
            )
        }
        if (currentDeclineStreak >= 2) {
            trendPeriods.add(
                TrendPeriod(
                    startDate = dates[declineStartIndex],
                    endDate = dates.last(),
                    direction = TrendDirection.DECLINING,
                    duration = currentDeclineStreak
                )
            )
        }

        return trendPeriods
    }

    private fun calculateVolatilityIndex(moods: List<MoodEntryEntity>): Float {
        if (moods.isEmpty()) return 0f

        val dailyScores = moods
            .sortedWith(compareBy({ it.date }, { it.timestamp }))
            .groupBy { it.date }
            .values
            .map { day -> day.map { moodScore(it) }.average().toFloat() }

        if (dailyScores.size < 2) return 0f

        val stdDev = calculateStandardDeviation(dailyScores).toFloat()
        val maxRange = 4f // от 1 до 5
        
        return (stdDev / maxRange).coerceIn(0f, 1f)
    }

    private fun calculateMoodLevel(moods: List<MoodEntryEntity>): MoodLevel {
        if (moods.isEmpty()) return MoodLevel.MEDIUM

        val avgScore = moods.map { moodScore(it) }.average().toFloat()
        
        return when {
            avgScore >= 4.0f -> MoodLevel.HIGH
            avgScore <= 2.0f -> MoodLevel.LOW
            else -> MoodLevel.MEDIUM
        }
    }

    private fun periodLabelRu(period: AnalyticsPeriod): String = when (period) {
        AnalyticsPeriod.WEEK -> "7 дней"
        AnalyticsPeriod.TWO_WEEKS -> "14 дней"
        AnalyticsPeriod.MONTH -> "30 дней"
        AnalyticsPeriod.ALL_TIME -> "Всё время"
    }

    private fun calculateGoodDaysPercentage(moods: List<MoodEntryEntity>): Float {
        if (moods.isEmpty()) return 0f
        val goodDays = moods.count { moodScore(it) >= 4f }
        return (goodDays.toFloat() / moods.size) * 100f
    }
}
