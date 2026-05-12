package com.emonotion.app.data.repository

import com.emonotion.app.data.local.dao.NoteDao
import com.emonotion.app.data.local.entities.NoteEntity
import com.emonotion.app.domain.model.Note
import com.emonotion.app.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Реализация Repository для работы с заметками
 */
@Singleton
class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao
) : NoteRepository {
    
    override suspend fun getAllNotes(): Flow<List<Note>> {
        return noteDao.getAllNotes().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun getNotesByDate(date: String): Flow<List<Note>> {
        return noteDao.getNotesByDate(date).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun getNoteById(id: String): Note? {
        return noteDao.getNoteById(id)?.toDomain()
    }
    
    override suspend fun getPinnedNotes(): Flow<List<Note>> {
        return noteDao.getPinnedNotes().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun searchNotes(query: String): Flow<List<Note>> {
        return noteDao.searchNotes(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun getNotesByTag(tag: String): Flow<List<Note>> {
        return noteDao.getNotesByTag(tag).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun insertNote(note: Note) {
        noteDao.insertNote(NoteEntity.fromDomain(note))
    }
    
    override suspend fun updateNote(note: Note) {
        noteDao.updateNote(NoteEntity.fromDomain(note))
    }
    
    override suspend fun deleteNote(note: Note) {
        noteDao.deleteNote(NoteEntity.fromDomain(note))
    }
    
    override suspend fun deleteNoteById(id: String) {
        noteDao.deleteNoteById(id)
    }
    
    override suspend fun getNotesCount(): Int {
        return noteDao.getNotesCount()
    }
    
    override suspend fun getNotesCountByDate(date: String): Int {
        return noteDao.getNotesCountByDate(date)
    }
}
