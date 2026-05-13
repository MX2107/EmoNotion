package com.emonotion.app.presentation.dailyentry

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.emonotion.app.R
import com.emonotion.app.databinding.FragmentDailyEntryBinding
import com.emonotion.app.domain.model.MoodType
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlinx.coroutines.launch

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
            // Убрали всплывающее уведомление об открытии записи
        } ?: run {
            // Если дата не передана, используем сегодняшнюю дату
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            viewModel.setDate(today)
            // Данные для даты загрузятся автоматически через observeViewModel
        }
    }
    
    private fun setupUI() {
        binding.apply {
            // Кнопка сохранения
            saveButton.setOnClickListener {
                viewModel.saveMoodEntry()
                // navigateUp() вызывается только после успешного сохранения в observeViewModel
            }
            
            // Кнопка возврата
            cancelButton.setOnClickListener {
                findNavController().navigateUp()
            }
            
            // Кнопка удаления (только в режиме редактирования)
            deleteButton.setOnClickListener {
                showDeleteConfirmationDialog()
            }
            
            // Обработчики кликов для настроений
            moodTerrible.setOnClickListener {
                viewModel.selectMoodType(com.emonotion.app.domain.model.MoodType.TERRIBLE)
            }
            
            moodBad.setOnClickListener {
                viewModel.selectMoodType(com.emonotion.app.domain.model.MoodType.BAD)
            }
            
            moodNeutral.setOnClickListener {
                viewModel.selectMoodType(com.emonotion.app.domain.model.MoodType.NEUTRAL)
            }
            
            moodGood.setOnClickListener {
                viewModel.selectMoodType(com.emonotion.app.domain.model.MoodType.GOOD)
            }
            
            moodGreat.setOnClickListener {
                viewModel.selectMoodType(com.emonotion.app.domain.model.MoodType.GREAT)
            }
            
            // Обработчики кликов для эмоций
            binding.tagEmotionJoy.setOnClickListener {
                toggleEmotionTag("радость")
            }
            
            binding.tagEmotionAnxiety.setOnClickListener {
                toggleEmotionTag("тревога")
            }
            
            binding.tagEmotionCalm.setOnClickListener {
                toggleEmotionTag("спокойствие")
            }
            
            binding.tagEmotionSadness.setOnClickListener {
                toggleEmotionTag("грусть")
            }
            
            binding.tagEmotionExcitement.setOnClickListener {
                toggleEmotionTag("волнение")
            }
            
            binding.tagEmotionStress.setOnClickListener {
                toggleEmotionTag("стресс")
            }
            
            binding.tagEmotionGratitude.setOnClickListener {
                toggleEmotionTag("благодарность")
            }
            
            binding.tagEmotionLoneliness.setOnClickListener {
                toggleEmotionTag("одиночество")
            }
            
            // Обработчики кликов для активностей
            binding.tagActivityWork.setOnClickListener {
                toggleActivityTag("работа")
            }
            
            binding.tagActivitySport.setOnClickListener {
                toggleActivityTag("спорт")
            }
            
            binding.tagActivityFriends.setOnClickListener {
                toggleActivityTag("друзья")
            }
            
            binding.tagActivityRest.setOnClickListener {
                toggleActivityTag("отдых")
            }
            
            binding.tagActivityFamily.setOnClickListener {
                toggleActivityTag("семья")
            }
            
            binding.tagActivityHobby.setOnClickListener {
                toggleActivityTag("хобби")
            }
            
            binding.tagActivityMeditation.setOnClickListener {
                toggleActivityTag("медитация")
            }
            
            binding.tagActivityReading.setOnClickListener {
                toggleActivityTag("чтение")
            }
            
            // Кнопки добавления пользовательских настроений и активностей
            addEmotionButton.setOnClickListener {
                showAddEmotionDialog()
            }
            
            addActivityButton.setOnClickListener {
                showAddActivityDialog()
            }
            
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
            
            // Обработчик клика на дату
            binding.dateText.setOnClickListener {
                showDatePickerDialog()
            }
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selectedDate.collect { date ->
                // Обновляем отображение даты
                binding.dateText.text = formatDate(date)
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentMood.collect { mood ->
                updateUIForExistingMood(mood)
                // Показываем или скрываем кнопку удаления в зависимости от наличия записи
                binding.deleteButton.visibility = if (mood != null) View.VISIBLE else View.GONE
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selectedMoodType.collect { moodType ->
                updateMoodTypeSelection(moodType)
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.notes.collect { notes ->
                val currentText = binding.notesEditText.text.toString()
                if (currentText != notes) {
                    val cursorPosition = binding.notesEditText.selectionStart
                    binding.notesEditText.setText(notes)
                    binding.notesEditText.setSelection(cursorPosition.coerceAtMost(notes.length))
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.emotions.collect { emotions ->
                updateEmotionsUI(emotions)
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.activities.collect { activities ->
                updateActivitiesUI(activities)
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selectedIntensity.collect { intensity ->
                binding.intensitySlider.progress = intensity - 1
                binding.intensityText.text = "Интенсивность: $intensity/5"
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                // Обновляем состояние загрузки
                binding.saveButton.isEnabled = !isLoading
                if (isLoading) {
                    binding.saveButton.text = "Сохранение..."
                } else {
                    binding.saveButton.text = "Сохранить"
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.errorMessage.collect { error ->
                error?.let {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.customEmotions.collect { _ ->
                // Обновляем отображение пользовательских эмоций
                displayCustomEmotions()
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.customActivities.collect { _ ->
                // Обновляем отображение пользовательских активностей
                displayCustomActivities()
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.successMessage.collect { message ->
                message?.let {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                    
                    // Выходим из экрана только при сохранении записи, а не при добавлении элементов
                    if (it.contains("успешно сохранена") || it.contains("успешно удалена")) {
                        findNavController().navigateUp()
                        
                        // Календарь обновится автоматически через onResume при возвращении
                        android.util.Log.d("DailyEntryFragment", "Запись сохранена, календарь обновится при onResume")
                    }
                }
            }
        }
    }
    
    private fun toggleEmotionTag(emotion: String) {
        val currentEmotions = viewModel.emotions.value
        if (currentEmotions.contains(emotion)) {
            viewModel.removeEmotion(emotion)
        } else {
            viewModel.addEmotion(emotion)
        }
    }
    
    private fun toggleActivityTag(activity: String) {
        val currentActivities = viewModel.activities.value
        if (currentActivities.contains(activity)) {
            viewModel.removeActivity(activity)
        } else {
            viewModel.addActivity(activity)
        }
    }
    
    private fun updateEmotionsUI(emotions: List<String>) {
        binding.apply {
            // Предопределенные эмоции с улучшенным визуальным выделением
            updateTagAppearance(tagEmotionJoy, emotions.contains("радость"))
            updateTagAppearance(tagEmotionAnxiety, emotions.contains("тревога"))
            updateTagAppearance(tagEmotionCalm, emotions.contains("спокойствие"))
            updateTagAppearance(tagEmotionSadness, emotions.contains("грусть"))
            updateTagAppearance(tagEmotionExcitement, emotions.contains("волнение"))
            updateTagAppearance(tagEmotionStress, emotions.contains("стресс"))
            updateTagAppearance(tagEmotionGratitude, emotions.contains("благодарность"))
            updateTagAppearance(tagEmotionLoneliness, emotions.contains("одиночество"))
            
            // Отображаем пользовательские эмоции
            displayCustomEmotions()
        }
    }
    
    private fun updateActivitiesUI(activities: List<String>) {
        binding.apply {
            // Предопределенные активности с улучшенным визуальным выделением
            updateTagAppearance(tagActivityWork, activities.contains("работа"))
            updateTagAppearance(tagActivitySport, activities.contains("спорт"))
            updateTagAppearance(tagActivityFriends, activities.contains("друзья"))
            updateTagAppearance(tagActivityRest, activities.contains("отдых"))
            updateTagAppearance(tagActivityFamily, activities.contains("семья"))
            updateTagAppearance(tagActivityHobby, activities.contains("хобби"))
            updateTagAppearance(tagActivityMeditation, activities.contains("медитация"))
            updateTagAppearance(tagActivityReading, activities.contains("чтение"))
            
            // Отображаем пользовательские активности
            displayCustomActivities()
        }
    }
    
    /**
     * Обновляет внешний вид тега в зависимости от того, выбран он или нет
     */
    private fun updateTagAppearance(tagView: android.widget.TextView, isSelected: Boolean) {
        if (isSelected) {
            // Выбранный тег - более выразительный стиль
            tagView.alpha = 1.0f
            tagView.background = resources.getDrawable(R.drawable.save_button_background, null)
            tagView.setTextColor(resources.getColor(R.color.primary_foreground, null))
            tagView.textSize = 13f
            tagView.setPadding(
                (16 * resources.displayMetrics.density).toInt(), // 16dp
                (8 * resources.displayMetrics.density).toInt(),  // 8dp
                (16 * resources.displayMetrics.density).toInt(), // 16dp
                (8 * resources.displayMetrics.density).toInt()   // 8dp
            )
        } else {
            // Невыбранный тег - стандартный стиль
            tagView.alpha = 0.7f
            tagView.background = resources.getDrawable(R.drawable.tab_switcher_background, null)
            tagView.setTextColor(resources.getColor(R.color.foreground, null))
            tagView.textSize = 12f
            tagView.setPadding(
                (12 * resources.displayMetrics.density).toInt(), // 12dp
                (6 * resources.displayMetrics.density).toInt(),  // 6dp
                (12 * resources.displayMetrics.density).toInt(), // 12dp
                (6 * resources.displayMetrics.density).toInt()   // 6dp
            )
        }
    }
    
    private fun displayCustomEmotions() {
        val predefinedEmotions = setOf("радость", "тревога", "спокойствие", "грусть", "волнение", "стресс", "благодарность", "одиночество")
        val allCustomEmotions = viewModel.customEmotions.value
        
        // Получаем пользовательские эмоции, которые есть в списке доступных
        val customEmotions = allCustomEmotions.filter { it !in predefinedEmotions }
        
        // Отображаем пользовательские эмоции
        val emotionsContainer = binding.root.findViewById<android.widget.LinearLayout>(R.id.custom_emotions_container)
        emotionsContainer.removeAllViews()
        
        if (customEmotions.isNotEmpty()) {
            emotionsContainer.visibility = View.VISIBLE
            customEmotions.forEach { emotion ->
                val tagView = createCustomTag(emotion, isEmotion = true)
                emotionsContainer.addView(tagView)
            }
        } else {
            emotionsContainer.visibility = View.GONE
        }
    }
    
    private fun displayCustomActivities() {
        val predefinedActivities = setOf("работа", "спорт", "друзья", "отдых", "семья", "хобби", "медитация", "чтение")
        val allCustomActivities = viewModel.customActivities.value
        
        // Получаем пользовательские активности, которые есть в списке доступных
        val customActivities = allCustomActivities.filter { it !in predefinedActivities }
        
        // Отображаем пользовательские активности
        val activitiesContainer = binding.root.findViewById<android.widget.LinearLayout>(R.id.custom_activities_container)
        activitiesContainer.removeAllViews()
        
        if (customActivities.isNotEmpty()) {
            activitiesContainer.visibility = View.VISIBLE
            customActivities.forEach { activity ->
                val tagView = createCustomTag(activity, isEmotion = false)
                activitiesContainer.addView(tagView)
            }
        } else {
            activitiesContainer.visibility = View.GONE
        }
    }
    
    private fun createCustomTag(name: String, isEmotion: Boolean): android.widget.TextView {
        val tagView = android.widget.TextView(requireContext()).apply {
            text = name
            setOnClickListener { 
                if (isEmotion) {
                    toggleEmotionTag(name)
                } else {
                    toggleActivityTag(name)
                }
            }
            
            // Устанавливаем начальное состояние
            val isSelected = if (isEmotion) {
                viewModel.emotions.value.contains(name)
            } else {
                viewModel.activities.value.contains(name)
            }
            updateTagAppearance(this, isSelected)
            
            // Параметры layout
            val params = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.marginEnd = (8 * resources.displayMetrics.density).toInt() // 8dp
            params.bottomMargin = (4 * resources.displayMetrics.density).toInt() // 4dp
            layoutParams = params
        }
        return tagView
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
    
    private fun updateUIForExistingMood(mood: com.emonotion.app.domain.model.MoodEntry?) {
        mood?.let { 
            binding.apply {
                // Устанавливаем интенсивность
                intensitySlider.progress = it.intensity - 1
                intensityText.text = "интенсивность: ${it.intensity}/5"
                // Устанавливаем текст заметок, сохраняя позицию курсора
                val currentNotes = it.notes ?: ""
                if (binding.notesEditText.text.toString() != currentNotes) {
                    val cursorPosition = binding.notesEditText.selectionStart
                    binding.notesEditText.setText(currentNotes)
                    binding.notesEditText.setSelection(cursorPosition.coerceAtMost(currentNotes.length))
                }
                
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
    
    private fun showAddEmotionDialog() {
        val dialogView = android.view.LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_emotion, null)
        
        val emotionInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.emotion_input)
        
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        dialogView.findViewById<Button>(R.id.cancel_button).setOnClickListener {
            dialog.dismiss()
        }
        
        dialogView.findViewById<Button>(R.id.add_button).setOnClickListener {
            val emotionName = emotionInput.text.toString().trim()
            if (emotionName.isNotBlank()) {
                addCustomEmotion(emotionName)
                dialog.dismiss()
            } else {
                emotionInput.error = "Введите название эмоции"
            }
        }
        
        dialog.show()
        
        // Автоматически показываем клавиатуру
        emotionInput.requestFocus()
        val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.showSoftInput(emotionInput, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
    }
    
    private fun addCustomEmotion(name: String) {
        // Добавляем эмоцию через ViewModel
        viewModel.addCustomEmotion(name)
        android.util.Log.d("DailyEntryFragment", "Добавлена эмоция: $name")
    }
    
    private fun showAddActivityDialog() {
        val dialogView = android.view.LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_activity, null)
        
        val activityInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.activity_input)
        
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        dialogView.findViewById<Button>(R.id.cancel_button).setOnClickListener {
            dialog.dismiss()
        }
        
        dialogView.findViewById<Button>(R.id.add_button).setOnClickListener {
            val activityName = activityInput.text.toString().trim()
            if (activityName.isNotBlank()) {
                addCustomActivity(activityName)
                dialog.dismiss()
            } else {
                activityInput.error = "Введите название активности"
            }
        }
        
        dialog.show()
        
        // Автоматически показываем клавиатуру
        activityInput.requestFocus()
        val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.showSoftInput(activityInput, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
    }
    
    private fun addCustomActivity(name: String) {
        // Добавляем активность через ViewModel
        viewModel.addCustomActivity(name)
        android.util.Log.d("DailyEntryFragment", "Добавлена активность: $name")
    }
    
    private fun showDeleteConfirmationDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Удалить запись")
            .setMessage("Вы уверены, что хотите удалить эту запись о настроении?")
            .setPositiveButton("Удалить") { dialog, _ ->
                deleteMoodEntry()
                dialog.dismiss()
            }
            .setNegativeButton("Отмена") { dialog, _ -> dialog.dismiss() }
            .show()
    }
    
    private fun deleteMoodEntry() {
        viewModel.deleteCurrentMood()
    }
    
    private fun showDatePickerDialog() {
        val currentDate = viewModel.selectedDate.value
        val dateParts = currentDate.split("-")
        val year = dateParts[0].toInt()
        val month = dateParts[1].toInt() - 1 // Calendar.MONTH 0-11
        val day = dateParts[2].toInt()
        
        val datePickerDialog = android.app.DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val calendar = Calendar.getInstance()
                calendar.set(selectedYear, selectedMonth, selectedDay)
                val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
                viewModel.setDate(formattedDate)
            },
            year,
            month,
            day
        )
        
        datePickerDialog.show()
    }
    
    private fun formatDate(dateString: String): String {
        return try {
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = java.text.SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            date?.let { outputFormat.format(it) } ?: dateString
        } catch (e: Exception) {
            dateString
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
