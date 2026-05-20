package com.emonotion.app.domain.repository

/**
 * Синхронизация с почтой (требует OAuth в продакшене).
 */
interface GmailSyncRepository {
    suspend fun syncNow(): Result<Unit>
    suspend fun setAutoSyncEnabled(enabled: Boolean, frequencyOrdinal: Int): Result<Unit>

    /** Текст для UI: когда была последняя успешная синхронизация. */
    fun getLastSyncDisplay(): String

    fun isAutoSyncEnabled(): Boolean
    fun getSyncFrequencyOrdinal(): Int
}
