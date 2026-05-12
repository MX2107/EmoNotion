package com.emonotion.app.presentation.dailyentry

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.emonotion.app.databinding.FragmentDailyEntryBinding
import com.emonotion.app.domain.model.MoodType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Экран дневной записи о настроении
 */
@AndroidEntryPoint
class DailyEntryFragment : Fragment() {
    
    private var _binding: FragmentDailyEntryBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: DailyEntryViewModel by viewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDailyEntryBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
        
        // Получаем дату из аргументов навигации
        arguments?.getString("date")?.let { date ->
            viewModel.setDate(date)
        } ?: run {
            viewModel.setDate(viewModel.selectedDate.value)
        }
    }
    
    private fun setupUI() {
        binding.apply {
            // Кнопка сохранения
            saveButton.setOnClickListener {
                viewModel.saveMoodEntry()
            }
            
            // Кнопка возврата
            cancelButton.setOnClickListener {
                findNavController().navigateUp()
            }
            
            // TODO: Добавить функциональность для активностей и сброса
            
            // Настройка выбора интенсивности
            intensitySlider.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        viewModel.setIntensity(progress + 1)
                    }
                }
                
                override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
            })
            
            // Настройка заметок
            notesEditText.addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: android.text.Editable?) {
                    viewModel.setNotes(s?.toString() ?: "")
                }
            })
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selectedDate.collect { date ->
                // TODO: Обновить отображение даты
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentMood.collect { mood ->
                updateUIForExistingMood(mood)
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selectedMoodType.collect { moodType ->
                updateMoodTypeSelection(moodType)
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.notes.collect { notes ->
                binding.notesEditText.setText(notes)
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                // TODO: Обновить состояние загрузки
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.errorMessage.collect { error ->
                error?.let {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                    viewModel.clearError()
                }
            }
        }
        
        // TODO: Обработать сохранение записи
    }
    
    private fun updateMoodTypeSelection(selectedMood: MoodType?) {
        binding.apply {
            moodTerrible.alpha = if (selectedMood == MoodType.TERRIBLE) 1.0f else 0.5f
            moodBad.alpha = if (selectedMood == MoodType.BAD) 1.0f else 0.5f
            moodNeutral.alpha = if (selectedMood == MoodType.NEUTRAL) 1.0f else 0.5f
            moodGood.alpha = if (selectedMood == MoodType.GOOD) 1.0f else 0.5f
            moodGreat.alpha = if (selectedMood == MoodType.GREAT) 1.0f else 0.5f
        }
    }
    
    private fun updateActivitiesList(activities: List<String>) {
        // TODO: Реализовать отображение активностей
    }
    
    private fun updateUIForExistingMood(mood: com.emonotion.app.domain.model.MoodEntry?) {
        mood?.let { 
            binding.apply {
                // Устанавливаем интенсивность
                intensitySlider.progress = it.intensity - 1
                intensityText.text = "интенсивность: ${it.intensity}/5"
                notesEditText.setText(it.notes ?: "")
                
                // Выбираем нужное настроение
                when (it.mood) {
                    MoodType.TERRIBLE -> moodTerrible.performClick()
                    MoodType.BAD -> moodBad.performClick()
                    MoodType.NEUTRAL -> moodNeutral.performClick()
                    MoodType.GOOD -> moodGood.performClick()
                    MoodType.GREAT -> moodGreat.performClick()
                    MoodType.HAPPY -> moodGreat.performClick()
                    MoodType.SAD -> moodBad.performClick()
                    MoodType.ANGRY -> moodTerrible.performClick()
                    MoodType.ANXIOUS -> moodBad.performClick()
                }
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
