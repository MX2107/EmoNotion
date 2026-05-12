package com.emonotion.app.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.emonotion.app.databinding.ItemNoteBinding
import com.emonotion.app.domain.model.Note
import java.text.SimpleDateFormat
import java.util.*

/**
 * Адаптер для списка заметок
 */
class NotesAdapter(
    private val onItemClick: (Note) -> Unit,
    private val onItemLongClick: (Note) -> Unit
) : ListAdapter<Note, NotesAdapter.NoteViewHolder>(NoteDiffCallback()) {

    class NoteViewHolder(
        private val binding: ItemNoteBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        
        fun bind(note: Note, onItemClick: (Note) -> Unit, onItemLongClick: (Note) -> Unit) {
            binding.apply {
                noteText.text = note.content
                noteTimestamp.text = dateFormat.format(Date(note.timestamp))
                
                // Обработчики кликов
                root.setOnClickListener { onItemClick(note) }
                deleteNoteButton.setOnClickListener { 
                    onItemLongClick(note)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClick, onItemLongClick)
    }

    private class NoteDiffCallback : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem == newItem
        }
    }
}
