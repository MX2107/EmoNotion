package com.emonotion.app.domain.usecase.avatar

import android.graphics.Bitmap
import com.emonotion.app.domain.repository.AvatarRepository
import javax.inject.Inject

/**
 * Use Case для сохранения аватара пользователя
 */
class SaveAvatarUseCase @Inject constructor(
    private val avatarRepository: AvatarRepository
) {
    suspend operator fun invoke(bitmap: Bitmap, userId: String = "current_user"): Result<String> {
        return avatarRepository.saveAvatar(bitmap, userId)
    }
}
