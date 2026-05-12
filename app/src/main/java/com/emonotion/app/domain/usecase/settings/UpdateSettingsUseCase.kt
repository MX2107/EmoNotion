package com.emonotion.app.domain.usecase.settings

import com.emonotion.app.domain.model.AppSettings
import com.emonotion.app.domain.repository.SettingsRepository
import javax.inject.Inject

/**
 * Use Case для обновления настроек приложения
 */
class UpdateSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(settings: AppSettings): Result<Unit> {
        return try {
            settingsRepository.updateSettings(settings)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
