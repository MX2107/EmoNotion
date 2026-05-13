package com.emonotion.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity для пользовательских настроений
 */
@Entity(tableName = "custom_moods")
data class CustomMoodEntity(
    @PrimaryKey 
    val id: String,
    val name: String, // Название настроения
    val color: String?, // HEX цвет для отображения
    val icon: String?, // Название иконки
    val isActive: Boolean = true, // Активно ли настроение
    val createdAt: Long,
    val updatedAt: Long
) {
    companion object {
        fun fromDomain(customMood: com.emonotion.app.domain.model.CustomMood): CustomMoodEntity {
            return CustomMoodEntity(
                id = customMood.id,
                name = customMood.name,
                color = customMood.color,
                icon = customMood.icon,
                isActive = customMood.isActive,
                createdAt = customMood.createdAt,
                updatedAt = customMood.updatedAt
            )
        }
    }

    fun toDomain(): com.emonotion.app.domain.model.CustomMood {
        return com.emonotion.app.domain.model.CustomMood(
            id = id,
            name = name,
            color = color,
            icon = icon,
            isActive = isActive,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
