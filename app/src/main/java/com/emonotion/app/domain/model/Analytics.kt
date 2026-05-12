package com.emonotion.app.domain.model

/**
 * Модель аналитики настроений
 */
data class Analytics(
    val period: AnalyticsPeriod,
    val moodDistribution: Map<MoodType, Int>,
    val averageIntensity: Float,
    val totalEntries: Int,
    val mostCommonActivities: List<String>,
    val improvementTrend: TrendDirection
)

/**
 * Периоды аналитики
 */
enum class AnalyticsPeriod {
    WEEK,
    MONTH,
    QUARTER,
    YEAR
}

/**
 * Направление тренда
 */
enum class TrendDirection {
    IMPROVING,
    STABLE,
    DECLINING
}
