package com.emonotion.app.domain.repository

import com.emonotion.app.domain.model.MoodEntry
import kotlinx.coroutines.flow.Flow

/**
 * Repository интерфейс для работы с записями о настроении
 */
interface MoodRepository {
    
    /**
     * Получить все записи о настроении
     */
    fun getAllMoods(): Flow<List<MoodEntry>>
    
    /**
     * Получить запись о настроении за конкретную дату
     */
    suspend fun getMoodByDate(date: String): MoodEntry?
    
    /**
     * Получить записи о настроении за период времени
     */
    suspend fun getMoodsByDateRange(from: Long, to: Long): Flow<List<MoodEntry>>
    
    /**
     * Получить записи о настроении за диапазон дат
     */
    suspend fun getMoodsByDateRange(startDate: String, endDate: String): Flow<List<MoodEntry>>
    
    /**
     * Добавить новую запись о настроении
     */
    suspend fun insertMood(mood: MoodEntry)
    
    /**
     * Обновить существующую запись о настроении
     */
    suspend fun updateMood(mood: MoodEntry)
    
    /**
     * Удалить запись о настроении
     */
    suspend fun deleteMood(mood: MoodEntry)
    
    /**
     * Удалить запись о настроении по ID
     */
    suspend fun deleteMoodById(id: String)
    
    /**
     * Проверить, есть ли запись за конкретную дату
     */
    suspend fun hasMoodForDate(date: String): Boolean
    
    /**
     * Получить количество всех записей
     */
    suspend fun getMoodsCount(): Int
}
