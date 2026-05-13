package com.emonotion.app.domain.usecase.custommood

import com.emonotion.app.domain.model.CustomMood
import com.emonotion.app.domain.repository.CustomMoodRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use Case для получения пользовательских настроений
 */
class GetCustomMoodsUseCase @Inject constructor(
    private val customMoodRepository: CustomMoodRepository
) {
    fun getActiveCustomMoods(): Flow<List<CustomMood>> {
        return customMoodRepository.getAllActiveCustomMoods()
    }
    
    fun getAllCustomMoods(): Flow<List<CustomMood>> {
        return customMoodRepository.getAllCustomMoods()
    }
    
    suspend fun getCustomMoodById(id: String): CustomMood? {
        return customMoodRepository.getCustomMoodById(id)
    }
    
    suspend fun getCustomMoodByName(name: String): CustomMood? {
        return customMoodRepository.getCustomMoodByName(name)
    }
}
