package com.emonotion.app.domain.usecase.analytics

import com.emonotion.app.domain.model.Analytics
import com.emonotion.app.domain.repository.AnalyticsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use Case для получения аналитики
 */
class GetAnalyticsUseCase @Inject constructor(
    private val analyticsRepository: AnalyticsRepository
) {
    suspend operator fun invoke(): Analytics {
        return analyticsRepository.getAnalytics()
    }
}
