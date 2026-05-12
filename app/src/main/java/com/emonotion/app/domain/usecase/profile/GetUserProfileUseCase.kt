package com.emonotion.app.domain.usecase.profile

import com.emonotion.app.domain.model.UserProfile
import com.emonotion.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use Case для получения профиля пользователя
 */
class GetUserProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Flow<UserProfile?> {
        return userRepository.getUserProfileFlow()
    }
    
    suspend operator fun invoke(userId: String): Flow<UserProfile?> {
        return userRepository.getUserProfileFlow(userId)
    }
}
