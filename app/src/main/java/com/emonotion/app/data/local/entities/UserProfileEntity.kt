package com.emonotion.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity для профиля пользователя в базе данных Room
 */
@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey 
    val userId: String = "current_user",
    val name: String,
    val avatar: String?, // URI or path
    val email: String?,
    val createdAt: Long,
    val updatedAt: Long
) {
    companion object {
        fun fromDomain(userProfile: com.emonotion.app.domain.model.UserProfile): UserProfileEntity {
            return UserProfileEntity(
                userId = userProfile.userId,
                name = userProfile.name,
                avatar = userProfile.avatar,
                email = userProfile.email,
                createdAt = userProfile.createdAt,
                updatedAt = userProfile.updatedAt
            )
        }
    }

    fun toDomain(): com.emonotion.app.domain.model.UserProfile {
        return com.emonotion.app.domain.model.UserProfile(
            userId = userId,
            name = name,
            avatar = avatar,
            email = email,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
