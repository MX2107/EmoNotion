package com.emonotion.app.domain.usecase.settings

import com.emonotion.app.domain.model.AppSettings
import com.emonotion.app.domain.model.ThemeMode
import com.emonotion.app.domain.repository.SettingsRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify

class UpdateSettingsUseCaseTest {

    @Mock
    private lateinit var settingsRepository: SettingsRepository

    private lateinit var updateSettingsUseCase: UpdateSettingsUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        updateSettingsUseCase = UpdateSettingsUseCase(settingsRepository)
    }

    @Test
    fun `invoke should update settings successfully`() = runTest {
        // Arrange
        val settings = AppSettings(
            theme = ThemeMode.DARK,
            notificationsEnabled = true,
            autoBackupEnabled = false,
            reminderTime = "09:00",
            language = "ru",
            moodReminderEnabled = true
        )

        // Act
        val result = updateSettingsUseCase(settings)

        // Assert
        assertTrue(result.isSuccess)
        verify(settingsRepository).updateSettings(settings)
    }

    @Test
    fun `invoke should return failure when repository throws exception`() = runTest {
        // Arrange
        val settings = AppSettings(
            theme = ThemeMode.LIGHT,
            notificationsEnabled = false,
            autoBackupEnabled = true,
            reminderTime = "10:00",
            language = "en",
            moodReminderEnabled = false
        )
        val exception = RuntimeException("Database error")
        
        org.mockito.kotlin.whenever(settingsRepository.updateSettings(settings)).thenThrow(exception)

        // Act
        val result = updateSettingsUseCase(settings)

        // Assert
        assertTrue(result.isFailure)
    }
}
