package com.emonotion.app.domain.usecase.customtag

import com.emonotion.app.domain.model.CustomTag
import com.emonotion.app.domain.repository.CustomTagRepository
import javax.inject.Inject

/**
 * Use case для обновления пользовательского тега
 */
class UpdateCustomTagUseCase @Inject constructor(
    private val customTagRepository: CustomTagRepository
) {
    suspend operator fun invoke(customTag: CustomTag): Result<Unit> {
        return try {
            customTagRepository.updateCustomTag(customTag)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
