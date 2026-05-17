package com.emonotion.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity для пользовательских тегов
 */
@Entity(tableName = "custom_tags")
data class CustomTagEntity(
    @PrimaryKey 
    val id: String,
    val name: String, // Название тега
    val color: String?, // HEX цвет для отображения
    val isActive: Boolean = true, // Активен ли тег
    val createdAt: Long,
    val updatedAt: Long
) {
    companion object {
        fun fromDomain(customTag: com.emonotion.app.domain.model.CustomTag): CustomTagEntity {
            return CustomTagEntity(
                id = customTag.id,
                name = customTag.name,
                color = customTag.color,
                isActive = customTag.isActive,
                createdAt = customTag.createdAt,
                updatedAt = customTag.updatedAt
            )
        }
    }

    fun toDomain(): com.emonotion.app.domain.model.CustomTag {
        return com.emonotion.app.domain.model.CustomTag(
            id = id,
            name = name,
            color = color,
            isActive = isActive,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
