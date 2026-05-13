package com.emonotion.app.domain.model

/**
 * Модель пользовательского занятия/активности
 */
data class CustomActivity(
    val id: String,
    val name: String,
    val category: String? = null,
    val color: String? = null,
    val icon: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
