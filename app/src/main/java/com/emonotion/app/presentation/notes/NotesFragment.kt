package com.emonotion.app.presentation.notes

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
        setupRecyclerView()
        setupUI()
        observeViewModel()
        loadCustomTags()
        viewModel.loadNotes()
        tasksViewModel.loadTasks()
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
            
            // Теги заметок
            tagWork.setOnClickListener {
                toggleTag(getString(R.string.tag_work))
            }
            
            tagPersonal.setOnClickListener {
                toggleTag(getString(R.string.tag_personal))
            }
            
            binding.tagIdea.setOnClickListener {
                toggleTag(getString(R.string.tag_idea))
            }
            
            binding.addCustomTagButton.setOnClickListener {
                showAddCustomTagDialog()
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
                        updateSortText(getString(R.string.sort_newest))
                    }
                    1 -> {
                        tasksViewModel.setSortOrder(TasksViewModel.SortOrder.OLDEST_FIRST)
                        updateSortText(getString(R.string.sort_oldest))
                    }
                    2 -> {
                        tasksViewModel.setSortOrder(TasksViewModel.SortOrder.ALPHABETICAL_ASC)
                        updateSortText(getString(R.string.sort_alphabetical_asc))
                    }
                    3 -> {
                        tasksViewModel.setSortOrder(TasksViewModel.SortOrder.ALPHABETICAL_DESC)
                        updateSortText(getString(R.string.sort_alphabetical_desc))
                    }
                }
                dialog.dismiss()
            }
            .show()
    }
    
    private fun updateSortText(text: String) {
        binding.sortText.text = text
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
                tasksAdapter.submitList(tasks)
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
        displayCustomTags()
    }
    
    private fun hideNoteForm() {
        binding.addNoteButton.visibility = View.VISIBLE
        binding.addNoteForm.visibility = View.GONE
        binding.noteTitleInput.text?.clear()
        binding.noteInput.text?.clear()
        selectedTags.clear()
        // Не очищаем allCustomTags - это глобальный список тегов из базы данных
        resetTagButtons()
        binding.customTagsContainer.removeAllViews()
        binding.customTagsContainer.visibility = View.GONE
    }
    
    private fun toggleTag(tag: String) {
        if (selectedTags.contains(tag)) {
            selectedTags.remove(tag)
        } else {
            selectedTags.add(tag)
        }
        updateTagButtons()
        displayCustomTags()
    }
    
    private fun updateTagButtons() {
        val workTag = getString(R.string.tag_work)
        val personalTag = getString(R.string.tag_personal)
        val ideaTag = getString(R.string.tag_idea)
        
        updateTagAppearance(binding.tagWork, selectedTags.contains(workTag))
        updateTagAppearance(binding.tagPersonal, selectedTags.contains(personalTag))
        updateTagAppearance(binding.tagIdea, selectedTags.contains(ideaTag))
    }
    
    private fun updateTagAppearance(tagView: android.widget.TextView, isSelected: Boolean) {
        if (isSelected) {
            // Выбранный тег - фиолетовый фон с белым текстом
            tagView.alpha = 1.0f
            tagView.textSize = 14f
            tagView.background = ContextCompat.getDrawable(requireContext(), R.drawable.tag_purple_background)
            tagView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            tagView.setPadding(
                (16 * resources.displayMetrics.density).toInt(),
                (8 * resources.displayMetrics.density).toInt(),
                (16 * resources.displayMetrics.density).toInt(),
                (8 * resources.displayMetrics.density).toInt()
            )
        } else {
            // Невыбранный тег - базовый стиль без цвета
            tagView.alpha = 0.7f
            tagView.textSize = 13f
            tagView.background = ContextCompat.getDrawable(requireContext(), R.drawable.tag_background)
            tagView.setTextColor(ContextCompat.getColor(requireContext(), R.color.foreground))
            tagView.setPadding(
                (14 * resources.displayMetrics.density).toInt(),
                (6 * resources.displayMetrics.density).toInt(),
                (14 * resources.displayMetrics.density).toInt(),
                (6 * resources.displayMetrics.density).toInt()
            )
        }
    }
    
    private fun resetTagButtons() {
        updateTagAppearance(binding.tagWork, false)
        updateTagAppearance(binding.tagPersonal, false)
        updateTagAppearance(binding.tagIdea, false)
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
                val tagView = createCustomTag(tag)
                customTagsContainer.addView(tagView)
            }
        } else {
            customTagsContainer.visibility = View.GONE
        }
    }
    
    private fun createCustomTag(name: String): android.widget.TextView {
        val tagView = android.widget.TextView(requireContext()).apply {
            text = name
            setOnClickListener { toggleTag(name) }
            
            // Применяем базовый стиль для кастомных тегов
            background = ContextCompat.getDrawable(requireContext(), R.drawable.tag_background)
            setPadding(
                (14 * resources.displayMetrics.density).toInt(),
                (6 * resources.displayMetrics.density).toInt(),
                (14 * resources.displayMetrics.density).toInt(),
                (6 * resources.displayMetrics.density).toInt()
            )
            
            // Устанавливаем цвет текста для кастомных тегов
            setTextColor(ContextCompat.getColor(requireContext(), R.color.foreground))
            textSize = 13f
            setTypeface(null, android.graphics.Typeface.BOLD)
            
            // Применяем визуальное различие для выбранного/невыбранного состояния
            val isSelected = selectedTags.contains(name)
            updateTagAppearance(this, isSelected)
            
            val params = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.marginEnd = (8 * resources.displayMetrics.density).toInt()
            params.bottomMargin = (4 * resources.displayMetrics.density).toInt()
            layoutParams = params
        }
        return tagView
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
    }
    
    private fun hideTaskForm() {
        binding.addTaskButton.visibility = View.VISIBLE
        binding.addTaskForm.visibility = View.GONE
        binding.taskInput.text?.clear()
        binding.taskDescriptionInput.text?.clear()
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
        dialogView.findViewById<android.widget.TextView>(R.id.note_content).text = note.content
        
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
        
        val tagWork = dialogView.findViewById<android.widget.TextView>(R.id.tag_work)
        val tagPersonal = dialogView.findViewById<android.widget.TextView>(R.id.tag_personal)
        val tagIdea = dialogView.findViewById<android.widget.TextView>(R.id.tag_idea)
        
        fun updateTagButtons() {
            val workTag = getString(R.string.tag_work)
            val personalTag = getString(R.string.tag_personal)
            val ideaTag = getString(R.string.tag_idea)
            
            updateTagAppearance(tagWork, selectedTags.contains(workTag))
            updateTagAppearance(tagPersonal, selectedTags.contains(personalTag))
            updateTagAppearance(tagIdea, selectedTags.contains(ideaTag))
        }
        
        fun updateTagAppearance(tagView: android.widget.TextView, isSelected: Boolean) {
            if (isSelected) {
                tagView.alpha = 1.0f
                tagView.textSize = 14f
                tagView.setPadding(
                    (16 * resources.displayMetrics.density).toInt(),
                    (8 * resources.displayMetrics.density).toInt(),
                    (16 * resources.displayMetrics.density).toInt(),
                    (8 * resources.displayMetrics.density).toInt()
                )
            } else {
                tagView.alpha = 0.6f
                tagView.textSize = 13f
                tagView.setPadding(
                    (14 * resources.displayMetrics.density).toInt(),
                    (6 * resources.displayMetrics.density).toInt(),
                    (14 * resources.displayMetrics.density).toInt(),
                    (6 * resources.displayMetrics.density).toInt()
                )
            }
        }
        
        updateTagButtons()
        
        tagWork.setOnClickListener {
            val tag = getString(R.string.tag_work)
            if (selectedTags.contains(tag)) {
                selectedTags.remove(tag)
            } else {
                selectedTags.add(tag)
            }
            updateTagButtons()
        }
        
        tagPersonal.setOnClickListener {
            val tag = getString(R.string.tag_personal)
            if (selectedTags.contains(tag)) {
                selectedTags.remove(tag)
            } else {
                selectedTags.add(tag)
            }
            updateTagButtons()
        }
        
        tagIdea.setOnClickListener {
            val tag = getString(R.string.tag_idea)
            if (selectedTags.contains(tag)) {
                selectedTags.remove(tag)
            } else {
                selectedTags.add(tag)
            }
            updateTagButtons()
        }
        
        dialogView.findViewById<android.widget.TextView>(R.id.add_custom_tag_button).setOnClickListener {
            showAddCustomTagDialogInDialog(dialogView, selectedTags, allCustomTags) { updateTagButtons() }
        }
        
        displayCustomTagsInDialog(dialogView, selectedTags, allCustomTags) { updateTagButtons() }
        
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
                displayCustomTagsInDialog(dialogView, selectedTags, allCustomTags, onUpdateTags)
                dialog.dismiss()
            } else {
                tagInput?.error = "Введите название тега"
            }
        }
    }
    
    private fun displayCustomTagsInDialog(
        dialogView: android.view.View,
        selectedTags: MutableSet<String>,
        allCustomTags: MutableSet<String>,
        onUpdateTags: () -> Unit
    ) {
        val predefinedTags = setOf(
            getString(R.string.tag_work),
            getString(R.string.tag_personal),
            getString(R.string.tag_idea)
        )
        val customTags = allCustomTags.filter { it !in predefinedTags }
        
        val customTagsContainer = dialogView.findViewById<android.widget.LinearLayout>(R.id.custom_tags_container)
        customTagsContainer?.removeAllViews()
        
        if (customTags.isNotEmpty()) {
            customTagsContainer?.visibility = View.VISIBLE
            customTags.forEach { tag ->
                val tagView = createCustomTagInDialog(tag, selectedTags, onUpdateTags)
                customTagsContainer?.addView(tagView)
            }
        } else {
            customTagsContainer?.visibility = View.GONE
        }
    }
    
    private fun createCustomTagInDialog(
        name: String,
        selectedTags: MutableSet<String>,
        onUpdateTags: () -> Unit
    ): android.widget.TextView {
        val tagView = android.widget.TextView(requireContext()).apply {
            text = name
            setOnClickListener {
                if (selectedTags.contains(name)) {
                    selectedTags.remove(name)
                } else {
                    selectedTags.add(name)
                }
                onUpdateTags()
                // Обновляем внешний вид всех кастомных тегов
                updateTagAppearance(this, selectedTags.contains(name))
            }
            
            // Применяем базовый стиль для кастомных тегов
            background = ContextCompat.getDrawable(requireContext(), R.drawable.tag_background)
            setPadding(
                (14 * resources.displayMetrics.density).toInt(),
                (6 * resources.displayMetrics.density).toInt(),
                (14 * resources.displayMetrics.density).toInt(),
                (6 * resources.displayMetrics.density).toInt()
            )
            
            // Устанавливаем цвет текста для кастомных тегов
            setTextColor(ContextCompat.getColor(requireContext(), R.color.foreground))
            textSize = 13f
            setTypeface(null, android.graphics.Typeface.BOLD)
            
            // Применяем визуальное различие для выбранного/невыбранного состояния
            val isSelected = selectedTags.contains(name)
            updateTagAppearance(this, isSelected)
            
            val params = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.marginEnd = (8 * resources.displayMetrics.density).toInt()
            params.bottomMargin = (4 * resources.displayMetrics.density).toInt()
            layoutParams = params
        }
        return tagView
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
