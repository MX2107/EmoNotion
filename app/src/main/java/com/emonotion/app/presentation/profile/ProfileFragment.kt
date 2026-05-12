package com.emonotion.app.presentation.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.emonotion.app.databinding.FragmentProfileBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Экран профиля пользователя
 */
@AndroidEntryPoint
class ProfileFragment : Fragment() {
    
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ProfileViewModel by viewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
        viewModel.loadUserProfile()
    }
    
    private fun setupUI() {
        binding.apply {
            // Кнопка редактирования профиля
            editProfileButton.setOnClickListener {
                if (viewModel.isEditing.value) {
                    viewModel.saveProfile()
                } else {
                    viewModel.startEditing()
                }
            }
            
            // Кнопка настроек
            settingsButton.setOnClickListener {
                findNavController().navigate(com.emonotion.app.R.id.action_profileFragment_to_settingsFragment)
            }
            
            // Кнопка аналитики
            analyticsButton.setOnClickListener {
                findNavController().navigate(com.emonotion.app.R.id.action_profileFragment_to_analyticsFragment)
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
        
        // TODO: Обработать editedName, editedBio, editedAvatar и isLoading когда будут готовы UI элементы
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.errorMessage.collect { error ->
                error?.let {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun updateProfileDisplay(profile: com.emonotion.app.domain.model.UserProfile?) {
        binding.apply {
            if (profile != null) {
                // Отображаем данные профиля
                nameText.text = profile.name
                emailText.text = profile.email ?: ""
                
                // TODO: Настроить аватар и другие элементы когда будут готовы
                
            } else {
                // TODO: Показать состояние пустого профиля когда будут готовы элементы
            }
        }
    }
    
    private fun updateEditingUI(isEditing: Boolean) {
        // TODO: Обновить UI для режима редактирования когда будут готовы элементы
        // Заглушка для предотвращения предупреждения о неиспользуемом параметре
        if (isEditing) {
            // Будет реализовано позже
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
