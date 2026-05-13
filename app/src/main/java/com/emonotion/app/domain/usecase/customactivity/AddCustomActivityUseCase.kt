package com.emonotion.app.domain.usecase.customactivity

import com.emonotion.app.domain.model.CustomActivity
import com.emonotion.app.domain.repository.CustomActivityRepository
import javax.inject.Inject

/**
 * Use Case для добавления пользовательского занятия/активности
 */
class AddCustomActivityUseCase @Inject constructor(
    private val customActivityRepository: CustomActivityRepository
) {
    suspend operator fun invoke(customActivity: CustomActivity): Result<Unit> {
        return try {
            // Проверяем, что занятие с таким именем еще не существует
            val existingActivity = customActivityRepository.getCustomActivityByName(customActivity.name)
            if (existingActivity != null) {
                return Result.failure(IllegalArgumentException("Занятие с таким названием уже существует"))
            }
            
            customActivityRepository.addCustomActivity(customActivity)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
