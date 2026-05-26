package com.emonotion.app.presentation.settings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.emonotion.app.databinding.ItemCustomEmotionBinding
import com.emonotion.app.domain.model.CustomMood
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Adapter для отображения списка пользовательских эмоций
 */
class CustomEmotionsAdapter(
    private val onDeleteClick: (CustomMood) -> Unit
) : ListAdapter<CustomMood, CustomEmotionsAdapter.EmotionViewHolder>(EmotionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmotionViewHolder {
        val binding = ItemCustomEmotionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EmotionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EmotionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class EmotionViewHolder(
        private val binding: ItemCustomEmotionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(customMood: CustomMood) {
            binding.nameText.text = customMood.name
            
            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val createdDate = dateFormat.format(Date(customMood.createdAt))
            binding.dateText.text = "Создано: $createdDate"

            binding.deleteButton.setOnClickListener {
                onDeleteClick(customMood)
            }
        }
    }

    private class EmotionDiffCallback : DiffUtil.ItemCallback<CustomMood>() {
        override fun areItemsTheSame(oldItem: CustomMood, newItem: CustomMood): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CustomMood, newItem: CustomMood): Boolean {
            return oldItem == newItem
        }
    }
}
