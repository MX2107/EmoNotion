package com.emonotion.app.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.emonotion.app.data.local.dao.*
import com.emonotion.app.data.local.entities.*

/**
 * Главная база данных приложения Room
 */
@Database(
    entities = [
        MoodEntryEntity::class,
        NoteEntity::class,
        TaskEntity::class,
        UserProfileEntity::class,
        AppSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun moodDao(): MoodDao
    abstract fun noteDao(): NoteDao
    abstract fun taskDao(): TaskDao
    abstract fun userDao(): UserDao
    abstract fun settingsDao(): SettingsDao
    
    companion object {
        const val DATABASE_NAME = "emonotion_database"
        
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                .addMigrations(*getAllMigrations())
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
        
        private fun getAllMigrations(): Array<Migration> {
            return arrayOf(
                // Здесь будут миграции в будущем
                // MigrationFrom1To2, MigrationFrom2To3 и т.д.
            )
        }
    }
}
