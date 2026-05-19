package com.emonotion.app.data.repository

import com.emonotion.app.data.local.dao.MoodDao
import com.emonotion.app.domain.model.Analytics
import com.emonotion.app.domain.model.AnalyticsPeriod
import com.emonotion.app.domain.model.MoodType
import com.emonotion.app.domain.model.TrendDirection
import com.emonotion.app.domain.model.UserStats
import com.emonotion.app.domain.repository.AnalyticsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository implementation для аналитики
 */
@Singleton
class AnalyticsRepositoryImpl @Inject constructor(
    private val moodDao: MoodDao
) : AnalyticsRepository {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    override suspend fun getAnalytics(period: AnalyticsPeriod): Analytics {
        val allMoods = moodDao.getAllMoodsList()
        val filteredMoods = filterMoodsByPeriod(allMoods, period)
        
        return Analytics(
            period = period,
            moodDistribution = calculateMoodDistribution(filteredMoods),
            averageIntensity = calculateAverageIntensity(filteredMoods),
            averageMood = calculateAverageMoodScore(filteredMoods),
            totalEntries = filteredMoods.size,
            currentStreak = calculateCurrentStreak(filteredMoods),
            longestStreak = calculateLongestStreak(filteredMoods),
            mostCommonActivities = getMostCommonActivities(filteredMoods),
            improvementTrend = calculateTrend(filteredMoods)
        )
    }
    
    override fun getAnalyticsFlow(): Flow<Analytics> {
        return moodDao.getAllMoods().map { moods ->
            Analytics(
                period = AnalyticsPeriod.WEEK,
                moodDistribution = calculateMoodDistribution(moods),
                averageIntensity = calculateAverageIntensity(moods),
                averageMood = calculateAverageMoodScore(moods),
                totalEntries = moods.size,
                currentStreak = calculateCurrentStreak(moods),
                longestStreak = calculateLongestStreak(moods),
                mostCommonActivities = getMostCommonActivities(moods),
                improvementTrend = calculateTrend(moods)
            )
        }
    }
    
    override fun getUserStats(): Flow<UserStats> {
        return moodDao.getAllMoods().map { moods ->
            calculateUserStats(moods)
        }
    }
    
    private fun calculateUserStats(moods: List<com.emonotion.app.data.local.entities.MoodEntryEntity>): UserStats {
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
    
    private fun calculateCurrentStreak(moods: List<com.emonotion.app.data.local.entities.MoodEntryEntity>): Int {
        if (moods.isEmpty()) {
            android.util.Log.d("AnalyticsRepository", "calculateCurrentStreak: moods is empty")
            return 0
        }
        
        val calendar = Calendar.getInstance()
        val today = dateFormat.format(calendar.time)
        
        // Фильтруем записи только на даты до сегодня или сегодня
        val validMoods = moods.filter { it.date <= today }
        val sortedDates = validMoods.map { it.date }.distinct().sortedDescending()
        
        android.util.Log.d("AnalyticsRepository", "calculateCurrentStreak: today=$today, sortedDates=$sortedDates")
        
        if (sortedDates.isEmpty()) return 0
        
        // Определяем дату начала отсчёта серии
        val checkCalendar = Calendar.getInstance()
        
        // Если сегодня нет записи, проверяем вчера
        if (sortedDates[0] != today) {
            checkCalendar.add(Calendar.DAY_OF_YEAR, -1)
            val yesterday = dateFormat.format(checkCalendar.time)
            
            // Если и вчера нет записи, серия прервана
            if (sortedDates[0] != yesterday) {
                android.util.Log.d("AnalyticsRepository", "calculateCurrentStreak: no entry today or yesterday, streak=0")
                return 0
            }
        }
        
        var streak = 0
        var currentDate = Calendar.getInstance()
        
        // Если первая дата не сегодня, начинаем проверку с вчерашнего дня
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
    
    private fun calculateLongestStreak(moods: List<com.emonotion.app.data.local.entities.MoodEntryEntity>): Int {
        if (moods.isEmpty()) {
            android.util.Log.d("AnalyticsRepository", "calculateLongestStreak: moods is empty")
            return 0
        }
        
        // Фильтруем записи только на даты до сегодня или сегодня
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
    
    private fun calculateAverageMoodScore(moods: List<com.emonotion.app.data.local.entities.MoodEntryEntity>): Float {
        if (moods.isEmpty()) return 0f
        
        val moodScores = moods.mapNotNull { mood ->
            when (mood.mood.lowercase()) {
                "great" -> 5f
                "good" -> 4f
                "neutral" -> 3f
                "bad" -> 2f
                "terrible" -> 1f
                else -> 3f
            }
        }
        
        return if (moodScores.isNotEmpty()) {
            moodScores.average().toFloat()
        } else {
            0f
        }
    }
    
    private fun getMostFrequentMood(moods: List<com.emonotion.app.data.local.entities.MoodEntryEntity>): MoodType? {
        if (moods.isEmpty()) return null
        
        val moodCounts = moods.groupingBy { it.mood }.eachCount()
        val mostFrequent = moodCounts.maxByOrNull { it.value }?.key ?: return null
        
        return when (mostFrequent) {
            "great" -> MoodType.GREAT
            "good" -> MoodType.GOOD
            "neutral" -> MoodType.NEUTRAL
            "bad" -> MoodType.BAD
            "terrible" -> MoodType.TERRIBLE
            else -> MoodType.NEUTRAL
        }
    }
    
    private fun calculateMoodDistribution(moods: List<com.emonotion.app.data.local.entities.MoodEntryEntity>): Map<MoodType, Int> {
        return moods.groupingBy { 
            when (it.mood) {
                "great" -> MoodType.GREAT
                "good" -> MoodType.GOOD
                "neutral" -> MoodType.NEUTRAL
                "bad" -> MoodType.BAD
                "terrible" -> MoodType.TERRIBLE
                else -> MoodType.NEUTRAL
            }
        }.eachCount()
    }
    
    private fun calculateAverageIntensity(moods: List<com.emonotion.app.data.local.entities.MoodEntryEntity>): Float {
        if (moods.isEmpty()) return 0f
        return moods.map { it.intensity }.average().toFloat()
    }
    
    private fun getMostCommonActivities(moods: List<com.emonotion.app.data.local.entities.MoodEntryEntity>): List<String> {
        val allActivities = moods.flatMap { mood ->
            if (mood.activities.isEmpty()) emptyList<String>()
            else mood.activities.split(",")
        }
        if (allActivities.isEmpty()) return emptyList()
        
        val activityCounts = allActivities.groupingBy { activity -> activity }.eachCount()
        return activityCounts.entries
            .sortedByDescending { entry -> entry.value }
            .take(5)
            .map { entry -> entry.key }
    }
    
    private fun calculateTrend(moods: List<com.emonotion.app.data.local.entities.MoodEntryEntity>): TrendDirection {
        if (moods.size < 2) return TrendDirection.STABLE
        
        val recentMoods = moods.takeLast(7)
        val olderMoods = moods.dropLast(7).takeLast(7)
        
        if (olderMoods.isEmpty()) return TrendDirection.STABLE
        
        val recentAvg = recentMoods.map { it.intensity }.average()
        val olderAvg = olderMoods.map { it.intensity }.average()
        
        return when {
            recentAvg > olderAvg + 0.5 -> TrendDirection.IMPROVING
            recentAvg < olderAvg - 0.5 -> TrendDirection.DECLINING
            else -> TrendDirection.STABLE
        }
    }
    
    private fun filterMoodsByPeriod(moods: List<com.emonotion.app.data.local.entities.MoodEntryEntity>, period: AnalyticsPeriod): List<com.emonotion.app.data.local.entities.MoodEntryEntity> {
        val calendar = Calendar.getInstance()
        val daysToSubtract = when (period) {
            AnalyticsPeriod.WEEK -> 7
            AnalyticsPeriod.TWO_WEEKS -> 14
            AnalyticsPeriod.MONTH -> 30
        }
        
        calendar.add(Calendar.DAY_OF_YEAR, -daysToSubtract)
        val cutoffDate = calendar.timeInMillis
        
        return moods.filter { it.timestamp >= cutoffDate }
    }
}
