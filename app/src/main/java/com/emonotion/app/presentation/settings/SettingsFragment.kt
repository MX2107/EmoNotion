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
                // Экспорт данных будет реализован через соответствующий use case в будущей версии
                Toast.makeText(requireContext(), "Экспорт данных будет реализован позже", Toast.LENGTH_SHORT).show()
            }
            
            // Дополнительные кнопки будут добавлены при расширении функционала
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.settings.collect { settings ->
                settings?.let {
                    // Обновление UI элементами настроек будет реализовано при добавлении в layout
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.errorMessage.collect { error ->
                error?.let {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun showTimePicker() {
        // Выбор времени напоминания будет реализован при добавлении соответствующих UI элементов
    }
    
    private fun showLanguageDialog() {
        // Диалог выбора языка будет реализован при добавлении соответствующих UI элементов
    }
    
    private fun showResetDialog() {
        // Диалог сброса данных будет реализован при добавлении соответствующих UI элементов
    }
    
    private fun showAboutDialog() {
        // Диалог о приложении будет реализован при добавлении соответствующих UI элементов
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
