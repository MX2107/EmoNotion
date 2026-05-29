package com.emonotion.app.domain.usecase.draft

import com.emonotion.app.domain.model.MoodEntry
import com.emonotion.app.domain.repository.DraftRepository
import javax.inject.Inject

/**
 * UseCase для сохранения черновика записи о настроении
 */
class SaveDraftUseCase @Inject constructor(
    private val draftRepository: DraftRepository
) {
    suspend operator fun invoke(moodEntry: MoodEntry) {
        draftRepository.saveDraft(moodEntry)
    }
}
