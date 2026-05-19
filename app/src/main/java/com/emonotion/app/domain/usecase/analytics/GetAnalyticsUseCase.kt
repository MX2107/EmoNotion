package com.emonotion.app.domain.usecase.analytics

import com.emonotion.app.domain.model.Analytics
import com.emonotion.app.domain.model.AnalyticsPeriod
import com.emonotion.app.domain.repository.AnalyticsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use Case для получения аналитики
 */
class GetAnalyticsUseCase @Inject constructor(
    private val analyticsRepository: AnalyticsRepository
) {
    suspend operator fun invoke(period: AnalyticsPeriod = AnalyticsPeriod.WEEK): Analytics {
        return analyticsRepository.getAnalytics(period)
    }
}
