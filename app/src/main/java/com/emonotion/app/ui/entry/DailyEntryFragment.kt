package com.emonotion.app.ui.entry

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.emonotion.app.R

class DailyEntryFragment : Fragment() {
    
    private lateinit var selectedMood: String
    private var intensity: Int = 3
    private val selectedEmotions = mutableListOf<String>()
    private val selectedActivities = mutableListOf<String>()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_daily_entry, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupMoodSelection(view)
        setupIntensitySlider(view)
        setupEmotionTags(view)
        setupActivityTags(view)
        setupButtons(view)
    }
    
    private fun setupMoodSelection(view: View) {
        val moodButtons = listOf(
            Pair(R.id.mood_terrible, "terrible"),
            Pair(R.id.mood_bad, "bad"),
            Pair(R.id.mood_neutral, "neutral"),
            Pair(R.id.mood_good, "good"),
            Pair(R.id.mood_great, "great")
        )
        
        moodButtons.forEach { (buttonId, mood) ->
            view.findViewById<LinearLayout>(buttonId)?.setOnClickListener {
                selectedMood = mood
                updateMoodSelection(view, mood)
            }
            
            // Устанавливаем эмодзи для каждой кнопки
            val emojiTextView = view.findViewById<LinearLayout>(buttonId)?.getChildAt(0) as? TextView
            emojiTextView?.text = getMoodEmoji(mood)
        }
    }
    
    private fun getMoodEmoji(mood: String): String {
        return when (mood) {
            "great" -> "😄"
            "good" -> "🙂"
            "neutral" -> "😐"
            "bad" -> "😔"
            "terrible" -> "😢"
            else -> "😐"
        }
    }
    
    private fun updateMoodSelection(view: View, selectedMood: String) {
        val moodButtons = listOf(
            R.id.mood_terrible, R.id.mood_bad, R.id.mood_neutral,
            R.id.mood_good, R.id.mood_great
        )
        
        moodButtons.forEach { buttonId ->
            val button = view.findViewById<LinearLayout>(buttonId)
            val isSelected = when (buttonId) {
                R.id.mood_terrible -> selectedMood == "terrible"
                R.id.mood_bad -> selectedMood == "bad"
                R.id.mood_neutral -> selectedMood == "neutral"
                R.id.mood_good -> selectedMood == "good"
                R.id.mood_great -> selectedMood == "great"
                else -> false
            }
            
            button?.alpha = if (isSelected) 1.0f else 0.5f
            button?.background?.setTint(
                resources.getColor(
                    if (isSelected) getMoodColor(selectedMood) 
                    else R.color.muted
                , null)
            )
        }
    }
    
    private fun getMoodColor(mood: String): Int {
        return when (mood) {
            "great" -> R.color.mood_great
            "good" -> R.color.mood_good
            "neutral" -> R.color.mood_neutral
            "bad" -> R.color.mood_bad
            "terrible" -> R.color.mood_terrible
            else -> R.color.mood_neutral
        }
    }
    
    private fun setupIntensitySlider(view: View) {
        val intensitySlider = view.findViewById<SeekBar>(R.id.intensity_slider)
        val intensityText = view.findViewById<TextView>(R.id.intensity_text)
        
        intensitySlider?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                intensity = progress + 1
                intensityText?.text = "Интенсивность: $intensity/5"
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        intensitySlider?.progress = 2 // По умолчанию 3
        intensityText?.text = "Интенсивность: 3/5"
    }
    
    private fun setupEmotionTags(view: View) {
        val emotionTags = listOf("радость", "тревога", "спокойствие", "грусть", "волнение", "стресс", "благодарность", "одиночество")
        
        emotionTags.forEach { emotion ->
            val tagId = resources.getIdentifier("tag_emotion_$emotion", "id", requireContext().packageName)
            view.findViewById<TextView>(tagId)?.setOnClickListener {
                toggleEmotion(emotion, it as TextView)
            }
        }
    }
    
    private fun toggleEmotion(emotion: String, textView: TextView) {
        if (selectedEmotions.contains(emotion)) {
            selectedEmotions.remove(emotion)
            textView.background.setTint(resources.getColor(R.color.muted, null))
            textView.setTextColor(resources.getColor(R.color.muted_foreground, null))
        } else {
            selectedEmotions.add(emotion)
            textView.background.setTint(resources.getColor(R.color.primary, null))
            textView.setTextColor(resources.getColor(R.color.primary_foreground, null))
        }
    }
    
    private fun setupActivityTags(view: View) {
        val activityTags = listOf("работа", "спорт", "друзья", "отдых", "семья", "хобби", "медитация", "чтение")
        
        activityTags.forEach { activity ->
            val tagId = resources.getIdentifier("tag_activity_$activity", "id", requireContext().packageName)
            view.findViewById<TextView>(tagId)?.setOnClickListener {
                toggleActivity(activity, it as TextView)
            }
        }
    }
    
    private fun toggleActivity(activity: String, textView: TextView) {
        if (selectedActivities.contains(activity)) {
            selectedActivities.remove(activity)
            textView.background.setTint(resources.getColor(R.color.muted, null))
            textView.setTextColor(resources.getColor(R.color.muted_foreground, null))
        } else {
            selectedActivities.add(activity)
            textView.background.setTint(resources.getColor(R.color.secondary, null))
            textView.setTextColor(resources.getColor(R.color.secondary_foreground, null))
        }
    }
    
    private fun setupButtons(view: View) {
        view.findViewById<Button>(R.id.save_button)?.setOnClickListener {
            saveEntry()
        }
        
        view.findViewById<Button>(R.id.cancel_button)?.setOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun saveEntry() {
        if (::selectedMood.isInitialized) {
            // Здесь должна быть логика сохранения в базу данных
            // Пока просто возвращаемся на главный экран
            findNavController().navigate(R.id.navigation_home)
        } else {
            Toast.makeText(requireContext(), "Пожалуйста, выберите настроение", Toast.LENGTH_SHORT).show()
        }
    }
}
