package com.emonotion.app.domain.usecase.note

import com.emonotion.app.domain.model.Note
import com.emonotion.app.domain.repository.NoteRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import java.util.Date

class AddNoteUseCaseTest {

    @Mock
    private lateinit var noteRepository: NoteRepository

    private lateinit var addNoteUseCase: AddNoteUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        addNoteUseCase = AddNoteUseCase(noteRepository)
    }

    @Test
    fun `invoke should insert note successfully`() = runTest {
        // Arrange
        val note = Note(
            id = "test-note-id",
            title = "Test Note",
            content = "Test content",
            timestamp = Date().time,
            isPinned = false,
            tags = emptyList()
        )

        // Act
        val result = addNoteUseCase(note)

        // Assert
        assertTrue(result.isSuccess)
        verify(noteRepository).insertNote(note)
    }

    @Test
    fun `invoke should return failure when repository throws exception`() = runTest {
        // Arrange
        val note = Note(
            id = "test-note-id",
            title = "Test Note",
            content = "Test content",
            timestamp = Date().time,
            isPinned = false,
            tags = emptyList()
        )
        val exception = RuntimeException("Database error")
        
        org.mockito.kotlin.whenever(noteRepository.insertNote(note)).thenThrow(exception)

        // Act
        val result = addNoteUseCase(note)

        // Assert
        assertTrue(result.isFailure)
    }
}
