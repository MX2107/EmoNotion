package com.emonotion.app.presentation.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.emonotion.app.R
import com.emonotion.app.databinding.FragmentSettingsBinding
import com.emonotion.app.domain.model.ThemeMode
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Экран настроек приложения
 */
@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels()

    private var suppressThemeListener = false
    private var suppressBackupSwitchListener = false
    private var suppressBackupSpinner = false

    private val importLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { onImportPicked(it) } }

    private val exportLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> uri?.let { onExportPicked(it) } }

    private val treeLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            try {
                val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                requireContext().contentResolver.takePersistableUriPermission(uri, flags)
            } catch (_: SecurityException) {
            }
            viewModel.applyPreferredDataTreeUri(uri)
        }
    }

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
        setupVersionText()
        setupSpinners()
        setupThemeRadios()
        setupActions()
        observeViewModel()
        viewModel.loadSettings()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshAuxiliaryUi()
        syncSpinnerSelectionsFromViewModel()
    }

    private fun setupVersionText() {
        val pm = requireContext().packageManager
        val pkg = requireContext().packageName
        val versionName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getPackageInfo(pkg, android.content.pm.PackageManager.PackageInfoFlags.of(0)).versionName
        } else {
            @Suppress("DEPRECATION")
            pm.getPackageInfo(pkg, 0).versionName
        }
        binding.versionText.text = getString(R.string.version_label, versionName ?: "—")
    }

    private fun freqLabels(): Array<String> = arrayOf(
        getString(R.string.freq_daily),
        getString(R.string.freq_weekly),
        getString(R.string.freq_monthly)
    )

    private fun setupSpinners() {
        binding.backupFrequencySpinner.adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, freqLabels())

        binding.backupFrequencySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (suppressBackupSpinner) return
                viewModel.onBackupFrequencyChanged(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
    }

    private fun syncSpinnerSelectionsFromViewModel() {
        suppressBackupSpinner = true
        binding.backupFrequencySpinner.setSelection(0)
        suppressBackupSpinner = false
    }

    private fun setupThemeRadios() {
        binding.themeLightCard.setOnClickListener {
            if (suppressThemeListener) return@setOnClickListener
            viewModel.onThemeSelected(ThemeMode.LIGHT)
        }

        binding.themeDarkCard.setOnClickListener {
            if (suppressThemeListener) return@setOnClickListener
            viewModel.onThemeSelected(ThemeMode.DARK)
        }
    }

    private fun bindThemeRadios(theme: ThemeMode) {
        suppressThemeListener = true
        when (theme) {
            ThemeMode.LIGHT -> {
                binding.themeLightCard.strokeWidth = 2
                binding.themeLightCard.strokeColor = ContextCompat.getColor(requireContext(), R.color.accent)
                binding.themeDarkCard.strokeWidth = 0
            }
            ThemeMode.DARK -> {
                binding.themeDarkCard.strokeWidth = 2
                binding.themeDarkCard.strokeColor = ContextCompat.getColor(requireContext(), R.color.accent)
                binding.themeLightCard.strokeWidth = 0
            }
            ThemeMode.SYSTEM -> {
                binding.themeLightCard.strokeWidth = 2
                binding.themeLightCard.strokeColor = ContextCompat.getColor(requireContext(), R.color.accent)
                binding.themeDarkCard.strokeWidth = 0
            }
        }
        suppressThemeListener = false
    }

    private fun setupActions() {
        binding.editEmotionsButton.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_customEmotionsFragment)
        }

        binding.editActivitiesButton.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_customActivitiesFragment)
        }

        binding.editTagsButton.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_customTagsFragment)
        }

        binding.exportDataButton.setOnClickListener {
            val timestamp = System.currentTimeMillis()
            exportLauncher.launch("emonotion_backup_$timestamp.json")
        }

        binding.importDataButton.setOnClickListener {
            importLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
        }

        binding.copyDataPathButton.setOnClickListener {
            val text = binding.dataDirectoryText.text?.toString().orEmpty()
            val cm = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("path", text))
            Toast.makeText(requireContext(), R.string.path_copied, Toast.LENGTH_SHORT).show()
        }

        binding.changeDirectoryButton.setOnClickListener {
            treeLauncher.launch(null)
        }

        binding.autoBackupSwitch.setOnCheckedChangeListener { _, checked ->
            if (suppressBackupSwitchListener) return@setOnCheckedChangeListener
            binding.backupSettingsContainer.visibility = if (checked) View.VISIBLE else View.GONE
            viewModel.onAutoBackupSwitch(checked, binding.backupFrequencySpinner.selectedItemPosition)
        }

        binding.backupNowButton.setOnClickListener {
            viewModel.createBackupNow()
        }

        binding.openAboutButton.setOnClickListener {
            showAboutDialog()
        }

        binding.resetSettingsButton.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.reset_settings_title)
                .setMessage(R.string.reset_settings_message)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    viewModel.resetToDefaults()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.settings.collect { settings ->
                settings?.let { s ->
                    suppressBackupSwitchListener = true
                    binding.autoBackupSwitch.isChecked = s.autoBackupEnabled
                    binding.backupSettingsContainer.visibility =
                        if (s.autoBackupEnabled) View.VISIBLE else View.GONE
                    suppressBackupSwitchListener = false
                    bindThemeRadios(s.theme)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.dataDisplayPath.collect { path ->
                binding.dataDirectoryText.text = path
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.lastBackupDisplay.collect { display ->
                binding.lastBackupText.text = "Последний бэкап: $display"
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.busyExport.collect { busy ->
                val alpha = if (busy) 0.5f else 1f
                binding.exportDataButton.alpha = alpha
                binding.exportDataButton.isClickable = !busy
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.busyImport.collect { busy ->
                val alpha = if (busy) 0.5f else 1f
                binding.importDataButton.alpha = alpha
                binding.importDataButton.isClickable = !busy
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.lastExportPath.collect { path ->
                path?.let {
                    val fileName = Uri.parse(it).lastPathSegment ?: "backup.json"
                    binding.exportPathText.text = "Сохранено: $fileName"
                } ?: run {
                    binding.exportPathText.text = "Сохранить в файл"
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.lastImportPath.collect { path ->
                path?.let {
                    val fileName = Uri.parse(it).lastPathSegment ?: "backup.json"
                    binding.importPathText.text = "Загружено: $fileName"
                } ?: run {
                    binding.importPathText.text = "Восстановить из файла"
                }
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
            viewModel.successMessage.collect { msg ->
                msg?.let {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                    viewModel.clearSuccess()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.shouldRecreateActivity.collect { recreate ->
                if (recreate) {
                    viewModel.consumeRecreateRequest()
                    requireActivity().recreate()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { loading ->
                binding.root.alpha = if (loading) 0.85f else 1f
            }
        }
    }

    private fun showAboutDialog() {
        val body = buildString {
            appendLine(getString(R.string.app_description))
            appendLine()
            appendLine(getString(R.string.about_licenses_body))
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.about_details)
            .setMessage(body)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun onImportPicked(uri: Uri) {
        val json = readUriAsText(uri)?.trim().orEmpty()
        if (json.isEmpty()) {
            Toast.makeText(requireContext(), R.string.invalid_import_file, Toast.LENGTH_LONG).show()
            return
        }
        viewModel.setLastImportPath(uri.toString())
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.import_confirm_title)
            .setMessage(R.string.import_confirm_message)
            .setNeutralButton(R.string.import_merge) { _: android.content.DialogInterface, _: Int ->
                viewModel.importAllData(json, replaceExisting = false)
            }
            .setPositiveButton(R.string.import_replace_title) { _: android.content.DialogInterface, _: Int ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.import_replace_title)
                    .setMessage(R.string.import_replace_message)
                    .setPositiveButton(android.R.string.ok) { _: android.content.DialogInterface, _: Int ->
                        viewModel.importAllData(json, replaceExisting = true)
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun onExportPicked(uri: Uri) {
        viewModel.setLastExportPath(uri.toString())
        viewModel.exportAllDataToUri(uri)
    }

    private fun readUriAsText(uri: Uri): String? {
        return try {
            requireContext().contentResolver.openInputStream(uri)?.use { input ->
                BufferedReader(InputStreamReader(input, Charsets.UTF_8)).readText()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
            null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
