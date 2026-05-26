package com.emonotion.app.domain.usecase.custommood

import com.emonotion.app.domain.model.CustomMood
import com.emonotion.app.domain.repository.CustomMoodRepository
import javax.inject.Inject

/**
 * Use Case для удаления пользовательского настроения
 */
class DeleteCustomMoodUseCase @Inject constructor(
    private val customMoodRepository: CustomMoodRepository
) {
    suspend operator fun invoke(customMood: CustomMood): Result<Unit> {
        return try {
            customMoodRepository.deleteCustomMood(customMood)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend operator fun invoke(id: String): Result<Unit> {
        return try {
            customMoodRepository.deleteCustomMoodById(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
