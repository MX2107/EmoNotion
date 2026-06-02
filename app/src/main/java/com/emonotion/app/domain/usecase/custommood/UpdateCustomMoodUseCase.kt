package com.emonotion.app.domain.usecase.custommood

import com.emonotion.app.domain.model.CustomMood
import com.emonotion.app.domain.repository.CustomMoodRepository
import javax.inject.Inject

/**
 * Use case для обновления пользовательской эмоции
 */
class UpdateCustomMoodUseCase @Inject constructor(
    private val customMoodRepository: CustomMoodRepository
) {
    suspend operator fun invoke(customMood: CustomMood): Result<Unit> {
        return try {
            customMoodRepository.updateCustomMood(customMood)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
