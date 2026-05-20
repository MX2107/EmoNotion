package com.emonotion.app.data.repository

import android.content.Context
import com.emonotion.app.R
import com.emonotion.app.domain.repository.GmailSyncRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Локальная заглушка: фиксирует время «синхронизации» без Gmail API.
 * Полноценная интеграция требует OAuth и регистрации приложения в Google Cloud.
 */
@Singleton
class GmailSyncRepositoryStub @Inject constructor(
    @ApplicationContext private val context: Context
) : GmailSyncRepository {

    private val prefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val formatter =
        SimpleDateFormat("d MMM yyyy, HH:mm", Locale.forLanguageTag("ru"))

    override suspend fun syncNow(): Result<Unit> = withContext(Dispatchers.IO) {
        prefs.edit().putLong(KEY_LAST_SYNC_MS, System.currentTimeMillis()).apply()
        Result.success(Unit)
    }

    override suspend fun setAutoSyncEnabled(enabled: Boolean, frequencyOrdinal: Int): Result<Unit> =
        withContext(Dispatchers.IO) {
            prefs.edit()
                .putBoolean(KEY_AUTO_ENABLED, enabled)
                .putInt(KEY_FREQ, frequencyOrdinal.coerceIn(0, 2))
                .apply()
            Result.success(Unit)
        }

    override fun getLastSyncDisplay(): String {
        val ms = prefs.getLong(KEY_LAST_SYNC_MS, 0L)
        if (ms == 0L) return context.getString(R.string.never_synced)
        return formatter.format(Date(ms))
    }

    override fun isAutoSyncEnabled(): Boolean =
        prefs.getBoolean(KEY_AUTO_ENABLED, false)

    override fun getSyncFrequencyOrdinal(): Int =
        prefs.getInt(KEY_FREQ, 0)

    companion object {
        private const val PREFS_NAME = "gmail_sync_stub_prefs"
        private const val KEY_LAST_SYNC_MS = "last_sync_ms"
        private const val KEY_AUTO_ENABLED = "auto_sync_enabled"
        private const val KEY_FREQ = "sync_frequency_ordinal"
    }
}
