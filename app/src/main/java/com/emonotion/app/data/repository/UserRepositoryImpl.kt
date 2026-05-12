package com.emonotion.app.data.repository

import com.emonotion.app.data.local.dao.UserDao
import com.emonotion.app.data.local.entities.UserProfileEntity
import com.emonotion.app.domain.model.UserProfile
import com.emonotion.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Реализация Repository для работы с профилем пользователя
 */
@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao
) : UserRepository {
    
    override suspend fun getUserProfile(userId: String): UserProfile? {
        return userDao.getUserProfile(userId)?.toDomain()
    }
    
    override suspend fun getUserProfileFlow(userId: String): Flow<UserProfile?> {
        return userDao.getUserProfileFlow(userId).map { entity ->
            entity?.toDomain()
        }
    }
    
    override suspend fun updateProfile(profile: UserProfile) {
        userDao.updateUserProfile(UserProfileEntity.fromDomain(profile))
    }
    
    override suspend fun deleteProfile(userId: String) {
        userDao.deleteUserProfile(userId)
    }
}
