package com.emonotion.app.presentation.settings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.emonotion.app.databinding.ItemCustomActivityBinding
import com.emonotion.app.domain.model.CustomActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Adapter для отображения списка пользовательских активностей
 */
class CustomActivitiesAdapter(
    private val onDeleteClick: (CustomActivity) -> Unit
) : ListAdapter<CustomActivity, CustomActivitiesAdapter.ActivityViewHolder>(ActivityDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val binding = ItemCustomActivityBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ActivityViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ActivityViewHolder(
        private val binding: ItemCustomActivityBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(customActivity: CustomActivity) {
            binding.nameText.text = customActivity.name
            
            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val createdDate = dateFormat.format(Date(customActivity.createdAt))
            binding.dateText.text = "Создано: $createdDate"

            binding.deleteButton.setOnClickListener {
                onDeleteClick(customActivity)
            }
        }
    }

    private class ActivityDiffCallback : DiffUtil.ItemCallback<CustomActivity>() {
        override fun areItemsTheSame(oldItem: CustomActivity, newItem: CustomActivity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CustomActivity, newItem: CustomActivity): Boolean {
            return oldItem == newItem
        }
    }
}
