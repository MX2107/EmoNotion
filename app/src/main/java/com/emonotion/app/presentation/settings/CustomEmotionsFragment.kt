package com.emonotion.app.presentation.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.emonotion.app.R
import com.emonotion.app.databinding.FragmentCustomEmotionsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Экран редактирования пользовательских эмоций
 */
@AndroidEntryPoint
class CustomEmotionsFragment : Fragment() {

    private var _binding: FragmentCustomEmotionsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CustomEmotionsViewModel by viewModels()
    private lateinit var adapter: CustomEmotionsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomEmotionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = CustomEmotionsAdapter(
            onDeleteClick = { customMood ->
                showDeleteConfirmationDialog(customMood)
            },
            onEditClick = { customMood ->
                showEditEmotionDialog(customMood)
            }
        )
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@CustomEmotionsFragment.adapter
        }
    }

    private fun setupListeners() {
        binding.addButton.setOnClickListener {
            showAddEmotionDialog()
        }

        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.customMoods.collect { moods ->
                adapter.submitList(moods)
                updateEmptyState(moods.isEmpty())
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.errorMessage.collect { error ->
                error?.let {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                    viewModel.clearError()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.successMessage.collect { message ->
                message?.let {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                    viewModel.clearSuccess()
                }
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun showAddEmotionDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_emotion, null)

        val emotionInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.emotion_input)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogView.findViewById<android.widget.Button>(R.id.cancel_button).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<android.widget.Button>(R.id.add_button).setOnClickListener {
            val emotionName = emotionInput.text.toString().trim()
            if (emotionName.isNotBlank()) {
                viewModel.addCustomEmotion(emotionName)
                dialog.dismiss()
            } else {
                emotionInput.error = "Введите название эмоции"
            }
        }

        dialog.show()

        emotionInput.requestFocus()
        val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.showSoftInput(emotionInput, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
    }

    private fun showEditEmotionDialog(customMood: com.emonotion.app.domain.model.CustomMood) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_emotion, null)

        val emotionInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.emotion_input)
        emotionInput.setText(customMood.name)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Редактировать эмоцию")
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogView.findViewById<android.widget.Button>(R.id.cancel_button).setOnClickListener {
            dialog.dismiss()
        }

        val addButton = dialogView.findViewById<android.widget.Button>(R.id.add_button)
        addButton.text = "Сохранить"
        addButton.setOnClickListener {
            val emotionName = emotionInput.text.toString().trim()
            if (emotionName.isNotBlank()) {
                viewModel.updateCustomEmotion(customMood.copy(name = emotionName))
                dialog.dismiss()
            } else {
                emotionInput.error = "Введите название эмоции"
            }
        }

        dialog.show()

        emotionInput.requestFocus()
        emotionInput.setSelection(emotionInput.text?.length ?: 0)
        val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.showSoftInput(emotionInput, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
    }

    private fun showDeleteConfirmationDialog(customMood: com.emonotion.app.domain.model.CustomMood) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Удалить эмоцию")
            .setMessage("Вы уверены, что хотите удалить эмоцию \"${customMood.name}\"?")
            .setPositiveButton("Удалить") { _, _ ->
                viewModel.deleteCustomMood(customMood)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
