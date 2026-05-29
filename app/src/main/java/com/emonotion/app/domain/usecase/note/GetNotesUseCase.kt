package com.emonotion.app.domain.usecase.note

import com.emonotion.app.domain.model.Note
import com.emonotion.app.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use Case для получения заметок
 */
class GetNotesUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    suspend operator fun invoke(): Flow<List<Note>> {
        return noteRepository.getAllNotes()
    }
    
    suspend fun getNotesByDate(date: String): Flow<List<Note>> {
        return noteRepository.getNotesByDate(date)
    }
    
    suspend fun searchNotes(query: String): Flow<List<Note>> {
        return noteRepository.searchNotes(query)
    }
    
    suspend fun getNotesPaginated(limit: Int, offset: Int): Flow<List<Note>> {
        return noteRepository.getNotesPaginated(limit, offset)
    }
    
    suspend fun getRecentNotes(limit: Int): Flow<List<Note>> {
        return noteRepository.getRecentNotes(limit)
    }
}
