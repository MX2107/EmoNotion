package com.emonotion.app.domain.usecase.draft

import com.emonotion.app.domain.model.MoodEntry
import com.emonotion.app.domain.repository.DraftRepository
import javax.inject.Inject

/**
 * UseCase для удаления черновика
 */
class DeleteDraftUseCase @Inject constructor(
    private val draftRepository: DraftRepository
) {
    suspend operator fun invoke(draft: MoodEntry) {
        draftRepository.deleteDraft(draft)
    }
    
    suspend operator fun invoke(date: String) {
        draftRepository.deleteDraftByDate(date)
    }
}
