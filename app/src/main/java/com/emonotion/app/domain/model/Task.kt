package com.emonotion.app.domain.model

/**
 * Модель задачи
 */
data class Task(
    val id: String,
    val title: String,
    val description: String? = null,
    val isCompleted: Boolean = false,
    val timestamp: Long,
    val date: String, // yyyy-MM-dd
    val priority: TaskPriority = TaskPriority.MEDIUM
)

/**
 * Приоритеты задач
 */
enum class TaskPriority(val displayName: String) {
    LOW("Низкий"),
    MEDIUM("Средний"),
    HIGH("Высокий")
}
