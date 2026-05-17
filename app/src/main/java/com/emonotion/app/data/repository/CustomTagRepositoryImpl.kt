package com.emonotion.app.data.repository

import com.emonotion.app.data.local.dao.CustomTagDao
import com.emonotion.app.data.local.entities.CustomTagEntity
import com.emonotion.app.domain.model.CustomTag
import com.emonotion.app.domain.repository.CustomTagRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Реализация Repository для работы с пользовательскими тегами
 */
@Singleton
class CustomTagRepositoryImpl @Inject constructor(
    private val customTagDao: CustomTagDao
) : CustomTagRepository {
    
    override fun getAllActiveCustomTags(): Flow<List<CustomTag>> {
        return customTagDao.getAllActiveCustomTags().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getAllCustomTags(): Flow<List<CustomTag>> {
        return customTagDao.getAllCustomTags().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun getCustomTagById(id: String): CustomTag? {
        return customTagDao.getCustomTagById(id)?.toDomain()
    }
    
    override suspend fun getCustomTagByName(name: String): CustomTag? {
        return customTagDao.getCustomTagByName(name)?.toDomain()
    }
    
    override suspend fun addCustomTag(customTag: CustomTag): Result<Unit> {
        return try {
            val tagWithId = customTag.copy(
                id = customTag.id.ifEmpty { UUID.randomUUID().toString() },
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            customTagDao.insertCustomTag(CustomTagEntity.fromDomain(tagWithId))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateCustomTag(customTag: CustomTag): Result<Unit> {
        return try {
            val updatedTag = customTag.copy(updatedAt = System.currentTimeMillis())
            customTagDao.updateCustomTag(CustomTagEntity.fromDomain(updatedTag))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteCustomTag(customTag: CustomTag): Result<Unit> {
        return try {
            customTagDao.deleteCustomTag(CustomTagEntity.fromDomain(customTag))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deactivateCustomTag(id: String): Result<Unit> {
        return try {
            customTagDao.deactivateCustomTag(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun activateCustomTag(id: String): Result<Unit> {
        return try {
            customTagDao.activateCustomTag(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteCustomTagById(id: String): Result<Unit> {
        return try {
            customTagDao.deleteCustomTagById(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
