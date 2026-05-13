package com.emonotion.app.domain.repository

import com.emonotion.app.domain.model.CustomMood
import kotlinx.coroutines.flow.Flow

/**
 * Repository для работы с пользовательскими настроениями
 */
interface CustomMoodRepository {
    
    fun getAllActiveCustomMoods(): Flow<List<CustomMood>>
    
    fun getAllCustomMoods(): Flow<List<CustomMood>>
    
    suspend fun getCustomMoodById(id: String): CustomMood?
    
    suspend fun getCustomMoodByName(name: String): CustomMood?
    
    suspend fun addCustomMood(customMood: CustomMood): Result<Unit>
    
    suspend fun updateCustomMood(customMood: CustomMood): Result<Unit>
    
    suspend fun deleteCustomMood(customMood: CustomMood): Result<Unit>
    
    suspend fun deactivateCustomMood(id: String): Result<Unit>
    
    suspend fun activateCustomMood(id: String): Result<Unit>
    
    suspend fun deleteCustomMoodById(id: String): Result<Unit>
}
