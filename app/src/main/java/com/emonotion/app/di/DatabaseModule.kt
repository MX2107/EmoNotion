package com.emonotion.app.di

import android.content.Context
import androidx.room.Room
import com.emonotion.app.data.local.database.AppDatabase
import com.emonotion.app.data.local.database.Converters
import com.emonotion.app.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt модуль для предоставления зависимостей базы данных
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
        .addMigrations(*AppDatabase.getAllMigrations())
        .fallbackToDestructiveMigration()
        .build()
    }
    
    @Provides
    fun provideMoodDao(database: AppDatabase): MoodDao {
        return database.moodDao()
    }
    
    @Provides
    fun provideNoteDao(database: AppDatabase): NoteDao {
        return database.noteDao()
    }
    
    @Provides
    fun provideTaskDao(database: AppDatabase): TaskDao {
        return database.taskDao()
    }
    
    @Provides
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }
    
    @Provides
    fun provideSettingsDao(database: AppDatabase): SettingsDao {
        return database.settingsDao()
    }
    
    @Provides
    fun provideCustomMoodDao(database: AppDatabase): CustomMoodDao {
        return database.customMoodDao()
    }
    
    @Provides
    fun provideCustomActivityDao(database: AppDatabase): CustomActivityDao {
        return database.customActivityDao()
    }
    
    @Provides
    fun provideCustomTagDao(database: AppDatabase): CustomTagDao {
        return database.customTagDao()
    }
    
    @Provides
    fun provideDraftDao(database: AppDatabase): DraftDao {
        return database.draftDao()
    }
    
    @Provides
    @Singleton
    fun provideConverters(): Converters {
        return Converters()
    }
}
