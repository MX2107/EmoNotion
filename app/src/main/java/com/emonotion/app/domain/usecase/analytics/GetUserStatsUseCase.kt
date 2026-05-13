package com.emonotion.app.domain.usecase.analytics

import com.emonotion.app.domain.model.UserStats
import com.emonotion.app.domain.repository.AnalyticsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case для получения статистики пользователя
 */
class GetUserStatsUseCase @Inject constructor(
    private val analyticsRepository: AnalyticsRepository
) {
    operator fun invoke(): Flow<UserStats> {
        return analyticsRepository.getUserStats()
    }
}
