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
import com.emonotion.app.databinding.FragmentCustomActivitiesBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Экран редактирования пользовательских активностей
 */
@AndroidEntryPoint
class CustomActivitiesFragment : Fragment() {

    private var _binding: FragmentCustomActivitiesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CustomActivitiesViewModel by viewModels()
    private lateinit var adapter: CustomActivitiesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomActivitiesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = CustomActivitiesAdapter(
            onDeleteClick = { customActivity ->
                showDeleteConfirmationDialog(customActivity)
            }
        )
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@CustomActivitiesFragment.adapter
        }
    }

    private fun setupListeners() {
        binding.addButton.setOnClickListener {
            showAddActivityDialog()
        }

        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.customActivities.collect { activities ->
                adapter.submitList(activities)
                updateEmptyState(activities.isEmpty())
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

    private fun showAddActivityDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_activity, null)

        val activityInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.activity_input)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogView.findViewById<android.widget.Button>(R.id.cancel_button).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<android.widget.Button>(R.id.add_button).setOnClickListener {
            val activityName = activityInput.text.toString().trim()
            if (activityName.isNotBlank()) {
                viewModel.addCustomActivity(activityName)
                dialog.dismiss()
            } else {
                activityInput.error = "Введите название активности"
            }
        }

        dialog.show()

        activityInput.requestFocus()
        val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.showSoftInput(activityInput, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
    }

    private fun showDeleteConfirmationDialog(customActivity: com.emonotion.app.domain.model.CustomActivity) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Удалить активность")
            .setMessage("Вы уверены, что хотите удалить активность \"${customActivity.name}\"?")
            .setPositiveButton("Удалить") { _, _ ->
                viewModel.deleteCustomActivity(customActivity)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
