package com.emonotion.app.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.emonotion.app.R

class HomeFragment : Fragment() {
    
    private lateinit var streakContainer: LinearLayout
    private lateinit var streakText: TextView
    private lateinit var moodCard: LinearLayout
    private lateinit var moodText: TextView
    private lateinit var moodEmoji: TextView
    private lateinit var addMoodButton: LinearLayout
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupClickListeners()
        updateUI()
    }
    
    private fun initViews(view: View) {
        try {
            streakContainer = view.findViewById(R.id.streak_container)
            streakText = view.findViewById(R.id.streak_text)
            moodCard = view.findViewById(R.id.mood_card)
            moodText = view.findViewById(R.id.mood_text)
            moodEmoji = view.findViewById(R.id.mood_emoji)
            addMoodButton = view.findViewById(R.id.add_mood_button)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun setupClickListeners() {
        // Кнопка добавления настроения
        addMoodButton.setOnClickListener {
            findNavController().navigate(R.id.navigation_entry)
        }
        
        // Быстрые действия
        view?.findViewById<LinearLayout>(R.id.diary_button)?.setOnClickListener {
            findNavController().navigate(R.id.navigation_entry)
        }
        
        view?.findViewById<LinearLayout>(R.id.calendar_button)?.setOnClickListener {
            findNavController().navigate(R.id.navigation_calendar)
        }
        
        view?.findViewById<LinearLayout>(R.id.analytics_button)?.setOnClickListener {
            findNavController().navigate(R.id.navigation_analytics)
        }
        
        view?.findViewById<LinearLayout>(R.id.notes_button)?.setOnClickListener {
            findNavController().navigate(R.id.navigation_notes)
        }
    }
    
    private fun updateUI() {
        try {
            // Реальное приложение будет загружать данные из базы данных
            // Пока показываем пустое состояние
            showAddMoodButton()
            
            // Скрываем streak, так как нет данных
            streakContainer.visibility = View.GONE
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun showAddMoodButton() {
        try {
            moodCard.visibility = View.GONE
            addMoodButton.visibility = View.VISIBLE
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
