package com.emonotion.app.presentation.settings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.emonotion.app.databinding.ItemCustomTagBinding
import com.emonotion.app.domain.model.CustomTag
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Adapter для отображения списка пользовательских тегов
 */
class CustomTagsAdapter(
    private val onDeleteClick: (CustomTag) -> Unit
) : ListAdapter<CustomTag, CustomTagsAdapter.TagViewHolder>(TagDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        val binding = ItemCustomTagBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TagViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TagViewHolder(
        private val binding: ItemCustomTagBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(customTag: CustomTag) {
            binding.nameText.text = customTag.name
            
            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val createdDate = dateFormat.format(Date(customTag.createdAt))
            binding.dateText.text = "Создано: $createdDate"

            binding.deleteButton.setOnClickListener {
                onDeleteClick(customTag)
            }
        }
    }

    private class TagDiffCallback : DiffUtil.ItemCallback<CustomTag>() {
        override fun areItemsTheSame(oldItem: CustomTag, newItem: CustomTag): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CustomTag, newItem: CustomTag): Boolean {
            return oldItem == newItem
        }
    }
}
