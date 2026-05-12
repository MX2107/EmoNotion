package com.emonotion.app.domain.usecase.profile

import com.emonotion.app.domain.model.UserProfile
import com.emonotion.app.domain.repository.UserRepository
import javax.inject.Inject

/**
 * Use Case для обновления профиля пользователя
 */
class UpdateUserProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(profile: UserProfile): Result<Unit> {
        return try {
            userRepository.updateProfile(profile)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
