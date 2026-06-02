package com.emonotion.app.domain.usecase.customactivity

import com.emonotion.app.domain.model.CustomActivity
import com.emonotion.app.domain.repository.CustomActivityRepository
import javax.inject.Inject

/**
 * Use case для обновления пользовательской активности
 */
class UpdateCustomActivityUseCase @Inject constructor(
    private val customActivityRepository: CustomActivityRepository
) {
    suspend operator fun invoke(customActivity: CustomActivity): Result<Unit> {
        return try {
            customActivityRepository.updateCustomActivity(customActivity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
