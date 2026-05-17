package com.emonotion.app.domain.repository

import com.emonotion.app.domain.model.CustomTag
import kotlinx.coroutines.flow.Flow

/**
 * Repository для работы с пользовательскими тегами
 */
interface CustomTagRepository {
    
    fun getAllActiveCustomTags(): Flow<List<CustomTag>>
    
    fun getAllCustomTags(): Flow<List<CustomTag>>
    
    suspend fun getCustomTagById(id: String): CustomTag?
    
    suspend fun getCustomTagByName(name: String): CustomTag?
    
    suspend fun addCustomTag(customTag: CustomTag): Result<Unit>
    
    suspend fun updateCustomTag(customTag: CustomTag): Result<Unit>
    
    suspend fun deleteCustomTag(customTag: CustomTag): Result<Unit>
    
    suspend fun deactivateCustomTag(id: String): Result<Unit>
    
    suspend fun activateCustomTag(id: String): Result<Unit>
    
    suspend fun deleteCustomTagById(id: String): Result<Unit>
}
