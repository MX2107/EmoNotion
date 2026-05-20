package com.emonotion.app.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.emonotion.app.domain.model.ThemeMode

/**
 * Сохранение и применение темы (LIGHT / DARK / SYSTEM).
 */
object ThemeManager {

    private const val PREFS = "emo_app_prefs"
    private const val KEY_THEME = "theme_mode"

    fun persist(context: Context, mode: ThemeMode) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME, mode.name)
            .apply()
        apply(mode)
    }

    fun applyFromStorage(context: Context) {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_THEME, ThemeMode.LIGHT.name)
        val mode = runCatching { ThemeMode.valueOf(raw!!) }.getOrDefault(ThemeMode.LIGHT)
        apply(mode)
    }

    fun apply(mode: ThemeMode) {
        val nightMode = when (mode) {
            ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
            ThemeMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }
}
