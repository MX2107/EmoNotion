package com.emonotion.app.presentation.notes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.emonotion.app.R
import com.emonotion.app.databinding.FragmentNotesBinding
import com.emonotion.app.domain.model.Note
import com.emonotion.app.domain.model.Task
import com.emonotion.app.presentation.adapter.NotesAdapter
import com.emonotion.app.presentation.adapter.TasksAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

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
        viewModel.loadNotes()
        tasksViewModel.loadTasks()
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
            
            // Кнопки формы задачи
            saveTaskButton.setOnClickListener {
                saveTask()
            }
            
            cancelTaskButton.setOnClickListener {
                hideTaskForm()
            }
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.notes.collect { notes ->
                notesAdapter.submitList(notes)
                updateEmptyState(notes.isEmpty())
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
    }
    
    private fun hideNoteForm() {
        binding.addNoteButton.visibility = View.VISIBLE
        binding.addNoteForm.visibility = View.GONE
        binding.noteInput.text?.clear()
    }
    
    private fun saveNote() {
        val content = binding.noteInput.text?.toString()?.trim() ?: ""
        if (content.isNotBlank()) {
            viewModel.addNote("", content)
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
    }
    
    private fun saveTask() {
        val title = binding.taskInput.text?.toString()?.trim() ?: ""
        if (title.isNotBlank()) {
            tasksViewModel.addTask(title)
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
                tasksViewModel.deleteTask(task)
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
        
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        dialogView.findViewById<android.widget.Button>(R.id.cancel_button).setOnClickListener {
            dialog.dismiss()
        }
        
        dialogView.findViewById<android.widget.Button>(R.id.save_button).setOnClickListener {
            val newTitle = titleInput.text?.toString()?.trim() ?: ""
            if (newTitle.isNotBlank()) {
                tasksViewModel.updateTask(task.copy(title = newTitle))
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
        
        val contentInput = dialogView.findViewById<android.widget.EditText>(R.id.note_content_input)
        contentInput.setText(note.content)
        
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        dialogView.findViewById<android.widget.Button>(R.id.cancel_button).setOnClickListener {
            dialog.dismiss()
        }
        
        dialogView.findViewById<android.widget.Button>(R.id.save_button).setOnClickListener {
            val newContent = contentInput.text?.toString()?.trim() ?: ""
            if (newContent.isNotBlank()) {
                viewModel.updateNote(note.copy(content = newContent))
                dialog.dismiss()
            }
        }
        
        dialog.show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
