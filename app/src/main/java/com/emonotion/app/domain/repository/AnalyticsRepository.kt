package com.emonotion.app.domain.repository

import android.net.Uri
import com.emonotion.app.domain.model.Analytics
import com.emonotion.app.domain.model.AnalyticsExportFormat
import com.emonotion.app.domain.model.AnalyticsPeriod
import com.emonotion.app.domain.model.UserStats
import kotlinx.coroutines.flow.Flow

/**
 * Repository для работы с аналитикой
 */
interface AnalyticsRepository {
    suspend fun getAnalytics(period: AnalyticsPeriod = AnalyticsPeriod.WEEK): Analytics
    fun getAnalyticsFlow(): Flow<Analytics>
    fun getUserStats(): Flow<UserStats>

    /**
     * Экспорт отчёта в Downloads/EmoNotion/Analytics/
     */
    suspend fun exportAnalytics(
        period: AnalyticsPeriod,
        format: AnalyticsExportFormat
    ): Result<Uri>
}
