package com.emonotion.app.data.local.database

import android.content.Context
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Callback для инициализации базы данных
 * Отключено создание тестовых данных
 */
class AppDatabaseCallback(
    private val context: Context
) : RoomDatabase.Callback() {
    
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        // Тестовые данные больше не создаются
    }
}
