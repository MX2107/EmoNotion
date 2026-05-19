package com.emonotion.app.presentation.profile

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.emonotion.app.R
import com.emonotion.app.databinding.FragmentEditProfileBinding
import com.emonotion.app.utils.ImageUtils
import com.emonotion.app.utils.ImageHelper
import com.emonotion.app.domain.usecase.avatar.SaveAvatarUseCase
import com.emonotion.app.domain.usecase.avatar.DeleteAvatarUseCase
import com.yalantis.ucrop.UCrop
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.io.File
import java.io.FileOutputStream
import android.graphics.Bitmap
import android.graphics.BitmapFactory

/**
 * Экран редактирования профиля
 */
@AndroidEntryPoint
class EditProfileFragment : Fragment() {
    
    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ProfileViewModel by viewModels()
    
    private var selectedImageUri: Uri? = null
    private var currentAvatarPath: String? = null
    private var tempAvatarPath: String? = null
    private var originalAvatarPath: String? = null

    @Inject
    lateinit var saveAvatarUseCase: SaveAvatarUseCase

    @Inject
    lateinit var deleteAvatarUseCase: DeleteAvatarUseCase
    
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            // Запускаем обрезку изображения
            startCropImage(uri)
        }
    }

    private val cropImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.let { intent ->
                val resultUri = UCrop.getOutput(intent)
                resultUri?.let {
                    // Сохраняем оригинал и обрезанное изображение
                    saveOriginalAndCroppedImage(selectedImageUri, it)
                }
            }
        } else if (result.resultCode == UCrop.RESULT_ERROR) {
            result.data?.let { intent ->
                val cropError = UCrop.getError(intent)
                Toast.makeText(requireContext(), "Ошибка обрезки: ${cropError?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openImagePicker()
        } else {
            Toast.makeText(requireContext(), "Разрешение на доступ к галерее необходимо", Toast.LENGTH_SHORT).show()
        }
    }
    
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
        viewModel.loadUserProfile()
    }
    
    private fun openImagePicker() {
        pickImageLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }
    
    private fun startCropImage(sourceUri: Uri) {
        try {
            // Создаем временный файл для сохранения обрезанного изображения
            val destinationUri = Uri.fromFile(File(
                requireContext().cacheDir,
                "cropped_avatar_${System.currentTimeMillis()}.jpg"
            ))

            // Настраиваем параметры обрезки - круг для отображения в кружке
            val options = UCrop.Options().apply {
                setToolbarTitle("Обрезать фото")
                setToolbarColor(ContextCompat.getColor(requireContext(), R.color.foreground))
                setStatusBarColor(ContextCompat.getColor(requireContext(), R.color.background))
                setRootViewBackgroundColor(ContextCompat.getColor(requireContext(), R.color.background))
                setCircleDimmedLayer(true) // Включаем круговую маску в интерфейсе
                setShowCropGrid(false)
                setShowCropFrame(false)
                setHideBottomControls(false)
                setFreeStyleCropEnabled(false)
                setCropGridColor(ContextCompat.getColor(requireContext(), R.color.foreground))
                setCropFrameColor(ContextCompat.getColor(requireContext(), R.color.foreground))
                setToolbarWidgetColor(ContextCompat.getColor(requireContext(), R.color.background))
                setLogoColor(ContextCompat.getColor(requireContext(), R.color.foreground))
            }

            val uCrop = UCrop.of(sourceUri, destinationUri)
                .withOptions(options)
                .withAspectRatio(1f, 1f)
                .withMaxResultSize(500, 500)

            cropImageLauncher.launch(uCrop.getIntent(requireContext()))

        } catch (e: Exception) {
            android.util.Log.e("EditProfileFragment", "Ошибка запуска обрезки", e)
            Toast.makeText(requireContext(), "Ошибка запуска обрезки", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun saveOriginalAndCroppedImage(originalUri: Uri?, croppedUri: Uri) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Сохраняем обрезанное изображение для отображения
                val inputStream = requireContext().contentResolver.openInputStream(croppedUri)
                val croppedBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (croppedBitmap != null) {
                    val userId = "current_user"
                    val result = saveAvatarUseCase(croppedBitmap, userId)

                    result.fold(
                        onSuccess = { path ->
                            tempAvatarPath = path
                            viewModel.updateAvatar(path)
                            android.util.Log.d("EditProfileFragment", "Сохранено обрезанное изображение: $path")

                            // Сохраняем оригинальное изображение с суффиксом _original
                            originalUri?.let { origUri ->
                                val origInputStream = requireContext().contentResolver.openInputStream(origUri)
                                val originalBitmap = BitmapFactory.decodeStream(origInputStream)
                                origInputStream?.close()

                                if (originalBitmap != null) {
                                    val originalPath = ImageHelper.getOriginalPath(path)
                                    val originalFile = File(originalPath)
                                    FileOutputStream(originalFile).use { out ->
                                        originalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                                    }
                                    android.util.Log.d("EditProfileFragment", "Сохранено оригинальное изображение: $originalPath")
                                }
                            }

                            Toast.makeText(requireContext(), "Фото сохранено", Toast.LENGTH_SHORT).show()
                            updateAvatarDisplay(path)
                        },
                        onFailure = { error ->
                            android.util.Log.e("EditProfileFragment", "Ошибка сохранения фото", error)
                            Toast.makeText(requireContext(), "Ошибка сохранения фото", Toast.LENGTH_SHORT).show()
                        }
                    )
                } else {
                    Toast.makeText(requireContext(), "Ошибка загрузки фото", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                android.util.Log.e("EditProfileFragment", "Ошибка сохранения изображений", e)
                Toast.makeText(requireContext(), "Ошибка сохранения фото", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun checkPermissionAndOpenPicker() {
        // PickVisualMedia не требует разрешений на Android 13+
        openImagePicker()
    }
    
    private fun updateAvatarDisplay(avatarPath: String?) {
        if (!avatarPath.isNullOrEmpty()) {
            android.util.Log.d(
                "EditProfileFragment",
                "Загрузка аватара из: $avatarPath, exists=${File(avatarPath).exists()}"
            )
            ImageHelper.loadAvatarInto(binding.avatarImage, avatarPath)
            binding.deleteAvatarButton.visibility = View.VISIBLE
            binding.avatarImage.setOnClickListener {
                ImageHelper.openAvatarFullscreen(requireContext(), avatarPath)
            }
        } else {
            ImageHelper.showPlaceholder(binding.avatarImage)
            binding.deleteAvatarButton.visibility = View.GONE
            binding.avatarImage.setOnClickListener(null)
        }
    }
    
    private fun setupUI() {
        binding.apply {
            // Кнопка сохранения
            saveButton.setOnClickListener {
                val name = nameEditText.text?.toString()?.trim() ?: ""
                val email = emailEditText.text?.toString()?.trim() ?: ""
                
                android.util.Log.d("EditProfileFragment", "Сохранение профиля: name='$name', email='$email'")
                viewModel.updateName(name)
                viewModel.updateEmail(email)
                
                if (viewModel.isProfileValid()) {
                    viewModel.saveProfile()
                    // Не навигируемся сразу, ждём завершения сохранения
                    // Навигация произойдёт после успешного сохранения через observeViewModel
                } else {
                    Toast.makeText(requireContext(), "Имя не может быть пустым", Toast.LENGTH_SHORT).show()
                }
            }
            
            // Кнопка отмены
            cancelButton.setOnClickListener {
                // Удаляем временное изображение если есть
                tempAvatarPath?.let { tempPath ->
                    ImageUtils.deleteImage(tempPath)
                    android.util.Log.d("EditProfileFragment", "Удалено временное изображение: $tempPath")
                }
                // Восстанавливаем оригинальный аватар
                originalAvatarPath?.let { viewModel.updateAvatar(it) }
                viewModel.cancelEditing()
                findNavController().navigateUp()
            }
            
            // Кнопка изменения фото
            changePhotoButton.setOnClickListener {
                checkPermissionAndOpenPicker()
            }
            
            // Кнопка удаления фото
            deleteAvatarButton.setOnClickListener {
                deleteAvatar()
            }
        }
    }
    
    private fun deleteAvatar() {
        // Показываем диалог подтверждения удаления
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Удалить фото")
            .setMessage("Вы уверены, что хотите удалить фото профиля?")
            .setPositiveButton("Удалить") { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        // Удаляем текущий аватар через Use Case
                        currentAvatarPath?.let { path ->
                            deleteAvatarUseCase.deleteByPath(path)
                            android.util.Log.d("EditProfileFragment", "Удален аватар: $path")

                            // Удаляем оригинальное изображение если есть
                            val originalPath = ImageHelper.getOriginalPath(path)
                            deleteAvatarUseCase.deleteByPath(originalPath)
                            android.util.Log.d("EditProfileFragment", "Удален оригинал: $originalPath")
                        }

                        // Очищаем путь к аватару в ViewModel
                        viewModel.clearAvatar()

                        ImageHelper.showPlaceholder(binding.avatarImage)

                        // Скрываем кнопку удаления
                        binding.deleteAvatarButton.visibility = android.view.View.GONE

                        // Обновляем временные пути
                        tempAvatarPath = null
                        currentAvatarPath = null

                        Toast.makeText(requireContext(), "Фото удалено", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        android.util.Log.e("EditProfileFragment", "Ошибка удаления аватара", e)
                        Toast.makeText(requireContext(), "Ошибка удаления фото", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.userProfile.collect { profile ->
                updateProfileDisplay(profile)
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.editedName.collect { name ->
                if (binding.nameEditText.text?.toString() != name) {
                    binding.nameEditText.setText(name)
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.editedEmail.collect { email ->
                if (binding.emailEditText.text?.toString() != email) {
                    binding.emailEditText.setText(email)
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.saveButton.isEnabled = !isLoading
                binding.cancelButton.isEnabled = !isLoading
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.errorMessage.collect { error ->
                error?.let {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.successMessage.collect { message ->
                message?.let {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                    // Удаляем старое изображение только после успешного сохранения
                    if (tempAvatarPath != null && originalAvatarPath != null && tempAvatarPath != originalAvatarPath) {
                        ImageUtils.deleteImage(originalAvatarPath)
                        android.util.Log.d("EditProfileFragment", "Удалено старое изображение после сохранения: $originalAvatarPath")
                    }
                    // Навигируемся обратно только после успешного сохранения
                    findNavController().navigateUp()
                }
            }
        }
    }
    
    private fun updateProfileDisplay(profile: com.emonotion.app.domain.model.UserProfile?) {
        binding.apply {
            if (profile != null) {
                binding.nameEditText.setText(profile.name)
                binding.emailEditText.setText(profile.email ?: "")

                // Сохраняем текущий путь к аватару
                currentAvatarPath = profile.avatar
                originalAvatarPath = profile.avatar
                tempAvatarPath = null

                // Загрузка аватара
                if (!profile.avatar.isNullOrEmpty()) {
                    updateAvatarDisplay(profile.avatar)
                }
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
