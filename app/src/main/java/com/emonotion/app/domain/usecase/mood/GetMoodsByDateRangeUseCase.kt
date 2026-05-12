package com.emonotion.app.domain.usecase.mood

import com.emonotion.app.domain.model.MoodEntry
import com.emonotion.app.domain.repository.MoodRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use Case для получения записей о настроении за период времени
 */
class GetMoodsByDateRangeUseCase @Inject constructor(
    private val moodRepository: MoodRepository
) {
    suspend operator fun invoke(startDate: String, endDate: String): Flow<List<MoodEntry>> {
        return moodRepository.getMoodsByDateRange(startDate, endDate)
    }
    
    suspend operator fun invoke(from: Long, to: Long): Flow<List<MoodEntry>> {
        return moodRepository.getMoodsByDateRange(from, to)
    }
}
