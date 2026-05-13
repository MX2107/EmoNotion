package com.emonotion.app.domain.repository

import com.emonotion.app.domain.model.Analytics
import com.emonotion.app.domain.model.UserStats
import kotlinx.coroutines.flow.Flow

/**
 * Repository для работы с аналитикой
 */
interface AnalyticsRepository {
    suspend fun getAnalytics(): Analytics
    fun getAnalyticsFlow(): Flow<Analytics>
    fun getUserStats(): Flow<UserStats>
}
