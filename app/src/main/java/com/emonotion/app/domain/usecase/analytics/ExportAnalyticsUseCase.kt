package com.emonotion.app.domain.usecase.analytics

import android.net.Uri
import com.emonotion.app.domain.model.AnalyticsExportFormat
import com.emonotion.app.domain.model.AnalyticsPeriod
import com.emonotion.app.domain.repository.AnalyticsRepository
import javax.inject.Inject

class ExportAnalyticsUseCase @Inject constructor(
    private val analyticsRepository: AnalyticsRepository
) {
    suspend operator fun invoke(
        period: AnalyticsPeriod,
        format: AnalyticsExportFormat
    ): Result<Uri> = analyticsRepository.exportAnalytics(period, format)
}
