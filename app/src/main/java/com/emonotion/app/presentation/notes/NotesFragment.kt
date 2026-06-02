package com.emonotion.app.presentation.notes

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.emonotion.app.R
import com.emonotion.app.databinding.FragmentNotesBinding
import com.emonotion.app.domain.model.CustomTag
import com.emonotion.app.domain.model.Note
import com.emonotion.app.domain.model.Task
import com.emonotion.app.domain.usecase.customtag.AddCustomTagUseCase
import com.emonotion.app.domain.usecase.customtag.GetCustomTagsUseCase
import com.emonotion.app.presentation.adapter.NotesAdapter
import com.emonotion.app.presentation.adapter.TasksAdapter
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlinx.coroutines.launch

/**
 * Экран заметок
 */
@AndroidEntryPoint
class NotesFragment : Fragment() {
    
    private var _binding: FragmentNotesBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: NotesViewModel by viewModels()
    private val tasksViewModel: TasksViewModel by viewModels()
    private lateinit var notesAdapter: NotesAdapter
    private lateinit var tasksAdapter: TasksAdapter
    
    @Inject
    lateinit var addCustomTagUseCase: AddCustomTagUseCase
    
    @Inject
    lateinit var getCustomTagsUseCase: GetCustomTagsUseCase
    
    private val selectedTags = mutableSetOf<String>()
    private val allCustomTags = mutableSetOf<String>()
    
    private lateinit var sharedPreferences: SharedPreferences
    private companion object {
        private const val PREFS_NAME = "notes_draft"
        private const val KEY_NOTE_TITLE = "draft_note_title"
        private const val KEY_NOTE_CONTENT = "draft_note_content"
        private const val KEY_NOTE_TAGS = "draft_note_tags"
        private const val KEY_TASK_TITLE = "draft_task_title"
        private const val KEY_TASK_DESCRIPTION = "draft_task_description"
        private const val KEY_NOTE_FORM_VISIBLE = "draft_note_form_visible"
        private const val KEY_TASK_FORM_VISIBLE = "draft_task_form_visible"
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotesBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        setupRecyclerView()
        observeViewModel()
        setupUI()
        // Загружаем данные только при первом создании
        if (savedInstanceState == null) {
            viewModel.loadNotes()
            tasksViewModel.loadTasks()
        }
        
        // Восстанавливаем черновики если есть
        restoreDrafts()
    }
    
    private fun loadCustomTags() {
        viewLifecycleOwner.lifecycleScope.launch {
            getCustomTagsUseCase().collect { customTags ->
                val predefinedTags = setOf(
                    getString(R.string.tag_work),
                    getString(R.string.tag_personal),
                    getString(R.string.tag_idea)
                )
                allCustomTags.clear()
                // Сначала добавляем предопределенные теги
                allCustomTags.addAll(predefinedTags)
                // Затем добавляем только пользовательские теги, которых нет в предопределенных
                customTags.map { it.name }.forEach { tagName ->
                    if (tagName !in predefinedTags) {
                        allCustomTags.add(tagName)
                    }
                }
                displayCustomTags()
            }
        }
    }
    
    private fun setupRecyclerView() {
        notesAdapter = NotesAdapter(
            onItemClick = { note ->
                showNoteDetail(note)
            },
            onItemLongClick = { note ->
                deleteNoteDirectly(note)
            }
        )
        
        binding.notesRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = notesAdapter
        }
        
        tasksAdapter = TasksAdapter(
            onItemClick = { task ->
                showTaskDetail(task)
            },
            onItemLongClick = { task ->
                deleteTaskDirectly(task)
            },
            onToggleTask = { task ->
                tasksViewModel.toggleTaskCompletion(task)
            }
        )
        
