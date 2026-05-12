package com.emonotion.app.presentation.notes

import androidx.lifecycle.viewModelScope
import com.emonotion.app.domain.model.Note
import com.emonotion.app.domain.usecase.note.AddNoteUseCase
import com.emonotion.app.domain.usecase.note.DeleteNoteUseCase
import com.emonotion.app.domain.usecase.note.GetNotesUseCase
import com.emonotion.app.presentation.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * ViewModel для экрана заметок
 */
@HiltViewModel
class NotesViewModel @Inject constructor(
    private val getNotesUseCase: GetNotesUseCase,
    private val addNoteUseCase: AddNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase
) : BaseViewModel() {
    
    private val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    
    // Состояния UI
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()
    
    private val _showAllNotes = MutableStateFlow(false)
    val showAllNotes: StateFlow<Boolean> = _showAllNotes.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    /**
     * Загружает заметки
     */
    fun loadNotes() {
        executeWithLoading {
            viewModelScope.launch {
                if (_searchQuery.value.isNotEmpty()) {
                    // Поиск заметок
                    getNotesUseCase.searchNotes(_searchQuery.value).collect { notesList ->
                        _notes.value = notesList
                    }
                } else if (_showAllNotes.value) {
                    // Все заметки
                    getNotesUseCase().collect { notesList ->
                        _notes.value = notesList
                    }
                } else {
                    // Заметки за сегодня
                    getNotesUseCase.getNotesByDate(today).collect { notesList ->
                        _notes.value = notesList
                    }
                }
            }
        }
    }
    
    /**
     * Переключает между всеми заметками и заметками за сегодня
     */
    fun toggleShowAllNotes() {
        _showAllNotes.value = !_showAllNotes.value
        loadNotes()
    }
    
    /**
     * Выполняет поиск заметок
     */
    fun searchNotes(query: String) {
        _searchQuery.value = query
        loadNotes()
    }
    
    /**
     * Добавляет новую заметку
     */
    fun addNote(title: String, content: String) {
        executeWithResult(
            operation = {
                val newNote = Note(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    content = content,
                    timestamp = System.currentTimeMillis(),
                    date = today,
                    tags = emptyList(),
                    isPinned = false
                )
                addNoteUseCase(newNote)
            },
            onSuccess = {
                loadNotes() // Обновляем список после добавления
            }
        )
    }
    
    /**
     * Удаляет заметку
     */
    fun deleteNote(noteId: String) {
        executeWithResult(
            operation = {
                deleteNoteUseCase(noteId)
            },
            onSuccess = {
                loadNotes() // Обновляем список после удаления
            }
        )
    }
    
    /**
     * Удаляет заметку
     */
    fun deleteNote(note: Note) {
        deleteNote(note.id)
    }
    
    /**
     * Обновляет данные
     */
    fun refreshNotes() {
        loadNotes()
    }
}
