package com.emonotion.app.domain.repository

import com.emonotion.app.domain.model.Note
import kotlinx.coroutines.flow.Flow

/**
 * Repository интерфейс для работы с заметками
 */
interface NoteRepository {
    
    /**
     * Получить все заметки
     */
    suspend fun getAllNotes(): Flow<List<Note>>
    
    /**
     * Получить заметки за конкретную дату
     */
    suspend fun getNotesByDate(date: String): Flow<List<Note>>
    
    /**
     * Получить заметку по ID
     */
    suspend fun getNoteById(id: String): Note?
    
    /**
     * Получить закрепленные заметки
     */
    suspend fun getPinnedNotes(): Flow<List<Note>>
    
    /**
     * Поиск заметок по тексту
     */
    suspend fun searchNotes(query: String): Flow<List<Note>>
    
    /**
     * Получить заметки по тегу
     */
    suspend fun getNotesByTag(tag: String): Flow<List<Note>>
    
    /**
     * Добавить новую заметку
     */
    suspend fun insertNote(note: Note)
    
    /**
     * Обновить существующую заметку
     */
    suspend fun updateNote(note: Note)
    
    /**
     * Удалить заметку
     */
    suspend fun deleteNote(note: Note)
    
    /**
     * Удалить заметку по ID
     */
    suspend fun deleteNoteById(id: String)
    
    /**
     * Получить количество всех заметок
     */
    suspend fun getNotesCount(): Int
    
    /**
     * Получить количество заметок за дату
     */
    suspend fun getNotesCountByDate(date: String): Int
}
