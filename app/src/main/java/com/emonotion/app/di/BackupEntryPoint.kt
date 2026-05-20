package com.emonotion.app.di

import com.emonotion.app.domain.repository.AppBackupRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface BackupEntryPoint {
    fun appBackupRepository(): AppBackupRepository
}
