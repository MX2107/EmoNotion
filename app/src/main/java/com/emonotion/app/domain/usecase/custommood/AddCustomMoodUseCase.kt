package com.emonotion.app.domain.usecase.custommood

import com.emonotion.app.domain.model.CustomMood
import com.emonotion.app.domain.repository.CustomMoodRepository
import javax.inject.Inject

/**
 * Use Case для добавления пользовательского настроения
 */
class AddCustomMoodUseCase @Inject constructor(
    private val customMoodRepository: CustomMoodRepository
) {
    suspend operator fun invoke(customMood: CustomMood): Result<Unit> {
        return try {
            // Проверяем, что настроение с таким именем еще не существует
            val existingMood = customMoodRepository.getCustomMoodByName(customMood.name)
            if (existingMood != null) {
                return Result.failure(IllegalArgumentException("Настроение с таким названием уже существует"))
            }
            
            customMoodRepository.addCustomMood(customMood)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
