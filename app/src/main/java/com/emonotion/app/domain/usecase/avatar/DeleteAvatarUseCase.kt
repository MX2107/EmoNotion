package com.emonotion.app.domain.usecase.avatar

import com.emonotion.app.domain.repository.AvatarRepository
import javax.inject.Inject

/**
 * Use Case для удаления аватара пользователя
 */
class DeleteAvatarUseCase @Inject constructor(
    private val avatarRepository: AvatarRepository
) {
    suspend operator fun invoke(userId: String = "current_user"): Result<Unit> {
        return avatarRepository.deleteAvatar(userId)
    }
    
    suspend fun deleteByPath(path: String): Result<Unit> {
        return avatarRepository.deleteAvatarByPath(path)
    }
}
