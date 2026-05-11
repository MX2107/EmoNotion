package com.emonotion.app.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.emonotion.app.R

class ProfileFragment : Fragment() {
    
    private lateinit var avatarImage: ImageView
    private lateinit var nameText: TextView
    private lateinit var emailText: TextView
        private lateinit var editProfileButton: LinearLayout
    private lateinit var totalEntriesText: TextView
    private lateinit var currentStreakText: TextView
    private lateinit var longestStreakText: TextView
    private lateinit var averageMoodText: TextView
    private lateinit var analyticsButton: LinearLayout
    private lateinit var settingsButton: LinearLayout
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupClickListeners()
        updateUI()
    }
    
    private fun initViews(view: View) {
        try {
            avatarImage = view.findViewById(R.id.avatar_image)
            nameText = view.findViewById(R.id.name_text)
            emailText = view.findViewById(R.id.email_text)
                        editProfileButton = view.findViewById(R.id.edit_profile_button)
                        totalEntriesText = view.findViewById(R.id.total_entries_text)
            currentStreakText = view.findViewById(R.id.current_streak_text)
            longestStreakText = view.findViewById(R.id.longest_streak_text)
            averageMoodText = view.findViewById(R.id.average_mood_text)
            analyticsButton = view.findViewById(R.id.analytics_button)
            settingsButton = view.findViewById(R.id.settings_button)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun setupClickListeners() {
        // Кнопка редактирования профиля
        editProfileButton.setOnClickListener {
            // В будущем откроет диалог редактирования профиля
        }
        
                
        // Кнопка аналитики
        analyticsButton.setOnClickListener {
            findNavController().navigate(R.id.navigation_analytics)
        }
        
        // Кнопка настроек
        settingsButton.setOnClickListener {
            findNavController().navigate(R.id.navigation_settings)
        }
    }
    
    private fun updateUI() {
        try {
            // Реальное приложение будет загружать данные из базы данных
            // Пока показываем демо-данные
            showDemoData()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun showDemoData() {
        try {
            nameText.text = "Пользователь"
            emailText.text = "user@example.com"
            
            totalEntriesText.text = "0"
            currentStreakText.text = "0"
            longestStreakText.text = "0"
            averageMoodText.text = "0.0"
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
