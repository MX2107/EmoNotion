package com.emonotion.app.domain.model

/**
 * Точка линейного графика: дата и средний балл настроения за день.
 */
data class MoodTrendPoint(
    val date: String,
    val averageScore: Float
)

/**
 * Подпись и значение для столбчатой диаграммы.
 */
data class NamedCount(
    val label: String,
    val count: Int
)

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
    val improvementTrend: TrendDirection,
    val moodTrendByDate: List<MoodTrendPoint> = emptyList(),
    val activityCounts: List<NamedCount> = emptyList(),
    val emotionCounts: List<NamedCount> = emptyList(),
    val goodDaysPercentage: Float = 0f,
    // Дополнительные метрики для комплексного анализа тренда
    val trendStrength: TrendStrength = TrendStrength.MODERATE,
    val stabilityPeriods: Int = 0,
    val stabilityPeriodsList: List<StabilityPeriod> = emptyList(),
    val trendPeriodsList: List<TrendPeriod> = emptyList(),
    val volatilityIndex: Float = 0f,
    val moodLevel: MoodLevel = MoodLevel.MEDIUM
)

/**
 * Формат экспорта отчёта аналитики.
 */
enum class AnalyticsExportFormat {
    CSV,
    PDF
}

/**
 * Периоды аналитики
 */
enum class AnalyticsPeriod {
    WEEK,
    TWO_WEEKS,
    MONTH,
    ALL_TIME
}

/**
 * Направление тренда
 */
enum class TrendDirection {
    IMPROVING,
    STABLE,
    DECLINING,
    /** Сильные перепады — нельзя считать стабильным трендом */
    VOLATILE,
    /** Стабильно на высоком уровне (4-5) */
    STABLE_POSITIVE,
    /** Стабильно на низком уровне (1-2) */
    STABLE_NEGATIVE,
    /** Восстановление после падения */
    RECOVERING,
    /** Колебания без явного тренда */
    FLUCTUATING
}

/**
 * Сила тренда
 */
enum class TrendStrength {
    WEAK,
    MODERATE,
    STRONG
}

/**
 * Уровень настроения
 */
enum class MoodLevel {
    LOW,    // 1-2
    MEDIUM, // 3
    HIGH    // 4-5
}

/**
 * Период стабильности настроения
 */
data class StabilityPeriod(
    val startDate: String,
    val endDate: String,
    val level: MoodLevel,
    val duration: Int // количество дней
)

/**
 * Период изменения настроения (улучшение или ухудшение)
 */
data class TrendPeriod(
    val startDate: String,
    val endDate: String,
    val direction: TrendDirection, // IMPROVING или DECLINING
    val duration: Int // количество дней
)
