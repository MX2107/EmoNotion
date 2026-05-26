package com.emonotion.app.domain.usecase.customactivity

import com.emonotion.app.domain.model.CustomActivity
import com.emonotion.app.domain.repository.CustomActivityRepository
import javax.inject.Inject

/**
 * Use Case для удаления пользовательской активности
 */
class DeleteCustomActivityUseCase @Inject constructor(
    private val customActivityRepository: CustomActivityRepository
) {
    suspend operator fun invoke(customActivity: CustomActivity): Result<Unit> {
        return try {
            customActivityRepository.deleteCustomActivity(customActivity)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend operator fun invoke(id: String): Result<Unit> {
        return try {
            customActivityRepository.deleteCustomActivityById(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
