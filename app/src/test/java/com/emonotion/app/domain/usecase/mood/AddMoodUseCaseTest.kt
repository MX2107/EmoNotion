package com.emonotion.app.domain.usecase.mood

import com.emonotion.app.domain.model.MoodEntry
import com.emonotion.app.domain.repository.MoodRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

class AddMoodUseCaseTest {

    @Mock
    private lateinit var moodRepository: MoodRepository

    private lateinit var addMoodUseCase: AddMoodUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        addMoodUseCase = AddMoodUseCase(moodRepository)
    }

    @Test
    fun `invoke should insert mood successfully`() = runTest {
        // Arrange
        val mood = MoodEntry(
            id = "test-id",
            mood = "good",
            intensity = 3,
            activities = listOf("work", "exercise"),
            notes = "Test note",
            timestamp = Date().time,
            date = "2024-01-01"
        )

        // Act
        val result = addMoodUseCase(mood)

        // Assert
        assertTrue(result.isSuccess)
        verify(moodRepository).insertMood(mood)
    }

    @Test
    fun `invoke should return failure when repository throws exception`() = runTest {
        // Arrange
        val mood = MoodEntry(
            id = "test-id",
            mood = "good",
            intensity = 3,
            activities = emptyList(),
            notes = null,
            timestamp = Date().time,
            date = "2024-01-01"
        )
        val exception = RuntimeException("Database error")
        
        whenever(moodRepository.insertMood(mood)).thenThrow(exception)

        // Act
        val result = addMoodUseCase(mood)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Database error", result.exceptionOrNull()?.message)
    }
}
