package com.emonotion.app.data.repository

import com.emonotion.app.data.local.dao.MoodDao
import com.emonotion.app.data.local.entities.MoodEntryEntity
import com.emonotion.app.domain.model.MoodEntry
import com.emonotion.app.domain.repository.MoodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Реализация Repository для работы с записями о настроении
 */
@Singleton
class MoodRepositoryImpl @Inject constructor(
    private val moodDao: MoodDao
) : MoodRepository {
    
    override fun getAllMoods(): Flow<List<MoodEntry>> {
        return moodDao.getAllMoods().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getMoodByDate(date: String): Flow<MoodEntry?> {
        return moodDao.getMoodByDateFlow(date).map { entity ->
            entity?.toDomain()
        }
    }
    
    override suspend fun getMoodsByDateRange(from: Long, to: Long): Flow<List<MoodEntry>> {
        return moodDao.getMoodsByDateRange(from, to).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun getMoodsByDateRange(startDate: String, endDate: String): Flow<List<MoodEntry>> {
        return moodDao.getMoodsByDateRange(startDate, endDate).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun insertMood(mood: MoodEntry) {
        moodDao.insertMood(MoodEntryEntity.fromDomain(mood))
    }
    
    override suspend fun updateMood(mood: MoodEntry) {
        moodDao.updateMood(MoodEntryEntity.fromDomain(mood))
    }
    
    override suspend fun deleteMood(mood: MoodEntry) {
        moodDao.deleteMood(MoodEntryEntity.fromDomain(mood))
    }
    
    override suspend fun deleteMoodById(id: String) {
        moodDao.deleteMoodById(id)
    }
    
    override suspend fun hasMoodForDate(date: String): Boolean {
        return moodDao.hasMoodForDate(date)
    }
    
    override suspend fun getMoodsCount(): Int {
        return moodDao.getMoodsCount()
    }
}
