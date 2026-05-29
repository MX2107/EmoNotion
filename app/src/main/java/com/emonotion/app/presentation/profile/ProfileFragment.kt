package com.emonotion.app.presentation.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.emonotion.app.R
import com.emonotion.app.databinding.FragmentProfileBinding
import com.emonotion.app.domain.model.MoodEmoji
import com.emonotion.app.domain.model.UserStats
import com.emonotion.app.utils.ImageHelper
import com.emonotion.app.utils.StreakUiHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

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
        // Загружаем данные только при первом создании
        if (savedInstanceState == null) {
            viewModel.loadUserProfile()
        }
    }

    override fun onResume() {
        super.onResume()
        val avatarPath = viewModel.userProfile.value?.avatar
        if (!avatarPath.isNullOrEmpty()) {
            ImageHelper.loadAvatarInto(binding.avatarImage, avatarPath)
        }
    }
    
    private fun setupUI() {
        binding.apply {
            editProfileButton.setOnClickListener {
                findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
            }
            
            settingsButton.setOnClickListener {
                findNavController().navigate(R.id.action_profileFragment_to_settingsFragment)
            }
            
            analyticsButton.setOnClickListener {
                findNavController().navigate(R.id.action_profileFragment_to_analyticsFragment)
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
            viewModel.errorMessage.collect { error ->
                error?.let {
                    android.util.Log.d("ProfileFragment", "errorMessage: $it")
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.successMessage.collect { message ->
                message?.let {
                    android.util.Log.d("ProfileFragment", "successMessage: $it")
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.userStats.collect { stats ->
                updateStatsDisplay(stats)
            }
        }
    }
    
    private fun updateProfileDisplay(profile: com.emonotion.app.domain.model.UserProfile?) {
        binding.apply {
            if (profile != null) {
                nameText.text = profile.name
                emailText.text = profile.email ?: ""

                if (!profile.avatar.isNullOrEmpty()) {
                    android.util.Log.d(
                        "ProfileFragment",
                        "Загрузка аватара из: ${profile.avatar}, exists=${java.io.File(profile.avatar).exists()}"
                    )
                    ImageHelper.loadAvatarInto(avatarImage, profile.avatar)
                    avatarImage.setOnClickListener {
                        ImageHelper.openAvatarFullscreen(requireContext(), profile.avatar)
                    }
                } else {
                    android.util.Log.d("ProfileFragment", "Путь к аватару пустой")
                    ImageHelper.showPlaceholder(avatarImage)
                    avatarImage.setOnClickListener(null)
                }
            } else {
                nameText.text = "Гость"
                emailText.text = ""
                ImageHelper.showPlaceholder(avatarImage)
                avatarImage.setColorFilter(
                    ResourcesCompat.getColor(resources, R.color.foreground, null)
                )
                avatarImage.setOnClickListener(null)
            }
        }
    }
    
    private fun updateStatsDisplay(stats: com.emonotion.app.domain.model.UserStats) {
        android.util.Log.d("ProfileFragment", "updateStatsDisplay: stats=$stats")
        binding.apply {
            totalEntriesText.text = stats.totalEntries.toString()
            currentStreakText.text = stats.currentStreak.toString()
            val activeToday = StreakUiHelper.isStreakActiveToday(stats)
            StreakUiHelper.applyFlameTint(currentStreakFlameIcon, activeToday)
            longestStreakText.text = stats.longestStreak.toString()
            averageMoodText.text = String.format("%.1f", stats.averageMood)
            averageMoodEmoji.text = getAverageMoodEmoji(stats.averageMood)
        }
    }

    private fun getAverageMoodEmoji(averageMood: Float): String {
        return MoodEmoji.fromScore(averageMood)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
