package com.emonotion.app.domain.repository

import com.emonotion.app.domain.model.MoodEntry
import kotlinx.coroutines.flow.Flow

/**
 * Repository интерфейс для работы с черновиками записей о настроении
 */
interface DraftRepository {
    
    /**
     * Получает черновик для указанной даты
     */
    suspend fun getDraftByDate(date: String): MoodEntry?
    
    /**
     * Получает черновик для указанной даты как Flow
     */
    fun getDraftByDateFlow(date: String): Flow<MoodEntry?>
    
    /**
     * Сохраняет черновик
     */
    suspend fun saveDraft(moodEntry: MoodEntry)
    
    /**
     * Удаляет черновик для указанной даты
     */
    suspend fun deleteDraftByDate(date: String)
    
    /**
     * Удаляет черновик
     */
    suspend fun deleteDraft(draft: MoodEntry)
    
    /**
     * Удаляет все черновики
     */
    suspend fun deleteAllDrafts()
    
    /**
     * Получает все черновики
     */
    fun getAllDrafts(): Flow<List<MoodEntry>>
}
