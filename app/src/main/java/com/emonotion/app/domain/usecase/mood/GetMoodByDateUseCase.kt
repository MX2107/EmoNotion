package com.emonotion.app.domain.usecase.mood

import com.emonotion.app.domain.model.MoodEntry
import com.emonotion.app.domain.repository.MoodRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case для получения записи о настроении по конкретной дате
 */
class GetMoodByDateUseCase @Inject constructor(
    private val moodRepository: MoodRepository
) {
    operator fun invoke(date: String): Flow<MoodEntry?> {
        return moodRepository.getMoodByDate(date)
    }
}
