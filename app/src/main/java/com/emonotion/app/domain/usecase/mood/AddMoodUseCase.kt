package com.emonotion.app.domain.usecase.mood

import com.emonotion.app.domain.model.MoodEntry
import com.emonotion.app.domain.repository.MoodRepository
import javax.inject.Inject

/**
 * Use Case для добавления записи о настроении
 */
class AddMoodUseCase @Inject constructor(
    private val moodRepository: MoodRepository
) {
    suspend operator fun invoke(mood: MoodEntry): Result<Unit> {
        return try {
            moodRepository.insertMood(mood)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
