package com.emonotion.app.domain.usecase.customtag

import com.emonotion.app.domain.model.CustomTag
import com.emonotion.app.domain.repository.CustomTagRepository
import javax.inject.Inject

/**
 * Use Case для добавления пользовательского тега
 */
class AddCustomTagUseCase @Inject constructor(
    private val customTagRepository: CustomTagRepository
) {
    suspend operator fun invoke(customTag: CustomTag): Result<Unit> {
        return try {
            // Проверяем, что тег с таким именем еще не существует
            val existingTag = customTagRepository.getCustomTagByName(customTag.name)
            if (existingTag != null) {
                return Result.failure(IllegalArgumentException("Тег с таким названием уже существует"))
            }
            
            customTagRepository.addCustomTag(customTag)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
