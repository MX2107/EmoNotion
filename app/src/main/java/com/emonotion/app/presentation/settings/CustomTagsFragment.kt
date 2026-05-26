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
import com.emonotion.app.R
import com.emonotion.app.databinding.FragmentCustomTagsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Экран редактирования пользовательских тегов
 */
@AndroidEntryPoint
class CustomTagsFragment : Fragment() {

    private var _binding: FragmentCustomTagsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CustomTagsViewModel by viewModels()
    private lateinit var adapter: CustomTagsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomTagsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = CustomTagsAdapter(
            onDeleteClick = { customTag ->
                showDeleteConfirmationDialog(customTag)
            }
        )
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@CustomTagsFragment.adapter
        }
    }

    private fun setupListeners() {
        binding.addButton.setOnClickListener {
            showAddTagDialog()
        }

        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.customTags.collect { tags ->
                adapter.submitList(tags)
                updateEmptyState(tags.isEmpty())
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

    private fun showAddTagDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_tag, null)

        val tagInput = dialogView.findViewById<TextInputEditText>(R.id.tag_input)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogView.findViewById<android.widget.Button>(R.id.cancel_button).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<android.widget.Button>(R.id.add_button).setOnClickListener {
            val tagName = tagInput.text.toString().trim()
            if (tagName.isNotBlank()) {
                viewModel.addCustomTag(tagName)
                dialog.dismiss()
            } else {
                tagInput.error = "Введите название тега"
            }
        }

        dialog.show()

        tagInput.requestFocus()
        val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.showSoftInput(tagInput, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
    }

    private fun showDeleteConfirmationDialog(customTag: com.emonotion.app.domain.model.CustomTag) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Удалить тег")
            .setMessage("Вы уверены, что хотите удалить тег \"${customTag.name}\"?")
            .setPositiveButton("Удалить") { _, _ ->
                viewModel.deleteCustomTag(customTag)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
