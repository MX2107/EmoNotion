package com.emonotion.app.presentation.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.emonotion.app.databinding.FragmentEditProfileBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Экран редактирования профиля
 */
@AndroidEntryPoint
class EditProfileFragment : Fragment() {
    
    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ProfileViewModel by viewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
        viewModel.startEditing()
    }
    
    private fun setupUI() {
        binding.apply {
            // Кнопка сохранения
            saveButton.setOnClickListener {
                viewModel.saveProfile()
                findNavController().navigateUp()
            }
            
            // Кнопка отмены
            cancelButton.setOnClickListener {
                findNavController().navigateUp()
            }
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.userProfile.collect { profile ->
                updateProfileDisplay(profile)
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isEditing.collect { isEditing ->
                updateEditingUI(isEditing)
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.errorMessage.collect { error ->
                error?.let {
                    // Показать ошибку
                }
            }
        }
    }
    
    private fun updateProfileDisplay(profile: com.emonotion.app.domain.model.UserProfile?) {
        binding.apply {
            // TODO: Заполнить поля профиля
        }
    }
    
    private fun updateEditingUI(isEditing: Boolean) {
        binding.apply {
            // TODO: Обновить UI для режима редактирования
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
