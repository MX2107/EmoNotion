package com.emonotion.app.domain.usecase.note

import com.emonotion.app.domain.model.Note
import com.emonotion.app.domain.repository.NoteRepository
import javax.inject.Inject

/**
 * Use Case для обновления заметки
 */
class UpdateNoteUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    suspend operator fun invoke(note: Note): Result<Unit> {
        return try {
            noteRepository.updateNote(note)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
