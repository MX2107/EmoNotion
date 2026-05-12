package com.emonotion.app.domain.usecase.settings

import com.emonotion.app.domain.model.AppSettings
import com.emonotion.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use Case для получения настроек приложения
 */
class GetSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(): Flow<AppSettings?> {
        return settingsRepository.getSettingsFlow()
    }
}
