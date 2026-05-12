package com.emonotion.app.domain.model

/**
 * Модель заметки
 */
data class Note(
    val id: String,
    val title: String,
    val content: String,
    val timestamp: Long,
    val date: String, // yyyy-MM-dd
    val tags: List<String> = emptyList(),
    val isPinned: Boolean = false
)
