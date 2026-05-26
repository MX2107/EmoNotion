package com.emonotion.app.domain.usecase.customtag

import com.emonotion.app.domain.model.CustomTag
import com.emonotion.app.domain.repository.CustomTagRepository
import javax.inject.Inject

/**
 * Use Case для удаления пользовательского тега
 */
class DeleteCustomTagUseCase @Inject constructor(
    private val customTagRepository: CustomTagRepository
) {
    suspend operator fun invoke(customTag: CustomTag): Result<Unit> {
        return try {
            customTagRepository.deleteCustomTag(customTag)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend operator fun invoke(id: String): Result<Unit> {
        return try {
            customTagRepository.deleteCustomTagById(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
