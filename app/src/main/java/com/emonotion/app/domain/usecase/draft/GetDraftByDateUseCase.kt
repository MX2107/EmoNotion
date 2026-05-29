package com.emonotion.app.domain.usecase.draft

import com.emonotion.app.domain.model.MoodEntry
import com.emonotion.app.domain.repository.DraftRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase для получения черновика по дате
 */
class GetDraftByDateUseCase @Inject constructor(
    private val draftRepository: DraftRepository
) {
    suspend operator fun invoke(date: String): MoodEntry? {
        return draftRepository.getDraftByDate(date)
    }
    
    fun invokeAsFlow(date: String): Flow<MoodEntry?> {
        return draftRepository.getDraftByDateFlow(date)
    }
}
