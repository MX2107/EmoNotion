package com.emonotion.app.domain.repository

/**
 * Отображаемый путь к данным и сохранение пользовательского каталога (SAF).
 */
interface DataLocationRepository {
    fun getDisplayPath(): String
    fun getPreferredStorageUri(): String?
    fun setPreferredStorageUri(uri: String?)
}
