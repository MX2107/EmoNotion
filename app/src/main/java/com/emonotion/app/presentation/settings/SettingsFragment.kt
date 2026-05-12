package com.emonotion.app.presentation.settings

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.emonotion.app.databinding.FragmentSettingsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Экран настроек приложения
 */
@AndroidEntryPoint
class SettingsFragment : Fragment() {
    
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: SettingsViewModel by viewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
        viewModel.loadSettings()
    }
    
    private fun setupUI() {
        binding.apply {
            // Кнопка экспорта данных
            exportDataButton.setOnClickListener {
                // TODO: Экспорт данных
            }
            
            // TODO: Добавить кнопку редактирования профиля когда будет в layout
            
            // TODO: Добавить остальные кнопки когда будут готовы в layout
        }
    }
    
    private fun observeViewModel() {
        // TODO: Обработать состояние настроек когда будут готовы UI элементы
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.errorMessage.collect { error ->
                error?.let {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun showTimePicker() {
        // TODO: Показать выбор времени когда будут готовы UI элементы
    }
    
    private fun showLanguageDialog() {
        // TODO: Показать диалог выбора языка когда будут готовы UI элементы
    }
    
    private fun showResetDialog() {
        // TODO: Показать диалог сброса когда будут готовы UI элементы
    }
    
    private fun showAboutDialog() {
        // TODO: Показать диалог о приложении когда будут готовы UI элементы
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
