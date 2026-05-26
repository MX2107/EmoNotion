package com.emonotion.app.domain.usecase.customtag

import com.emonotion.app.domain.model.CustomTag
import com.emonotion.app.domain.repository.CustomTagRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use Case для получения пользовательских тегов
 */
class GetCustomTagsUseCase @Inject constructor(
    private val customTagRepository: CustomTagRepository
) {
    /**
     * Получает все активные пользовательские теги
     */
    operator fun invoke(): Flow<List<CustomTag>> {
        return customTagRepository.getAllActiveCustomTags()
    }
    
    /**
     * Получает все пользовательские теги (включая неактивные)
     */
    fun getAllCustomTags(): Flow<List<CustomTag>> {
        return customTagRepository.getAllCustomTags()
    }
}
