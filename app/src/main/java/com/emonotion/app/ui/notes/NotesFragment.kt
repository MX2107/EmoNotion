package com.emonotion.app.ui.notes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.emonotion.app.R
import java.text.SimpleDateFormat
import java.util.*

data class Note(
    val id: String,
    val text: String,
    val timestamp: String
)

data class Task(
    val id: String,
    val text: String,
    var completed: Boolean,
    val dueDate: String?
)

class NotesFragment : Fragment() {
    
    private lateinit var notesTab: Button
    private lateinit var tasksTab: Button
    private lateinit var notesContent: LinearLayout
    private lateinit var tasksContent: LinearLayout
    
    private lateinit var addNoteButton: LinearLayout
    private lateinit var addNoteForm: LinearLayout
    private lateinit var noteInput: EditText
    private lateinit var saveNoteButton: Button
    private lateinit var cancelNoteButton: Button
    private lateinit var notesRecycler: RecyclerView
    
    private lateinit var addTaskButton: LinearLayout
    private lateinit var addTaskForm: LinearLayout
    private lateinit var taskInput: EditText
    private lateinit var saveTaskButton: Button
    private lateinit var cancelTaskButton: Button
    private lateinit var tasksRecycler: RecyclerView
    
    private var activeTab = "notes"
    private val notes = mutableListOf<Note>()
    private val tasks = mutableListOf<Task>()
    private val notesAdapter = NotesAdapter(notes) { note -> deleteNote(note) }
    private val tasksAdapter = TasksAdapter(tasks) { task -> toggleTask(task) }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notes, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupClickListeners()
        setupAdapters()
        // В реальном приложении данные будут загружаться из базы данных
    }
    
    private fun initViews(view: View) {
        try {
            notesTab = view.findViewById(R.id.notes_tab)
            tasksTab = view.findViewById(R.id.tasks_tab)
            notesContent = view.findViewById(R.id.notes_content)
            tasksContent = view.findViewById(R.id.tasks_content)
            
            addNoteButton = view.findViewById(R.id.add_note_button)
            addNoteForm = view.findViewById(R.id.add_note_form)
            noteInput = view.findViewById(R.id.note_input)
            saveNoteButton = view.findViewById(R.id.save_note_button)
            cancelNoteButton = view.findViewById(R.id.cancel_note_button)
            notesRecycler = view.findViewById(R.id.notes_recycler)
            
            addTaskButton = view.findViewById(R.id.add_task_button)
            addTaskForm = view.findViewById(R.id.add_task_form)
            taskInput = view.findViewById(R.id.task_input)
            saveTaskButton = view.findViewById(R.id.save_task_button)
            cancelTaskButton = view.findViewById(R.id.cancel_task_button)
            tasksRecycler = view.findViewById(R.id.tasks_recycler)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun setupClickListeners() {
        notesTab.setOnClickListener {
            switchToNotesTab()
        }
        
        tasksTab.setOnClickListener {
            switchToTasksTab()
        }
        
        addNoteButton.setOnClickListener {
            showAddNoteForm()
        }
        
        saveNoteButton.setOnClickListener {
            saveNote()
        }
        
        cancelNoteButton.setOnClickListener {
            hideAddNoteForm()
        }
        
        addTaskButton.setOnClickListener {
            showAddTaskForm()
        }
        
        saveTaskButton.setOnClickListener {
            saveTask()
        }
        
        cancelTaskButton.setOnClickListener {
            hideAddTaskForm()
        }
    }
    
    private fun setupAdapters() {
        notesRecycler.layoutManager = LinearLayoutManager(context)
        notesRecycler.adapter = notesAdapter
        
        tasksRecycler.layoutManager = LinearLayoutManager(context)
        tasksRecycler.adapter = tasksAdapter
    }
    
    private fun switchToNotesTab() {
        activeTab = "notes"
        notesTab.background = resources.getDrawable(R.drawable.tab_active_background, null)
        notesTab.setTextColor(resources.getColor(R.color.accent_foreground, null))
        tasksTab.background = resources.getDrawable(R.drawable.tab_inactive_background, null)
        tasksTab.setTextColor(resources.getColor(R.color.muted_foreground, null))
        
        notesContent.visibility = View.VISIBLE
        tasksContent.visibility = View.GONE
    }
    
    private fun switchToTasksTab() {
        activeTab = "tasks"
        tasksTab.background = resources.getDrawable(R.drawable.tab_active_background, null)
        tasksTab.setTextColor(resources.getColor(R.color.accent_foreground, null))
        notesTab.background = resources.getDrawable(R.drawable.tab_inactive_background, null)
        notesTab.setTextColor(resources.getColor(R.color.muted_foreground, null))
        
        tasksContent.visibility = View.VISIBLE
        notesContent.visibility = View.GONE
    }
    
    private fun showAddNoteForm() {
        addNoteButton.visibility = View.GONE
        addNoteForm.visibility = View.VISIBLE
        noteInput.requestFocus()
    }
    
    private fun hideAddNoteForm() {
        addNoteButton.visibility = View.VISIBLE
        addNoteForm.visibility = View.GONE
        noteInput.text.clear()
    }
    
    private fun saveNote() {
        val text = noteInput.text.toString().trim()
        if (text.isNotEmpty()) {
            val timestamp = getCurrentTimestamp()
            val note = Note(
                id = Date().time.toString(),
                text = text,
                timestamp = timestamp
            )
            notes.add(0, note)
            notesAdapter.notifyItemInserted(0)
            hideAddNoteForm()
        }
    }
    
    private fun showAddTaskForm() {
        addTaskButton.visibility = View.GONE
        addTaskForm.visibility = View.VISIBLE
        taskInput.requestFocus()
    }
    
    private fun hideAddTaskForm() {
        addTaskButton.visibility = View.VISIBLE
        addTaskForm.visibility = View.GONE
        taskInput.text.clear()
    }
    
    private fun saveTask() {
        val text = taskInput.text.toString().trim()
        if (text.isNotEmpty()) {
            val task = Task(
                id = Date().time.toString(),
                text = text,
                completed = false,
                dueDate = null
            )
            tasks.add(task)
            tasksAdapter.notifyItemInserted(tasks.size - 1)
            hideAddTaskForm()
        }
    }
    
    private fun deleteNote(note: Note) {
        val position = notes.indexOf(note)
        if (position != -1) {
            notes.removeAt(position)
            notesAdapter.notifyItemRemoved(position)
        }
    }
    
    private fun toggleTask(task: Task) {
        val position = tasks.indexOf(task)
        if (position != -1) {
            task.completed = !task.completed
            tasksAdapter.notifyItemChanged(position)
        }
    }
    
    private fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }
    
    inner class NotesAdapter(
        private val notes: List<Note>,
        private val onDeleteClick: (Note) -> Unit
    ) : RecyclerView.Adapter<NotesAdapter.ViewHolder>() {
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_note, parent, false)
            return ViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(notes[position])
        }
        
        override fun getItemCount(): Int = notes.size
        
        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val timestampText: TextView = itemView.findViewById(R.id.note_timestamp)
            private val noteText: TextView = itemView.findViewById(R.id.note_text)
            private val deleteButton: ImageButton = itemView.findViewById(R.id.delete_note_button)
            
            fun bind(note: Note) {
                timestampText.text = note.timestamp
                noteText.text = note.text
                
                deleteButton.setOnClickListener {
                    onDeleteClick(note)
                }
            }
        }
    }
    
    inner class TasksAdapter(
        private val tasks: List<Task>,
        private val onToggleClick: (Task) -> Unit
    ) : RecyclerView.Adapter<TasksAdapter.ViewHolder>() {
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_task, parent, false)
            return ViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(tasks[position])
        }
        
        override fun getItemCount(): Int = tasks.size
        
        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val checkbox: CheckBox = itemView.findViewById(R.id.task_checkbox)
            private val taskText: TextView = itemView.findViewById(R.id.task_text)
            private val dueDateText: TextView = itemView.findViewById(R.id.task_due_date)
            private val deleteButton: ImageButton = itemView.findViewById(R.id.delete_task_button)
            
            fun bind(task: Task) {
                checkbox.isChecked = task.completed
                taskText.text = task.text
                taskText.alpha = if (task.completed) 0.5f else 1.0f
                
                if (task.dueDate != null) {
                    dueDateText.text = task.dueDate
                    dueDateText.visibility = View.VISIBLE
                } else {
                    dueDateText.visibility = View.GONE
                }
                
                checkbox.setOnCheckedChangeListener { _, _ ->
                    onToggleClick(task)
                }
                
                deleteButton.setOnClickListener {
                    val position = tasks.indexOf(task)
                    if (position != -1) {
                        (this@NotesFragment.tasks as MutableList).removeAt(position)
                        notifyItemRemoved(position)
                    }
                }
            }
        }
    }
}
