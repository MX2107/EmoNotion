package com.emonotion.app.domain.repository

import com.emonotion.app.domain.model.CustomActivity
import kotlinx.coroutines.flow.Flow

/**
 * Repository для работы с пользовательскими занятиями/активностями
 */
interface CustomActivityRepository {
    
    fun getAllActiveCustomActivities(): Flow<List<CustomActivity>>
    
    fun getAllCustomActivities(): Flow<List<CustomActivity>>
    
    suspend fun getCustomActivityById(id: String): CustomActivity?
    
    suspend fun getCustomActivityByName(name: String): CustomActivity?
    
    fun getCustomActivitiesByCategory(category: String): Flow<List<CustomActivity>>
    
    fun getAllCategories(): Flow<List<String>>
    
    suspend fun addCustomActivity(customActivity: CustomActivity): Result<Unit>
    
    suspend fun updateCustomActivity(customActivity: CustomActivity): Result<Unit>
    
    suspend fun deleteCustomActivity(customActivity: CustomActivity): Result<Unit>
    
    suspend fun deactivateCustomActivity(id: String): Result<Unit>
    
    suspend fun activateCustomActivity(id: String): Result<Unit>
    
    suspend fun deleteCustomActivityById(id: String): Result<Unit>
}
