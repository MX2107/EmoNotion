package com.emonotion.app.domain.model

/**
 * Модель аналитики настроений
 */
data class Analytics(
    val period: AnalyticsPeriod,
    val moodDistribution: Map<MoodType, Int>,
    val averageIntensity: Float,
    val averageMood: Float,
    val totalEntries: Int,
    val currentStreak: Int,
    val longestStreak: Int,
    val mostCommonActivities: List<String>,
    val improvementTrend: TrendDirection
)

/**
 * Периоды аналитики
 */
enum class AnalyticsPeriod {
    WEEK,
    TWO_WEEKS,
    MONTH
}

/**
 * Направление тренда
 */
enum class TrendDirection {
    IMPROVING,
    STABLE,
    DECLINING
}
