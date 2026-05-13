package com.emonotion.app.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Миграции базы данных для обновления схемы
 */
object Migrations {
    
    /**
     * Миграция с версии 1 на версию 2
     * Добавляет поле emotions в таблицу mood_entries
     */
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Добавляем новую колонку emotions с значением по умолчанию ""
            db.execSQL(
                "ALTER TABLE mood_entries ADD COLUMN emotions TEXT NOT NULL DEFAULT ''"
            )
        }
    }
}
