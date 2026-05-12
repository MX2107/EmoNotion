package com.emonotion.app.domain.usecase.note

import com.emonotion.app.domain.model.Note
import com.emonotion.app.domain.repository.NoteRepository
import javax.inject.Inject

/**
 * Use Case для добавления заметки
 */
class AddNoteUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    suspend operator fun invoke(note: Note): Result<Unit> {
        return try {
            noteRepository.insertNote(note)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
