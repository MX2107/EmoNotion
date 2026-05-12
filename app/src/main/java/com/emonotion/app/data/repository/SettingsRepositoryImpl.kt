package com.emonotion.app.data.repository

import com.emonotion.app.data.local.dao.SettingsDao
import com.emonotion.app.data.local.entities.AppSettingsEntity
import com.emonotion.app.domain.model.AppSettings
import com.emonotion.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Реализация Repository для работы с настройками приложения
 */
@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val settingsDao: SettingsDao
) : SettingsRepository {
    
    override suspend fun getSettings(): AppSettings? {
        return settingsDao.getSetting()?.toDomain()
    }
    
    override suspend fun getSettingsFlow(): Flow<AppSettings?> {
        return settingsDao.getSettingFlow().map { entity ->
            entity?.toDomain()
        }
    }
    
    override suspend fun updateSettings(settings: AppSettings) {
        settingsDao.updateSetting(AppSettingsEntity.fromDomain(settings))
    }
    
    override suspend fun deleteSettings() {
        settingsDao.deleteSetting()
    }
}
