package com.emonotion.app.presentation.notes

import androidx.lifecycle.viewModelScope
import com.emonotion.app.domain.model.Note
import com.emonotion.app.domain.usecase.note.AddNoteUseCase
import com.emonotion.app.domain.usecase.note.DeleteNoteUseCase
import com.emonotion.app.domain.usecase.note.GetNotesUseCase
import com.emonotion.app.domain.usecase.note.UpdateNoteUseCase
import com.emonotion.app.presentation.common.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val updateNoteUseCase: UpdateNoteUseCase
) : BaseViewModel() {

    private val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    // Состояния UI
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    private val _showAllNotes = MutableStateFlow(true)
    val showAllNotes: StateFlow<Boolean> = _showAllNotes.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOrder = MutableStateFlow<NoteSortOrder>(NoteSortOrder.NEWEST_FIRST)
    val sortOrder: StateFlow<NoteSortOrder> = _sortOrder.asStateFlow()

    private val _filterTag = MutableStateFlow<String?>(null)
    val filterTag: StateFlow<String?> = _filterTag.asStateFlow()

    private var notesCollectionJob: kotlinx.coroutines.Job? = null

    enum class NoteSortOrder {
        NEWEST_FIRST, OLDEST_FIRST, ALPHABETICAL_ASC, ALPHABETICAL_DESC
    }

    init {
        // Предварительная загрузка заметок при создании ViewModel
        loadNotes()
    }

    /**
     * Загружает заметки
     */
    fun loadNotes() {
        // Отменяем предыдущую подписку если есть
        notesCollectionJob?.cancel()

        notesCollectionJob = viewModelScope.launch {
            if (_searchQuery.value.isNotEmpty()) {
                // Поиск заметок
                getNotesUseCase.searchNotes(_searchQuery.value).collect { notesList ->
                    _notes.value = filterAndSortNotes(notesList)
                }
            } else if (_showAllNotes.value) {
                // Все заметки - сначала загружаем из базы для кэша
                val cachedNotes = getNotesUseCase().first()
                _notes.value = filterAndSortNotes(cachedNotes)
                // Затем подписываемся на обновления
                getNotesUseCase().collect { notesList ->
                    _notes.value = filterAndSortNotes(notesList)
                }
            } else {
                // Заметки за сегодня - сначала загружаем из базы для кэша
                val cachedNotes = getNotesUseCase.getNotesByDate(today).first()
                _notes.value = filterAndSortNotes(cachedNotes)
                // Затем подписываемся на обновления
                getNotesUseCase.getNotesByDate(today).collect { notesList ->
                    _notes.value = filterAndSortNotes(notesList)
                }
            }
        }
    }
    
    /**
     * Фильтрует и сортирует заметки
     */
    private fun filterAndSortNotes(notes: List<Note>): List<Note> {
        var result = notes
        
        // Фильтрация по тегу
        if (_filterTag.value != null) {
            result = result.filter { it.tags.contains(_filterTag.value) }
        }
        
        // Сортировка
        result = sortNotes(result)
        
        return result
    }
    
    /**
     * Сортирует заметки
     */
    private fun sortNotes(notes: List<Note>): List<Note> {
        return when (_sortOrder.value) {
            NoteSortOrder.NEWEST_FIRST -> notes.sortedByDescending { it.timestamp }
            NoteSortOrder.OLDEST_FIRST -> notes.sortedBy { it.timestamp }
            NoteSortOrder.ALPHABETICAL_ASC -> notes.sortedBy { it.title.lowercase() }
            NoteSortOrder.ALPHABETICAL_DESC -> notes.sortedByDescending { it.title.lowercase() }
        }
    }
    
    /**
     * Устанавливает порядок сортировки заметок
     */
    fun setSortOrder(order: NoteSortOrder) {
        _sortOrder.value = order
        loadNotes()
    }
    
    /**
     * Устанавливает фильтр по тегу
     */
    fun setFilterTag(tag: String?) {
        _filterTag.value = tag
        loadNotes()
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
    fun addNote(title: String, content: String, tags: List<String> = emptyList()) {
        executeWithResult(
            operation = {
                val newNote = Note(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    content = content,
                    timestamp = System.currentTimeMillis(),
                    date = today,
                    tags = tags,
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
     * Обновляет заметку
     */
    fun updateNote(note: Note) {
        executeWithResult(
            operation = {
                updateNoteUseCase(note)
            },
            onSuccess = {
                loadNotes()
            }
        )
    }
    
    /**
     * Обновляет данные
     */
    fun refreshNotes() {
        loadNotes()
    }
}
