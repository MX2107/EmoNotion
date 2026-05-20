package com.emonotion.app.domain.repository

import android.net.Uri

/**
 * Экспорт и импорт полного набора данных приложения (JSON).
 */
interface AppBackupRepository {
    suspend fun exportAllToJsonString(): Result<String>
    suspend fun exportAllToDownloads(): Result<Uri>
    suspend fun importAllFromJsonString(json: String, replaceExisting: Boolean): Result<Unit>
}
