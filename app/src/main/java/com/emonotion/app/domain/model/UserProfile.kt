package com.emonotion.app.domain.model

/**
 * Модель профиля пользователя
 */
data class UserProfile(
    val userId: String = "current_user",
    val name: String,
    val avatar: String? = null, // URI or path
    val email: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)