        binding.tasksRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = tasksAdapter
        }
    }
    
    private fun setupUI() {
        binding.apply {
            // Кнопка добавления заметки
            addNoteButton.setOnClickListener {
                toggleNoteForm()
            }
            
            // Кнопка добавления задачи
            addTaskButton.setOnClickListener {
                toggleTaskForm()
            }
            
            // Кнопки вкладок
            notesTab.setOnClickListener {
                switchToNotesTab()
            }
            
            tasksTab.setOnClickListener {
                switchToTasksTab()
            }
            
            // Кнопки формы заметки
            saveNoteButton.setOnClickListener {
                saveNote()
            }
            
            cancelNoteButton.setOnClickListener {
                hideNoteForm()
            }
            
            // Сортировка заметок
            sortNotesButton.setOnClickListener {
                showNotesSortDialog()
            }
            
            // Фильтрация по тегам
            filterTagText.setOnClickListener {
                showTagFilterDialog()
            }
            
            binding.clearFilterTag.setOnClickListener {
                viewModel.setFilterTag(null)
                updateFilterTagText(getString(R.string.filter_all))
                binding.clearFilterTag.visibility = View.GONE
            }

            // Кнопки формы задачи
            saveTaskButton.setOnClickListener {
                saveTask()
            }
            
            cancelTaskButton.setOnClickListener {
                hideTaskForm()
            }
            
            // Поиск
            searchInput.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val query = s?.toString() ?: ""
                    viewModel.searchNotes(query)
                    tasksViewModel.searchTasks(query)
                    
                    // Показываем/скрываем кнопку очистки
                    clearSearch.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE
                }
            })
            
            clearSearch.setOnClickListener {
                searchInput.text?.clear()
            }
            
            // Фильтры задач
            filterAll.setOnClickListener {
                updateFilterButtons(TasksViewModel.FilterStatus.ALL)
                tasksViewModel.setFilterStatus(TasksViewModel.FilterStatus.ALL)
            }
            
            filterActive.setOnClickListener {
                updateFilterButtons(TasksViewModel.FilterStatus.ACTIVE)
                tasksViewModel.setFilterStatus(TasksViewModel.FilterStatus.ACTIVE)
            }
            
            filterCompleted.setOnClickListener {
                updateFilterButtons(TasksViewModel.FilterStatus.COMPLETED)
                tasksViewModel.setFilterStatus(TasksViewModel.FilterStatus.COMPLETED)
            }
            
            // Сортировка задач
            sortButton.setOnClickListener {
                showSortDialog()
            }
        }
    }
    
    private fun updateFilterButtons(status: TasksViewModel.FilterStatus) {
        binding.apply {
            when (status) {
                TasksViewModel.FilterStatus.ALL -> {
                    filterAll.background = ContextCompat.getDrawable(requireContext(), R.drawable.tab_active_background)
                    filterAll.setTextColor(ContextCompat.getColor(requireContext(), R.color.accent_foreground))
                    filterActive.background = ContextCompat.getDrawable(requireContext(), R.drawable.tab_inactive_background)
                    filterActive.setTextColor(ContextCompat.getColor(requireContext(), R.color.muted_foreground))
                    filterCompleted.background = ContextCompat.getDrawable(requireContext(), R.drawable.tab_inactive_background)
                    filterCompleted.setTextColor(ContextCompat.getColor(requireContext(), R.color.muted_foreground))
                }
                TasksViewModel.FilterStatus.ACTIVE -> {
                    filterAll.background = ContextCompat.getDrawable(requireContext(), R.drawable.tab_inactive_background)
                    filterAll.setTextColor(ContextCompat.getColor(requireContext(), R.color.muted_foreground))
                    filterActive.background = ContextCompat.getDrawable(requireContext(), R.drawable.tab_active_background)
                    filterActive.setTextColor(ContextCompat.getColor(requireContext(), R.color.accent_foreground))
                    filterCompleted.background = ContextCompat.getDrawable(requireContext(), R.drawable.tab_inactive_background)
                    filterCompleted.setTextColor(ContextCompat.getColor(requireContext(), R.color.muted_foreground))
                }
                TasksViewModel.FilterStatus.COMPLETED -> {
                    filterAll.background = ContextCompat.getDrawable(requireContext(), R.drawable.tab_inactive_background)
                    filterAll.setTextColor(ContextCompat.getColor(requireContext(), R.color.muted_foreground))
                    filterActive.background = ContextCompat.getDrawable(requireContext(), R.drawable.tab_inactive_background)
                    filterActive.setTextColor(ContextCompat.getColor(requireContext(), R.color.muted_foreground))
                    filterCompleted.background = ContextCompat.getDrawable(requireContext(), R.drawable.tab_active_background)
                    filterCompleted.setTextColor(ContextCompat.getColor(requireContext(), R.color.accent_foreground))
                }
            }
        }
    }
    
    private fun showSortDialog() {
        val currentOrder = tasksViewModel.sortOrder.value
        val sortOptions = arrayOf(
            getString(R.string.sort_newest),
            getString(R.string.sort_oldest),
            getString(R.string.sort_alphabetical_asc),
            getString(R.string.sort_alphabetical_desc)
        )
        
        val checkedItem = when (currentOrder) {
            TasksViewModel.SortOrder.NEWEST_FIRST -> 0
            TasksViewModel.SortOrder.OLDEST_FIRST -> 1
            TasksViewModel.SortOrder.ALPHABETICAL_ASC -> 2
            TasksViewModel.SortOrder.ALPHABETICAL_DESC -> 3
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("Сортировка")
            .setSingleChoiceItems(sortOptions, checkedItem) { dialog, which ->
                when (which) {
                    0 -> {
                        tasksViewModel.setSortOrder(TasksViewModel.SortOrder.NEWEST_FIRST)
                    }
                    1 -> {
                        tasksViewModel.setSortOrder(TasksViewModel.SortOrder.OLDEST_FIRST)
                    }
                    2 -> {
                        tasksViewModel.setSortOrder(TasksViewModel.SortOrder.ALPHABETICAL_ASC)
                    }
                    3 -> {
                        tasksViewModel.setSortOrder(TasksViewModel.SortOrder.ALPHABETICAL_DESC)
                    }
                }
                dialog.dismiss()
            }
            .show()
    }
    
    private fun showNotesSortDialog() {
        val currentOrder = viewModel.sortOrder.value
        val sortOptions = arrayOf(
            getString(R.string.sort_newest),
            getString(R.string.sort_oldest),
            getString(R.string.sort_alphabetical_asc),
            getString(R.string.sort_alphabetical_desc)
        )
        
        val checkedItem = when (currentOrder) {
            NotesViewModel.NoteSortOrder.NEWEST_FIRST -> 0
            NotesViewModel.NoteSortOrder.OLDEST_FIRST -> 1
            NotesViewModel.NoteSortOrder.ALPHABETICAL_ASC -> 2
            NotesViewModel.NoteSortOrder.ALPHABETICAL_DESC -> 3
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("Сортировка заметок")
            .setSingleChoiceItems(sortOptions, checkedItem) { dialog, which ->
                when (which) {
                    0 -> {
                        viewModel.setSortOrder(NotesViewModel.NoteSortOrder.NEWEST_FIRST)
                        updateNotesSortText(getString(R.string.sort_newest))
                    }
                    1 -> {
                        viewModel.setSortOrder(NotesViewModel.NoteSortOrder.OLDEST_FIRST)
                        updateNotesSortText(getString(R.string.sort_oldest))
                    }
                    2 -> {
                        viewModel.setSortOrder(NotesViewModel.NoteSortOrder.ALPHABETICAL_ASC)
                        updateNotesSortText(getString(R.string.sort_alphabetical_asc))
                    }
                    3 -> {
                        viewModel.setSortOrder(NotesViewModel.NoteSortOrder.ALPHABETICAL_DESC)
                        updateNotesSortText(getString(R.string.sort_alphabetical_desc))
                    }
                }
                dialog.dismiss()
            }
            .show()
    }
    
    private fun updateNotesSortText(text: String) {
        binding.sortNotesText.text = text
    }
    
    private fun showTagFilterDialog() {
        val predefinedTags = setOf(
            getString(R.string.tag_work),
            getString(R.string.tag_personal),
            getString(R.string.tag_idea)
        )
        val customTags = allCustomTags.filter { it !in predefinedTags }.sorted()
        
        val tagOptions = mutableListOf<String>()
        tagOptions.add(getString(R.string.filter_all))
        tagOptions.add(getString(R.string.tag_work))
        tagOptions.add(getString(R.string.tag_personal))
        tagOptions.add(getString(R.string.tag_idea))
        tagOptions.addAll(customTags)
        
        val currentTag = viewModel.filterTag.value
        val checkedItem = when (currentTag) {
            null -> 0
            getString(R.string.tag_work) -> 1
            getString(R.string.tag_personal) -> 2
            getString(R.string.tag_idea) -> 3
            else -> {
                val index = customTags.indexOf(currentTag)
                if (index >= 0) 4 + index else 0
            }
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("Фильтр по тегам")
            .setSingleChoiceItems(tagOptions.toTypedArray(), checkedItem) { dialog, which ->
                when (which) {
                    0 -> {
                        viewModel.setFilterTag(null)
                        updateFilterTagText(getString(R.string.filter_all))
                        binding.clearFilterTag.visibility = View.GONE
                    }
                    1 -> {
                        viewModel.setFilterTag(getString(R.string.tag_work))
                        updateFilterTagText(getString(R.string.tag_work))
                        binding.clearFilterTag.visibility = View.VISIBLE
                    }
                    2 -> {
                        viewModel.setFilterTag(getString(R.string.tag_personal))
                        updateFilterTagText(getString(R.string.tag_personal))
                        binding.clearFilterTag.visibility = View.VISIBLE
                    }
                    3 -> {
                        viewModel.setFilterTag(getString(R.string.tag_idea))
                        updateFilterTagText(getString(R.string.tag_idea))
                        binding.clearFilterTag.visibility = View.VISIBLE
                    }
                    else -> {
                        val customTagIndex = which - 4
                        if (customTagIndex >= 0 && customTagIndex < customTags.size) {
                            val selectedTag = customTags[customTagIndex]
                            viewModel.setFilterTag(selectedTag)
                            updateFilterTagText(selectedTag)
                            binding.clearFilterTag.visibility = View.VISIBLE
                        }
                    }
                }
                dialog.dismiss()
            }
            .show()
    }
    
    private fun updateFilterTagText(text: String) {
        binding.filterTagText.text = text
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.notes.collect { notes ->
                notesAdapter.submitList(notes)
                updateEmptyState(notes.isEmpty())
            }
        }
        
        // Сохраняем ссылку на заметки для поиска по ID
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.notes.collect { notes ->
                if (arguments?.getString("noteId") != null) {
                    val noteId = arguments?.getString("noteId")
                    val note = notes.find { it.id == noteId }
                    if (note != null) {
                        showEditNoteDialog(note)
                        arguments?.remove("noteId")
                    }
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            tasksViewModel.tasks.collect { tasks ->
                tasksAdapter.submitTasks(tasks, tasksViewModel.sortOrder.value)
                updateTasksEmptyState(tasks.isEmpty())
            }
        }
    }
    
    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.notesRecycler.visibility = View.GONE
        } else {
            binding.notesRecycler.visibility = View.VISIBLE
        }
    }
    
    private fun updateTasksEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.tasksRecycler.visibility = View.GONE
        } else {
            binding.tasksRecycler.visibility = View.VISIBLE
        }
    }
    
    private fun toggleNoteForm() {
        if (binding.addNoteForm.visibility == View.VISIBLE) {
            hideNoteForm()
        } else {
            showNoteForm()
        }
    }
    
    private fun showNoteForm() {
        binding.addNoteButton.visibility = View.GONE
        binding.addNoteForm.visibility = View.VISIBLE
        binding.noteInput.requestFocus()
        displayPredefinedTags()
        // Загружаем custom tags только при открытии формы
        loadCustomTags()
        sharedPreferences.edit().putBoolean(KEY_NOTE_FORM_VISIBLE, true).apply()
    }
    
    private fun hideNoteForm() {
        binding.addNoteButton.visibility = View.VISIBLE
        binding.addNoteForm.visibility = View.GONE
        binding.noteTitleInput.text?.clear()
        binding.noteInput.text?.clear()
        selectedTags.clear()
        // Не очищаем allCustomTags - это глобальный список тегов из базы данных
        binding.predefinedTagsContainer.removeAllViews()
        binding.customTagsContainer.removeAllViews()
        binding.customTagsContainer.visibility = View.GONE
        sharedPreferences.edit().putBoolean(KEY_NOTE_FORM_VISIBLE, false).apply()
        clearNoteDraft()
    }
    
    private fun toggleTag(tag: String) {
        if (selectedTags.contains(tag)) {
            selectedTags.remove(tag)
        } else {
            selectedTags.add(tag)
        }
        displayPredefinedTags()
        displayCustomTags()
    }

    private fun displayPredefinedTags() {
        val predefinedTags = listOf(
            getString(R.string.tag_work),
            getString(R.string.tag_personal),
            getString(R.string.tag_idea)
        )

        val predefinedContainer = binding.predefinedTagsContainer
        predefinedContainer.removeAllViews()

        predefinedTags.forEach { tag ->
            val chip = createTagChip(tag)
            predefinedContainer.addView(chip)
        }
        // Добавляем кнопку "+" для добавления пользовательских тегов
        val addChip = com.google.android.material.chip.Chip(requireContext()).apply {
            text = "+"
            isCloseIconVisible = false
            isCheckable = false
            chipBackgroundColor = ContextCompat.getColorStateList(requireContext(), R.color.muted)
            setTextColor(ContextCompat.getColor(requireContext(), R.color.foreground))
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
            chipStrokeWidth = 0f

            setOnClickListener {
                showAddCustomTagDialog()
            }
        }
        predefinedContainer.addView(addChip)
    }
    
    private fun showAddCustomTagDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_tag, null)
        
        val tagInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.tag_input)
        
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        dialogView.findViewById<Button>(R.id.cancel_button).setOnClickListener {
            dialog.dismiss()
        }
        
        dialogView.findViewById<Button>(R.id.add_button).setOnClickListener {
            val tagName = tagInput.text.toString().trim()
            if (tagName.isNotBlank()) {
                addCustomTag(tagName)
                dialog.dismiss()
            } else {
                tagInput.error = "Введите название тега"
            }
        }
        
        dialog.show()
        
        tagInput.requestFocus()
        val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.showSoftInput(tagInput, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
    }
    
    private fun addCustomTag(tagName: String) {
        selectedTags.add(tagName)
        
        // Добавляем тег в allCustomTags для немедленного отображения
        val predefinedTags = setOf(
            getString(R.string.tag_work),
            getString(R.string.tag_personal),
            getString(R.string.tag_idea)
        )
        if (tagName !in predefinedTags) {
            allCustomTags.add(tagName)
        }
        
        // Сохраняем тег в базу данных, Flow автоматически обновит allCustomTags
        lifecycleScope.launch {
            val customTag = CustomTag(
                id = "",
                name = tagName,
                color = null,
                isActive = true
            )
            addCustomTagUseCase(customTag)
        }
        
        displayCustomTags()
    }
    
    private fun displayCustomTags() {
        val predefinedTags = setOf(
            getString(R.string.tag_work),
            getString(R.string.tag_personal),
            getString(R.string.tag_idea)
        )
        val customTags = allCustomTags.filter { it !in predefinedTags }

        val customTagsContainer = binding.customTagsContainer
        customTagsContainer.removeAllViews()

        if (customTags.isNotEmpty()) {
            customTagsContainer.visibility = View.VISIBLE
            customTags.forEach { tag ->
                val chip = createTagChip(tag)
                customTagsContainer.addView(chip)
            }
        } else {
            customTagsContainer.visibility = View.GONE
        }
    }

    private fun createTagChip(name: String): com.google.android.material.chip.Chip {
        val chip = com.google.android.material.chip.Chip(requireContext()).apply {
            text = name
            isCloseIconVisible = false
            isCheckable = false

            // Устанавливаем начальное состояние
            val isSelected = selectedTags.contains(name)

            // Применяем стиль в зависимости от состояния
            updateTagChipAppearance(this, isSelected)

            setOnClickListener {
                toggleTag(name)
                val newSelected = selectedTags.contains(name)
                updateTagChipAppearance(this, newSelected)
            }
        }
        return chip
    }

    private fun updateTagChipAppearance(chip: com.google.android.material.chip.Chip, isSelected: Boolean) {
        // Все теги (и предопределённые, и пользовательские) используют одинаковый стиль
        if (isSelected) {
            chip.chipBackgroundColor = ContextCompat.getColorStateList(requireContext(), R.color.color_purple)
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        } else {
            chip.chipBackgroundColor = ContextCompat.getColorStateList(requireContext(), R.color.muted)
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.foreground))
        }
        chip.chipStrokeWidth = 0f
    }
    
    private fun saveNote() {
        val title = binding.noteTitleInput.text?.toString()?.trim() ?: ""
        val content = binding.noteInput.text?.toString()?.trim() ?: ""
        // Разрешаем создание заметки если есть название или описание
        if (title.isNotBlank() || content.isNotBlank()) {
            viewModel.addNote(title, content, selectedTags.toList())
            hideNoteForm()
        }
    }
    
    private fun toggleTaskForm() {
        if (binding.addTaskForm.visibility == View.VISIBLE) {
            hideTaskForm()
        } else {
            showTaskForm()
        }
    }
    
    private fun showTaskForm() {
        binding.addTaskButton.visibility = View.GONE
        binding.addTaskForm.visibility = View.VISIBLE
        binding.taskInput.requestFocus()
        sharedPreferences.edit().putBoolean(KEY_TASK_FORM_VISIBLE, true).apply()
    }
    
    private fun hideTaskForm() {
        binding.addTaskButton.visibility = View.VISIBLE
        binding.addTaskForm.visibility = View.GONE
        binding.taskInput.text?.clear()
        binding.taskDescriptionInput.text?.clear()
        sharedPreferences.edit().putBoolean(KEY_TASK_FORM_VISIBLE, false).apply()
        clearTaskDraft()
    }
    
    private fun saveTask() {
        val title = binding.taskInput.text?.toString()?.trim() ?: ""
        val description = binding.taskDescriptionInput.text?.toString()?.trim()
        if (title.isNotBlank()) {
            tasksViewModel.addTask(title, description)
            hideTaskForm()
        }
    }
    
    private fun switchToNotesTab() {
        binding.apply {
            notesTab.setBackgroundResource(R.drawable.tab_active_background)
            notesTab.setTextColor(resources.getColor(R.color.accent_foreground, null))
            tasksTab.setBackgroundResource(R.drawable.tab_inactive_background)
            tasksTab.setTextColor(resources.getColor(R.color.muted_foreground, null))
            
            notesContent.visibility = View.VISIBLE
            tasksContent.visibility = View.GONE
        }
    }
    
    private fun switchToTasksTab() {
        binding.apply {
            tasksTab.setBackgroundResource(R.drawable.tab_active_background)
            tasksTab.setTextColor(resources.getColor(R.color.accent_foreground, null))
            notesTab.setBackgroundResource(R.drawable.tab_inactive_background)
            notesTab.setTextColor(resources.getColor(R.color.muted_foreground, null))
            
            tasksContent.visibility = View.VISIBLE
            notesContent.visibility = View.GONE
        }
        tasksViewModel.loadTasks()
    }
    
    private fun deleteNoteDirectly(note: Note) {
        AlertDialog.Builder(requireContext())
            .setTitle("Удалить заметку?")
            .setMessage("Вы уверены, что хотите удалить эту заметку?")
            .setPositiveButton("Удалить") { _, _ ->
                viewModel.deleteNote(note)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showNoteOptionsDialog(note: Note) {
        AlertDialog.Builder(requireContext())
            .setTitle("Опции заметки")
            .setItems(arrayOf("Редактировать", "Удалить")) { _, which ->
                when (which) {
                    0 -> showEditNoteDialog(note)
                    1 -> deleteNoteDirectly(note)
                }
            }
            .show()
    }
    
    private fun deleteTaskDirectly(task: Task) {
        AlertDialog.Builder(requireContext())
            .setTitle("Удалить задачу?")
            .setMessage("Вы уверены, что хотите удалить эту задачу?")
            .setPositiveButton("Удалить") { _, _ ->
                tasksViewModel.deleteTask(task.id)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showTaskOptionsDialog(task: Task) {
        AlertDialog.Builder(requireContext())
            .setTitle("Опции задачи")
            .setItems(arrayOf("Редактировать", "Удалить")) { _, which ->
                when (which) {
                    0 -> showEditTaskDialog(task)
                    1 -> deleteTaskDirectly(task)
                }
            }
            .show()
    }
    
    private fun showTaskDetail(task: Task) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_view_task, null)
        
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        
        dialogView.findViewById<android.widget.TextView>(R.id.task_timestamp).text = 
            dateFormat.format(Date(task.timestamp))
        dialogView.findViewById<android.widget.TextView>(R.id.task_title).text = task.title
        
        val descriptionView = dialogView.findViewById<android.widget.TextView>(R.id.task_description)
        if (task.description != null) {
            descriptionView.text = task.description
            descriptionView.visibility = android.view.View.VISIBLE
        } else {
            descriptionView.visibility = android.view.View.GONE
        }
        
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        dialogView.findViewById<android.widget.Button>(R.id.close_button).setOnClickListener {
            dialog.dismiss()
        }
        
        dialogView.findViewById<android.widget.Button>(R.id.edit_button).setOnClickListener {
            dialog.dismiss()
            showEditTaskDialog(task)
        }
        
        dialog.show()
    }
    
    private fun showEditTaskDialog(task: Task) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_edit_task, null)
        
        val titleInput = dialogView.findViewById<android.widget.EditText>(R.id.task_title_input)
        titleInput.setText(task.title)
        
        val descriptionInput = dialogView.findViewById<android.widget.EditText>(R.id.task_description_input)
        descriptionInput.setText(task.description)
        
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        dialogView.findViewById<android.widget.Button>(R.id.cancel_button).setOnClickListener {
            dialog.dismiss()
        }
        
        dialogView.findViewById<android.widget.Button>(R.id.save_button).setOnClickListener {
            val newTitle = titleInput.text?.toString()?.trim() ?: ""
            val newDescription = descriptionInput.text?.toString()?.trim()
            if (newTitle.isNotBlank()) {
                tasksViewModel.updateTask(task.copy(
                    title = newTitle,
                    description = if (newDescription.isNullOrBlank()) null else newDescription
                ))
                dialog.dismiss()
            }
        }
        
        dialog.show()
    }
    
    private fun showNoteDetail(note: Note) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_view_note, null)
        
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        
        dialogView.findViewById<android.widget.TextView>(R.id.note_timestamp).text =
            dateFormat.format(Date(note.timestamp))
        dialogView.findViewById<android.widget.TextView>(R.id.note_title).text = note.title

        val contentView = dialogView.findViewById<android.widget.TextView>(R.id.note_content)
        if (note.content.isNotBlank()) {
            contentView.text = note.content
            contentView.visibility = View.VISIBLE
        } else {
            contentView.visibility = View.GONE
        }

        // Теги
        val tagsTextView = dialogView.findViewById<android.widget.TextView>(R.id.note_tags)
        if (note.tags.isNotEmpty()) {
            tagsTextView.text = note.tags.joinToString(", ")
            tagsTextView.visibility = View.VISIBLE
        } else {
            tagsTextView.visibility = View.GONE
        }
        
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        dialogView.findViewById<android.widget.Button>(R.id.close_button).setOnClickListener {
            dialog.dismiss()
        }
        
        dialogView.findViewById<android.widget.Button>(R.id.edit_button).setOnClickListener {
            dialog.dismiss()
            showEditNoteDialog(note)
        }
        
        dialog.show()
    }
    
    private fun showEditNoteDialog(note: Note) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_edit_note, null)

        val titleInput = dialogView.findViewById<android.widget.EditText>(R.id.note_title_input)
        val contentInput = dialogView.findViewById<android.widget.EditText>(R.id.note_content_input)

        titleInput.setText(note.title)
        contentInput.setText(note.content)

        // Теги
        val selectedTags = note.tags.toMutableSet()
        // Используем allCustomTags из фрагмента, который содержит все теги из базы данных
        val allCustomTags = this.allCustomTags.toMutableSet()

        val predefinedContainer = dialogView.findViewById<com.google.android.material.chip.ChipGroup>(R.id.predefined_tags_container)

        fun displayPredefinedTagsInDialog() {
            val predefinedTags = listOf(
                getString(R.string.tag_work),
                getString(R.string.tag_personal),
                getString(R.string.tag_idea)
            )

            predefinedContainer.removeAllViews()

            predefinedTags.forEach { tag ->
                val chip = createTagChipInDialog(tag, selectedTags)
                predefinedContainer.addView(chip)
            }

            // Добавляем кнопку "+" для добавления пользовательских тегов
            val addChip = com.google.android.material.chip.Chip(requireContext()).apply {
                text = "+"
                isCloseIconVisible = false
                isCheckable = false
                chipBackgroundColor = ContextCompat.getColorStateList(requireContext(), R.color.muted)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.foreground))
                textSize = 16f
                setTypeface(null, android.graphics.Typeface.BOLD)
                chipStrokeWidth = 0f

                setOnClickListener {
                    showAddCustomTagDialogInDialog(dialogView, selectedTags, allCustomTags) {
                        displayPredefinedTagsInDialog()
                        displayCustomTagsInDialog(dialogView, selectedTags, allCustomTags)
                    }
                }
            }
            predefinedContainer.addView(addChip)
        }

        displayPredefinedTagsInDialog()
        displayCustomTagsInDialog(dialogView, selectedTags, allCustomTags)
        
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        dialogView.findViewById<android.widget.Button>(R.id.cancel_button).setOnClickListener {
            dialog.dismiss()
        }
        
        dialogView.findViewById<android.widget.Button>(R.id.save_button).setOnClickListener {
            val newTitle = titleInput.text?.toString()?.trim() ?: ""
            val newContent = contentInput.text?.toString()?.trim() ?: ""
            if (newContent.isNotBlank()) {
                viewModel.updateNote(note.copy(title = newTitle, content = newContent, tags = selectedTags.toList()))
                dialog.dismiss()
            }
        }
        
        dialog.show()
    }
    
    private fun showAddCustomTagDialogInDialog(
        dialogView: android.view.View,
        selectedTags: MutableSet<String>,
        allCustomTags: MutableSet<String>,
        onUpdateTags: () -> Unit
    ) {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(R.layout.dialog_add_tag)
            .create()
        
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        dialog.show()
        
        val tagInput = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.tag_input)
        tagInput?.requestFocus()
        
        val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.showSoftInput(tagInput, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
        
        dialog.findViewById<Button>(R.id.cancel_button)?.setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.findViewById<Button>(R.id.add_button)?.setOnClickListener {
            val tagName = tagInput?.text?.toString()?.trim() ?: ""
            if (tagName.isNotBlank()) {
                val predefinedTags = setOf(
                    getString(R.string.tag_work),
                    getString(R.string.tag_personal),
                    getString(R.string.tag_idea)
                )
                if (tagName !in predefinedTags) {
                    allCustomTags.add(tagName)
                }
                selectedTags.add(tagName)
                
                // Сохраняем тег в базу данных, если это пользовательский тег
                lifecycleScope.launch {
                    val customTag = CustomTag(
                        id = "",
                        name = tagName,
                        color = null,
                        isActive = true
                    )
                    addCustomTagUseCase(customTag)
                }
                
                onUpdateTags()
                displayCustomTagsInDialog(dialogView, selectedTags, allCustomTags)
                dialog.dismiss()
            } else {
                tagInput?.error = "Введите название тега"
            }
        }
    }
    
    private fun displayCustomTagsInDialog(dialogView: View, selectedTags: MutableSet<String>, allCustomTags: MutableSet<String>) {
        val predefinedTags = setOf(
            getString(R.string.tag_work),
            getString(R.string.tag_personal),
            getString(R.string.tag_idea)
        )
        val customTags = allCustomTags.filter { it !in predefinedTags }

        val customTagsContainer = dialogView.findViewById<com.google.android.material.chip.ChipGroup>(R.id.custom_tags_container)
        customTagsContainer.removeAllViews()

        if (customTags.isNotEmpty()) {
            customTagsContainer.visibility = View.VISIBLE
            customTags.forEach { tag ->
                val chip = createTagChipInDialog(tag, selectedTags)
                customTagsContainer.addView(chip)
            }
        } else {
            customTagsContainer.visibility = View.GONE
        }
    }

    private fun createTagChipInDialog(name: String, selectedTags: MutableSet<String>): com.google.android.material.chip.Chip {
        val chip = com.google.android.material.chip.Chip(requireContext()).apply {
            text = name
            isCloseIconVisible = false
            isCheckable = false

            // Устанавливаем начальное состояние
            val isSelected = selectedTags.contains(name)

            // Применяем стиль в зависимости от состояния
            updateTagChipAppearanceInDialog(this, isSelected)

            setOnClickListener {
                if (selectedTags.contains(name)) {
                    selectedTags.remove(name)
                } else {
                    selectedTags.add(name)
                }
                val newSelected = selectedTags.contains(name)
                updateTagChipAppearanceInDialog(this, newSelected)
            }
        }
        return chip
    }

    private fun updateTagChipAppearanceInDialog(chip: com.google.android.material.chip.Chip, isSelected: Boolean) {
        // Все теги (и предопределённые, и пользовательские) используют одинаковый стиль
        if (isSelected) {
            chip.chipBackgroundColor = ContextCompat.getColorStateList(requireContext(), R.color.color_purple)
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        } else {
            chip.chipBackgroundColor = ContextCompat.getColorStateList(requireContext(), R.color.muted)
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.foreground))
        }
        chip.chipStrokeWidth = 0f
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    override fun onPause() {
        super.onPause()
        saveDrafts()
    }
    
    private fun saveDrafts() {
        // Сохраняем черновик заметки
        val noteTitle = binding.noteTitleInput.text?.toString()?.trim() ?: ""
        val noteContent = binding.noteInput.text?.toString()?.trim() ?: ""
        val noteFormVisible = binding.addNoteForm.visibility == View.VISIBLE
        
        sharedPreferences.edit().apply {
            putString(KEY_NOTE_TITLE, noteTitle)
            putString(KEY_NOTE_CONTENT, noteContent)
            putStringSet(KEY_NOTE_TAGS, selectedTags)
            putBoolean(KEY_NOTE_FORM_VISIBLE, noteFormVisible)
        }.apply()
        
        // Сохраняем черновик задачи
        val taskTitle = binding.taskInput.text?.toString()?.trim() ?: ""
        val taskDescription = binding.taskDescriptionInput.text?.toString()?.trim()
        val taskFormVisible = binding.addTaskForm.visibility == View.VISIBLE
        
        sharedPreferences.edit().apply {
            putString(KEY_TASK_TITLE, taskTitle)
            putString(KEY_TASK_DESCRIPTION, taskDescription)
            putBoolean(KEY_TASK_FORM_VISIBLE, taskFormVisible)
        }.apply()
    }
    
    private fun restoreDrafts() {
        // Восстанавливаем черновик заметки
        val noteTitle = sharedPreferences.getString(KEY_NOTE_TITLE, "")
        val noteContent = sharedPreferences.getString(KEY_NOTE_CONTENT, "")
        val noteTags = sharedPreferences.getStringSet(KEY_NOTE_TAGS, null)
        val noteFormVisible = sharedPreferences.getBoolean(KEY_NOTE_FORM_VISIBLE, false)
        
        if (noteTitle?.isNotBlank() == true || noteContent?.isNotBlank() == true || 
            (noteTags?.isNotEmpty() == true)) {
            
            binding.noteTitleInput.setText(noteTitle ?: "")
            binding.noteInput.setText(noteContent ?: "")
            
            noteTags?.forEach { tag ->
                selectedTags.add(tag)
            }
            
            if (noteFormVisible) {
                binding.addNoteButton.visibility = View.GONE
                binding.addNoteForm.visibility = View.VISIBLE
                displayPredefinedTags()
                loadCustomTags()
            }
        }
        
        // Восстанавливаем черновик задачи
        val taskTitle = sharedPreferences.getString(KEY_TASK_TITLE, "")
        val taskDescription = sharedPreferences.getString(KEY_TASK_DESCRIPTION, "")
        val taskFormVisible = sharedPreferences.getBoolean(KEY_TASK_FORM_VISIBLE, false)
        
        if (taskTitle?.isNotBlank() == true || taskDescription?.isNotBlank() == true) {
            binding.taskInput.setText(taskTitle ?: "")
            binding.taskDescriptionInput.setText(taskDescription ?: "")
            
            if (taskFormVisible) {
                binding.addTaskButton.visibility = View.GONE
                binding.addTaskForm.visibility = View.VISIBLE
            }
        }
    }
    
    private fun clearNoteDraft() {
        sharedPreferences.edit().apply {
            remove(KEY_NOTE_TITLE)
            remove(KEY_NOTE_CONTENT)
            remove(KEY_NOTE_TAGS)
            remove(KEY_NOTE_FORM_VISIBLE)
        }.apply()
    }
    
    private fun clearTaskDraft() {
        sharedPreferences.edit().apply {
            remove(KEY_TASK_TITLE)
            remove(KEY_TASK_DESCRIPTION)
            remove(KEY_TASK_FORM_VISIBLE)
        }.apply()
    }
}
