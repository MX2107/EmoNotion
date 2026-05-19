package com.emonotion.app.domain.model

/**
 * Модель статистики пользователя
 */
data class UserStats(
    val totalEntries: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val averageMood: Float = 0f,
    val mostFrequentMood: MoodType? = null,
    val totalDaysTracked: Int = 0,
    /** Есть запись в дневнике за сегодня (yyyy-MM-dd). */
    val hasEntryToday: Boolean = false
)
