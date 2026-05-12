package com.emonotion.app.domain.usecase.mood

import com.emonotion.app.domain.model.MoodEntry
import com.emonotion.app.domain.repository.MoodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * Use Case для получения записи о настроении за сегодня
 */
class GetTodayMoodUseCase @Inject constructor(
    private val moodRepository: MoodRepository
) {
    operator fun invoke(): Flow<MoodEntry?> {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return moodRepository.getAllMoods().map { moods: List<MoodEntry> ->
            moods.find { moodEntry: MoodEntry -> moodEntry.date == today }
        }
    }
}
