package com.emonotion.app.domain.usecase.mood

import com.emonotion.app.domain.model.MoodEntry
import com.emonotion.app.domain.repository.MoodRepository
import javax.inject.Inject

/**
 * Use Case для удаления записи о настроении
 */
class DeleteMoodUseCase @Inject constructor(
    private val moodRepository: MoodRepository
) {
    suspend operator fun invoke(mood: MoodEntry): Result<Unit> {
        return try {
            moodRepository.deleteMood(mood)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend operator fun invoke(id: String): Result<Unit> {
        return try {
            moodRepository.deleteMoodById(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
