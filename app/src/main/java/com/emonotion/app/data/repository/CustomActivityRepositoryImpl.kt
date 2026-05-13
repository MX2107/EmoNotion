package com.emonotion.app.data.repository

import com.emonotion.app.data.local.dao.CustomActivityDao
import com.emonotion.app.data.local.entities.CustomActivityEntity
import com.emonotion.app.domain.model.CustomActivity
import com.emonotion.app.domain.repository.CustomActivityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Реализация Repository для работы с пользовательскими занятиями/активностями
 */
@Singleton
class CustomActivityRepositoryImpl @Inject constructor(
    private val customActivityDao: CustomActivityDao
) : CustomActivityRepository {
    
    override fun getAllActiveCustomActivities(): Flow<List<CustomActivity>> {
        return customActivityDao.getAllActiveCustomActivities().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getAllCustomActivities(): Flow<List<CustomActivity>> {
        return customActivityDao.getAllCustomActivities().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun getCustomActivityById(id: String): CustomActivity? {
        return customActivityDao.getCustomActivityById(id)?.toDomain()
    }
    
    override suspend fun getCustomActivityByName(name: String): CustomActivity? {
        return customActivityDao.getCustomActivityByName(name)?.toDomain()
    }
    
    override fun getCustomActivitiesByCategory(category: String): Flow<List<CustomActivity>> {
        return customActivityDao.getCustomActivitiesByCategory(category).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getAllCategories(): Flow<List<String>> {
        return customActivityDao.getAllCategories()
    }
    
    override suspend fun addCustomActivity(customActivity: CustomActivity): Result<Unit> {
        return try {
            val activityWithId = customActivity.copy(
                id = customActivity.id.ifEmpty { UUID.randomUUID().toString() },
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            customActivityDao.insertCustomActivity(CustomActivityEntity.fromDomain(activityWithId))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateCustomActivity(customActivity: CustomActivity): Result<Unit> {
        return try {
            val updatedActivity = customActivity.copy(updatedAt = System.currentTimeMillis())
            customActivityDao.updateCustomActivity(CustomActivityEntity.fromDomain(updatedActivity))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteCustomActivity(customActivity: CustomActivity): Result<Unit> {
        return try {
            customActivityDao.deleteCustomActivity(CustomActivityEntity.fromDomain(customActivity))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deactivateCustomActivity(id: String): Result<Unit> {
        return try {
            customActivityDao.deactivateCustomActivity(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun activateCustomActivity(id: String): Result<Unit> {
        return try {
            customActivityDao.activateCustomActivity(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteCustomActivityById(id: String): Result<Unit> {
        return try {
            customActivityDao.deleteCustomActivityById(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
