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
        AppSettingsEntity::class,
        CustomMoodEntity::class,
        CustomActivityEntity::class,
        CustomTagEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun moodDao(): MoodDao
    abstract fun noteDao(): NoteDao
    abstract fun taskDao(): TaskDao
    abstract fun userDao(): UserDao
    abstract fun settingsDao(): SettingsDao
    abstract fun customMoodDao(): CustomMoodDao
    abstract fun customActivityDao(): CustomActivityDao
    abstract fun customTagDao(): CustomTagDao
    
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
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        android.util.Log.d("Database", "Database created with version 5")
                    }
                    
                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        android.util.Log.d("Database", "Database opened, version: ${db.version}")
                        // Проверяем наличие колонки bio
                        try {
                            val cursor = db.query("SELECT sql FROM sqlite_master WHERE type='table' AND name='user_profile'")
                            if (cursor.moveToFirst()) {
                                val sql = cursor.getString(0)
                                android.util.Log.d("Database", "user_profile table schema: $sql")
                            }
                            cursor.close()
                        } catch (e: Exception) {
                            android.util.Log.e("Database", "Failed to check schema: ${e.message}")
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
        
        fun getAllMigrations(): Array<Migration> {
            return arrayOf(
                // Миграция с версии 1 на 2 - добавляем таблицу пользовательских настроений
                object : Migration(1, 2) {
                    override fun migrate(db: SupportSQLiteDatabase) {
                        // Создаем таблицу для пользовательских настроений
                        db.execSQL(
                            """
                            CREATE TABLE IF NOT EXISTS custom_moods (
                                id TEXT NOT NULL PRIMARY KEY,
                                name TEXT NOT NULL,
                                color TEXT,
                                icon TEXT,
                                isActive INTEGER NOT NULL DEFAULT 1,
                                createdAt INTEGER NOT NULL,
                                updatedAt INTEGER NOT NULL
                            )
                            """.trimIndent()
                        )
                    }
                },
                // Миграция с версии 2 на 3 - добавляем поле emotions и таблицу custom_activities
                object : Migration(2, 3) {
                    override fun migrate(db: SupportSQLiteDatabase) {
                        // Добавляем поле emotions в таблицу mood_entries
                        db.execSQL(
                            "ALTER TABLE mood_entries ADD COLUMN emotions TEXT NOT NULL DEFAULT ''"
                        )
                        
                        // Создаем таблицу для пользовательских активностей
                        db.execSQL(
                            """
                            CREATE TABLE IF NOT EXISTS custom_activities (
                                id TEXT NOT NULL PRIMARY KEY,
                                name TEXT NOT NULL,
                                category TEXT,
                                color TEXT,
                                icon TEXT,
                                isActive INTEGER NOT NULL DEFAULT 1,
                                createdAt INTEGER NOT NULL,
                                updatedAt INTEGER NOT NULL
                            )
                            """.trimIndent()
                        )
                    }
                },
                // Миграция с версии 3 на 4 - добавляем таблицу custom_tags
                object : Migration(3, 4) {
                    override fun migrate(db: SupportSQLiteDatabase) {
                        // Создаем таблицу для пользовательских тегов
                        db.execSQL(
                            """
                            CREATE TABLE IF NOT EXISTS custom_tags (
                                id TEXT NOT NULL PRIMARY KEY,
                                name TEXT NOT NULL,
                                color TEXT,
                                isActive INTEGER NOT NULL DEFAULT 1,
                                createdAt INTEGER NOT NULL,
                                updatedAt INTEGER NOT NULL
                            )
                            """.trimIndent()
                        )
                    }
                },
                // Миграция с версии 4 на 5 - добавляем поле bio в таблицу user_profile
                object : Migration(4, 5) {
                    override fun migrate(db: SupportSQLiteDatabase) {
                        // Проверяем, существует ли колонка bio
                        try {
                            db.execSQL("ALTER TABLE user_profile ADD COLUMN bio TEXT")
                            android.util.Log.d("DatabaseMigration", "Added bio column to user_profile")
                        } catch (e: Exception) {
                            android.util.Log.w("DatabaseMigration", "bio column may already exist: ${e.message}")
                        }
                    }
                }
            )
        }
    }
}
