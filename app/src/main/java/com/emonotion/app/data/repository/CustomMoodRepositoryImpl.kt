package com.emonotion.app.data.repository

import com.emonotion.app.data.local.dao.CustomMoodDao
import com.emonotion.app.data.local.entities.CustomMoodEntity
import com.emonotion.app.domain.model.CustomMood
import com.emonotion.app.domain.repository.CustomMoodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Реализация Repository для работы с пользовательскими настроениями
 */
@Singleton
class CustomMoodRepositoryImpl @Inject constructor(
    private val customMoodDao: CustomMoodDao
) : CustomMoodRepository {
    
    override fun getAllActiveCustomMoods(): Flow<List<CustomMood>> {
        return customMoodDao.getAllActiveCustomMoods().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getAllCustomMoods(): Flow<List<CustomMood>> {
        return customMoodDao.getAllCustomMoods().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun getCustomMoodById(id: String): CustomMood? {
        return customMoodDao.getCustomMoodById(id)?.toDomain()
    }
    
    override suspend fun getCustomMoodByName(name: String): CustomMood? {
        return customMoodDao.getCustomMoodByName(name)?.toDomain()
    }
    
    override suspend fun addCustomMood(customMood: CustomMood): Result<Unit> {
        return try {
            val moodWithId = customMood.copy(
                id = customMood.id.ifEmpty { UUID.randomUUID().toString() },
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            customMoodDao.insertCustomMood(CustomMoodEntity.fromDomain(moodWithId))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateCustomMood(customMood: CustomMood): Result<Unit> {
        return try {
            val updatedMood = customMood.copy(updatedAt = System.currentTimeMillis())
            customMoodDao.updateCustomMood(CustomMoodEntity.fromDomain(updatedMood))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteCustomMood(customMood: CustomMood): Result<Unit> {
        return try {
            customMoodDao.deleteCustomMood(CustomMoodEntity.fromDomain(customMood))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deactivateCustomMood(id: String): Result<Unit> {
        return try {
            customMoodDao.deactivateCustomMood(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun activateCustomMood(id: String): Result<Unit> {
        return try {
            customMoodDao.activateCustomMood(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteCustomMoodById(id: String): Result<Unit> {
        return try {
            customMoodDao.deleteCustomMoodById(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
