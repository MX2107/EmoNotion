package com.emonotion.app.domain.model

/**
 * Модель пользовательского тега
 */
data class CustomTag(
    val id: String,
    val name: String,
    val color: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
