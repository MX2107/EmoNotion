package com.emonotion.app.data.repository

import android.content.Context
import com.emonotion.app.domain.repository.DataLocationRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataLocationRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : DataLocationRepository {

    private val prefs by lazy {
        context.getSharedPreferences("data_location_prefs", Context.MODE_PRIVATE)
    }

    override fun getDisplayPath(): String {
        val custom = prefs.getString(KEY_URI, null)
        return if (!custom.isNullOrBlank()) {
            custom
        } else {
            context.filesDir.absolutePath
        }
    }

    override fun getPreferredStorageUri(): String? =
        prefs.getString(KEY_URI, null)

    override fun setPreferredStorageUri(uri: String?) {
        prefs.edit().putString(KEY_URI, uri).apply()
    }

    companion object {
        private const val KEY_URI = "preferred_tree_uri"
    }
}
