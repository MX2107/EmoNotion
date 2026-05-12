package com.emonotion.app.domain.repository

import com.emonotion.app.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

/**
 * Repository интерфейс для работы с профилем пользователя
 */
interface UserRepository {
    
    /**
     * Получить профиль пользователя
     */
    suspend fun getUserProfile(userId: String = "current_user"): UserProfile?
    
    /**
     * Получить профиль пользователя как Flow
     */
    suspend fun getUserProfileFlow(userId: String = "current_user"): Flow<UserProfile?>
    
    /**
     * Обновить профиль пользователя
     */
    suspend fun updateProfile(profile: UserProfile)
    
    /**
     * Удалить профиль пользователя
     */
    suspend fun deleteProfile(userId: String = "current_user")
}
