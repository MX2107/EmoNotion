package com.emonotion.app.domain.model

/**
 * Эмодзи для отображения настроения
 */
object MoodEmoji {
    const val TERRIBLE = "🤬"  // Ужасно (1)
    const val BAD = "😟"       // Плохо (2)
    const val NEUTRAL = "😐"   // Нормально (3)
    const val GOOD = "🙂"      // Хорошо (4)
    const val GREAT = "😊"     // Отлично (5)

    /**
     * Получить эмодзи по числовому значению настроения (1-5)
     */
    fun fromScore(score: Float): String {
        return when {
            score >= 4.5f -> GREAT
            score >= 3.5f -> GOOD
            score >= 2.5f -> NEUTRAL
            score >= 1.5f -> BAD
            score > 0f -> TERRIBLE
            else -> NEUTRAL
        }
    }

    /**
     * Получить эмодзи по типу настроения
     */
    fun fromMoodType(mood: MoodType): String {
        return when (mood) {
            MoodType.GREAT -> GREAT
            MoodType.GOOD -> GOOD
            MoodType.NEUTRAL -> NEUTRAL
            MoodType.BAD -> BAD
            MoodType.TERRIBLE -> TERRIBLE
            MoodType.HAPPY -> GREAT
            MoodType.SAD -> TERRIBLE
            MoodType.ANGRY -> TERRIBLE
            MoodType.ANXIOUS -> BAD
        }
    }
}
