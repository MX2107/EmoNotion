package com.emonotion.app.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.emonotion.app.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Главный экран приложения
 */
@AndroidEntryPoint
class HomeFragment : Fragment() {
    
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: HomeViewModel by viewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
        viewModel.loadHomeData()
    }
    
    private fun setupUI() {
        binding.apply {
            // Кнопка добавления настроения
            addMoodButton.setOnClickListener {
                findNavController().navigate(com.emonotion.app.R.id.dailyEntryFragment)
            }
            
            // Кнопка дневника
            diaryButton.setOnClickListener {
                findNavController().navigate(com.emonotion.app.R.id.dailyEntryFragment)
            }
            
            // Кнопка календаря
            calendarButton.setOnClickListener {
                findNavController().navigate(com.emonotion.app.R.id.navigation_calendar)
            }
            
            // Кнопка аналитики
            analyticsButton.setOnClickListener {
                findNavController().navigate(com.emonotion.app.R.id.action_homeFragment_to_analyticsFragment)
            }
            
            // Кнопка заметок
            notesButton.setOnClickListener {
                findNavController().navigate(com.emonotion.app.R.id.navigation_notes)
            }
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.todayMood.collect { mood ->
                updateMoodDisplay(mood)
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.todayNotes.collect { notes ->
                updateNotesDisplay(notes)
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.todayTasks.collect { tasks ->
                updateTasksDisplay(tasks)
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.incompleteTasksCount.collect { count ->
                // Обновляем счетчик задач (можно добавить в layout)
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                // Обновляем состояние загрузки (можно добавить ProgressBar в layout)
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.errorMessage.collect { error ->
                error?.let {
                    // Показать ошибку (можно использовать Snackbar)
                }
            }
        }
    }
    
    private fun updateMoodDisplay(mood: com.emonotion.app.domain.model.MoodEntry?) {
        binding.apply {
            if (mood != null) {
                // Показываем карточку настроения
                moodCard.visibility = View.VISIBLE
                addMoodButton.visibility = View.GONE
                moodText.text = mood.mood.name
                // Можно добавить эмодзи в зависимости от настроения
            } else {
                // Показываем кнопку добавления настроения
                moodCard.visibility = View.GONE
                addMoodButton.visibility = View.VISIBLE
            }
        }
    }
    
    private fun updateNotesDisplay(notes: List<com.emonotion.app.domain.model.Note>) {
        // В текущем layout есть статические заметки, можно обновлять их динамически
        // TODO: Реализовать динамическое отображение заметок
    }
    
    private fun updateTasksDisplay(tasks: List<com.emonotion.app.domain.model.Task>) {
        // TODO: Реализовать отображение задач если нужно
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
