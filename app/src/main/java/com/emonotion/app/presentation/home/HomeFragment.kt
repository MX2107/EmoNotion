package com.emonotion.app.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.emonotion.app.R
import com.emonotion.app.databinding.FragmentHomeBinding
import com.emonotion.app.utils.StreakUiHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * Главный экран приложения
 */
@AndroidEntryPoint
class HomeFragment : Fragment() {
    
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var notesAdapter: com.emonotion.app.presentation.adapter.NotesAdapter
    
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
        setupRecyclerView()
        setupUI()
        observeViewModel()
        viewModel.loadHomeData()
    }
    
    private fun setupRecyclerView() {
        notesAdapter = com.emonotion.app.presentation.adapter.NotesAdapter(
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
    }
    
    private fun setupUI() {
        binding.apply {
            // Кнопка добавления настроения (только для создания новой записи)
            addMoodButton.setOnClickListener {
                val bundle = Bundle().apply {
                    putString("date", viewModel.today)
                }
                findNavController().navigate(com.emonotion.app.R.id.dailyEntryFragment, bundle)
            }
            
            // Кнопка дневника
            diaryButton.setOnClickListener {
                val bundle = Bundle().apply {
                    putString("date", viewModel.today)
                }
                findNavController().navigate(com.emonotion.app.R.id.dailyEntryFragment, bundle)
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
            viewModel.userStats.collect { stats ->
                updateStreakDisplay(stats)
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.incompleteTasksCount.collect { _ ->
                // Обновляем счетчик задач (можно добавить в layout)
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { _ ->
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
                moodText.text = mood.mood.displayName
                
                // Устанавливаем эмодзи в зависимости от настроения
                val emoji = when (mood.mood) {
                    com.emonotion.app.domain.model.MoodType.GREAT -> "😄"
                    com.emonotion.app.domain.model.MoodType.GOOD -> "😊"
                    com.emonotion.app.domain.model.MoodType.NEUTRAL -> "😐"
                    com.emonotion.app.domain.model.MoodType.BAD -> "😕"
                    com.emonotion.app.domain.model.MoodType.TERRIBLE -> "😢"
                    com.emonotion.app.domain.model.MoodType.HAPPY -> "😄"
                    com.emonotion.app.domain.model.MoodType.SAD -> "😢"
                    com.emonotion.app.domain.model.MoodType.ANGRY -> "😠"
                    com.emonotion.app.domain.model.MoodType.ANXIOUS -> "😰"
                }
                moodEmoji.text = emoji
                
                // Добавляем обработчик клика на карточку настроения
                moodCard.setOnClickListener {
                    val bundle = Bundle().apply {
                        putString("date", mood.date)
                    }
                    findNavController().navigate(com.emonotion.app.R.id.dailyEntryFragment, bundle)
                }
            } else {
                // Показываем кнопку добавления настроения
                moodCard.visibility = View.GONE
                addMoodButton.visibility = View.VISIBLE
            }
        }
    }
    
    private fun updateNotesDisplay(notes: List<com.emonotion.app.domain.model.Note>) {
        notesAdapter.submitList(notes.take(3)) // Показываем только последние 3 заметки
    }
    
    private fun updateTasksDisplay(tasks: List<com.emonotion.app.domain.model.Task>) {
        // Отображение задач будет реализовано в будущей версии
        android.util.Log.d("HomeFragment", "Получено задач: ${tasks.size}")
    }
    
    private fun updateStreakDisplay(stats: com.emonotion.app.domain.model.UserStats) {
        android.util.Log.d(
            "HomeFragment",
            "updateStreakDisplay: streak=${stats.currentStreak}, hasEntryToday=${stats.hasEntryToday}"
        )
        val streakContainer = binding.root.findViewById<android.widget.LinearLayout>(R.id.streak_container)
        val streakText = binding.root.findViewById<android.widget.TextView>(R.id.streak_text)
        val streakFlame = binding.root.findViewById<android.widget.ImageView>(R.id.streak_flame_icon)

        if (!StreakUiHelper.shouldShowStreakBadge(stats)) {
            streakContainer?.visibility = View.GONE
            return
        }

        streakContainer?.visibility = View.VISIBLE
        streakText?.text = stats.currentStreak.toString()
        val activeToday = StreakUiHelper.isStreakActiveToday(stats)
        StreakUiHelper.applyHomeStreak(streakContainer, streakFlame, streakText, activeToday)
    }
    
    private fun deleteNoteDirectly(note: com.emonotion.app.domain.model.Note) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Удалить заметку?")
            .setMessage("Вы уверены, что хотите удалить эту заметку?")
            .setPositiveButton("Удалить") { _, _ ->
                viewModel.deleteNote(note.id)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showNoteDetail(note: com.emonotion.app.domain.model.Note) {
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

        val tagsView = dialogView.findViewById<android.widget.TextView>(R.id.note_tags)
        if (note.tags.isNotEmpty()) {
            tagsView.text = note.tags.joinToString(", ")
            tagsView.visibility = View.VISIBLE
        } else {
            tagsView.visibility = View.GONE
        }

        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView as View)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogView.findViewById<android.widget.Button>(R.id.edit_button).setOnClickListener {
            val bundle = Bundle().apply {
                putString("noteId", note.id)
            }
            findNavController().navigate(com.emonotion.app.R.id.navigation_notes, bundle)
            dialog.dismiss()
        }

        dialogView.findViewById<android.widget.Button>(R.id.close_button).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
        
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
