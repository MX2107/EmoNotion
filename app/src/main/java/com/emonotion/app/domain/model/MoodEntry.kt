package com.emonotion.app.domain.model

/**
 * Модель записи о настроении
 */
data class MoodEntry(
    val id: String,
    val mood: MoodType?,
    val intensity: Int, // 1-5
    val emotions: List<String> = emptyList(),
    val activities: List<String> = emptyList(),
    val notes: String?,
    val timestamp: Long,
    val date: String // yyyy-MM-dd
)

/**
 * Типы настроений
 */
enum class MoodType(val displayName: String) {
    HAPPY("Счастливое"),
    SAD("Грустное"),
    ANGRY("Злое"),
    ANXIOUS("Тревожное"),
    NEUTRAL("Нейтральное"),
    GREAT("Отлично"),
    GOOD("Хорошо"),
    BAD("Плохо"),
    TERRIBLE("Ужасно")
}
