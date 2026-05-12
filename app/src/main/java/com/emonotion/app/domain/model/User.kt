package com.emonotion.app.domain.model

/**
 * Модель пользователя
 */
data class User(
    val id: String,
    val email: String,
    val name: String,
    val avatar: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)
