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
import com.emonotion.app.databinding.FragmentNotesBinding
import com.emonotion.app.domain.model.Note
import com.emonotion.app.presentation.adapter.NotesAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Экран заметок
 */
@AndroidEntryPoint
class NotesFragment : Fragment() {
    
    private var _binding: FragmentNotesBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: NotesViewModel by viewModels()
    private lateinit var notesAdapter: NotesAdapter
    
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
    }
    
    private fun setupRecyclerView() {
        notesAdapter = NotesAdapter(
            onItemClick = { note ->
                // TODO: Навигация к детальной информации заметки
            },
            onItemLongClick = { note ->
                showNoteOptionsDialog(note)
            }
        )
        
        // TODO: Настроить RecyclerView когда будет готов layout
    }
    
    private fun setupUI() {
        binding.apply {
            // Кнопка добавления заметки
            addNoteButton.setOnClickListener {
                showAddNoteDialog()
            }
            
            // Кнопки вкладок
            notesTab.setOnClickListener {
                // TODO: Переключиться на вкладку заметок
            }
            
            tasksTab.setOnClickListener {
                // TODO: Переключиться на вкладку задач
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
        
        // TODO: Обработать showAllNotes и isLoading когда будет готов UI
    }
    
    private fun updateEmptyState(isEmpty: Boolean) {
        // TODO: Обновить UI когда будет готов layout
    }
    
    private fun showAddNoteDialog() {
        // TODO: Показать диалог добавления заметки
    }
    
    private fun showNoteOptionsDialog(note: Note) {
        // TODO: Показать диалог опций заметки
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
