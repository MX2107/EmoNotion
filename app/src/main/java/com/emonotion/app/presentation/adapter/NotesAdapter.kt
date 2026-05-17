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
                noteTimestamp.text = dateFormat.format(Date(note.timestamp))
                
                // Название заметки
                if (note.title.isNotBlank()) {
                    noteTitle.text = note.title
                    noteTitle.visibility = View.VISIBLE
                } else {
                    noteTitle.visibility = View.GONE
                }
                
                // Содержимое заметки (превью)
                noteText.text = note.content
                
                // Теги
                if (note.tags.isNotEmpty()) {
                    noteTagsContainer.visibility = View.VISIBLE
                    val workTag = binding.root.context.getString(com.emonotion.app.R.string.tag_work)
                    val personalTag = binding.root.context.getString(com.emonotion.app.R.string.tag_personal)
                    val ideaTag = binding.root.context.getString(com.emonotion.app.R.string.tag_idea)
                    
                    binding.noteTagWork.visibility = if (note.tags.contains(workTag)) View.VISIBLE else View.GONE
                    binding.noteTagPersonal.visibility = if (note.tags.contains(personalTag)) View.VISIBLE else View.GONE
                    binding.noteTagIdea.visibility = if (note.tags.contains(ideaTag)) View.VISIBLE else View.GONE
                    
                    // Отображение кастомных тегов
                    val customTags = note.tags.filter { 
                        it != workTag && it != personalTag && it != ideaTag 
                    }
                    
                    // Удаляем старые кастомные теги из контейнера (если есть)
                    for (i in noteTagsContainer.childCount - 1 downTo 3) {
                        noteTagsContainer.removeViewAt(i)
                    }
                    
                    // Добавляем кастомные теги
                    customTags.forEach { tagName ->
                        val tagView = android.widget.TextView(binding.root.context).apply {
                            text = tagName
                            background = binding.root.context.getDrawable(com.emonotion.app.R.drawable.tag_purple_background)
                            setPadding(
                                (10 * binding.root.context.resources.displayMetrics.density).toInt(),
                                (4 * binding.root.context.resources.displayMetrics.density).toInt(),
                                (10 * binding.root.context.resources.displayMetrics.density).toInt(),
                                (4 * binding.root.context.resources.displayMetrics.density).toInt()
                            )
                            setTextColor(binding.root.context.getColor(com.emonotion.app.R.color.white))
                            textSize = 10f
                            setTypeface(null, android.graphics.Typeface.BOLD)
                            
                            val params = android.widget.LinearLayout.LayoutParams(
                                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            params.marginEnd = (6 * binding.root.context.resources.displayMetrics.density).toInt()
                            layoutParams = params
                        }
                        noteTagsContainer.addView(tagView)
                    }
                } else {
                    noteTagsContainer.visibility = View.GONE
                }
                
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
