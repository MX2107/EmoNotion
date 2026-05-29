package com.emonotion.app.presentation.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.emonotion.app.databinding.ItemMoodEntryBinding
import com.emonotion.app.domain.model.MoodEntry
import com.emonotion.app.domain.model.MoodType
import java.text.SimpleDateFormat
import java.util.*

/**
 * Адаптер для списка записей о настроении
 */
class MoodEntriesAdapter(
    private val onItemClick: (MoodEntry) -> Unit,
    private val onItemLongClick: (MoodEntry) -> Unit
) : ListAdapter<MoodEntry, MoodEntriesAdapter.MoodViewHolder>(MoodDiffCallback()) {

    class MoodViewHolder(private val binding: ItemMoodEntryBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(mood: MoodEntry, onItemClick: (MoodEntry) -> Unit, onItemLongClick: (MoodEntry) -> Unit) {
            binding.apply {
                tvMoodType.text = mood.mood?.displayName ?: "Не указано"
                tvIntensity.text = "${mood.intensity}/5"
                tvDate.text = mood.date
                tvActivities.text = mood.activities.joinToString(", ")
                tvNotes.text = mood.notes ?: ""
                tvNotes.visibility = if (mood.notes != null) View.VISIBLE else View.GONE

                // Цветовая индикация настроения
                cardMood.setCardBackgroundColor(getMoodColor(mood.mood))

                root.setOnClickListener { onItemClick(mood) }
                root.setOnLongClickListener {
                    onItemLongClick(mood)
                    true
                }
            }
        }

        private fun getMoodColor(mood: MoodType?): Int {
            return when (mood) {
                MoodType.HAPPY -> Color.parseColor("#4CAF50")
                MoodType.SAD -> Color.parseColor("#2196F3")
                MoodType.ANGRY -> Color.parseColor("#F44336")
                MoodType.ANXIOUS -> Color.parseColor("#FF9800")
                MoodType.NEUTRAL -> Color.parseColor("#9E9E9E")
                MoodType.GREAT -> Color.parseColor("#4CAF50")
                MoodType.GOOD -> Color.parseColor("#8BC34A")
                MoodType.BAD -> Color.parseColor("#F44336")
                MoodType.TERRIBLE -> Color.parseColor("#D32F2F")
                null -> Color.parseColor("#9E9E9E")
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val binding = ItemMoodEntryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MoodViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClick, onItemLongClick)
    }
    
    private class MoodDiffCallback : DiffUtil.ItemCallback<MoodEntry>() {
        override fun areItemsTheSame(oldItem: MoodEntry, newItem: MoodEntry): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MoodEntry, newItem: MoodEntry): Boolean {
            return oldItem == newItem
        }
    }
}
