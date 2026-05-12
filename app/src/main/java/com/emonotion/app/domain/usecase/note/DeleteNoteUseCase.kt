package com.emonotion.app.domain.usecase.note

import com.emonotion.app.domain.model.Note
import com.emonotion.app.domain.repository.NoteRepository
import javax.inject.Inject

/**
 * Use Case для удаления заметки
 */
class DeleteNoteUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    suspend operator fun invoke(note: Note): Result<Unit> {
        return try {
            noteRepository.deleteNote(note)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend operator fun invoke(noteId: String): Result<Unit> {
        return try {
            noteRepository.deleteNoteById(noteId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
