package com.emonotion.app.domain.usecase.customactivity

import com.emonotion.app.domain.model.CustomActivity
import com.emonotion.app.domain.repository.CustomActivityRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use Case для получения пользовательских занятий/активностей
 */
class GetCustomActivitiesUseCase @Inject constructor(
    private val customActivityRepository: CustomActivityRepository
) {
    fun getActiveCustomActivities(): Flow<List<CustomActivity>> {
        return customActivityRepository.getAllActiveCustomActivities()
    }
    
    fun getAllCustomActivities(): Flow<List<CustomActivity>> {
        return customActivityRepository.getAllCustomActivities()
    }
    
    fun getCustomActivitiesByCategory(category: String): Flow<List<CustomActivity>> {
        return customActivityRepository.getCustomActivitiesByCategory(category)
    }
    
    fun getAllCategories(): Flow<List<String>> {
        return customActivityRepository.getAllCategories()
    }
    
    suspend fun getCustomActivityById(id: String): CustomActivity? {
        return customActivityRepository.getCustomActivityById(id)
    }
    
    suspend fun getCustomActivityByName(name: String): CustomActivity? {
        return customActivityRepository.getCustomActivityByName(name)
    }
}
