package com.emonotion.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity для пользовательских занятий/активностей
 */
@Entity(tableName = "custom_activities")
data class CustomActivityEntity(
    @PrimaryKey 
    val id: String,
    val name: String, // Название занятия
    val category: String?, // Категория занятия
    val color: String?, // HEX цвет для отображения
    val icon: String?, // Название иконки
    val isActive: Boolean = true, // Активно ли занятие
    val createdAt: Long,
    val updatedAt: Long
) {
    companion object {
        fun fromDomain(customActivity: com.emonotion.app.domain.model.CustomActivity): CustomActivityEntity {
            return CustomActivityEntity(
                id = customActivity.id,
                name = customActivity.name,
                category = customActivity.category,
                color = customActivity.color,
                icon = customActivity.icon,
                isActive = customActivity.isActive,
                createdAt = customActivity.createdAt,
                updatedAt = customActivity.updatedAt
            )
        }
    }

    fun toDomain(): com.emonotion.app.domain.model.CustomActivity {
        return com.emonotion.app.domain.model.CustomActivity(
            id = id,
            name = name,
            category = category,
            color = color,
            icon = icon,
            isActive = isActive,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
