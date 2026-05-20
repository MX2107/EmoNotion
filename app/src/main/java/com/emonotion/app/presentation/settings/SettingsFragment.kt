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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
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
    private var suppressGmailSwitchListener = false
    private var suppressBackupSwitchListener = false
    private var suppressSyncSpinner = false
    private var suppressBackupSpinner = false

    private val importLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { onImportPicked(it) } }

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
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            freqLabels()
        )
        binding.syncFrequencySpinner.adapter = adapter
        binding.backupFrequencySpinner.adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, freqLabels())

        binding.syncFrequencySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (suppressSyncSpinner) return
                viewModel.onGmailFrequencyChanged(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        binding.backupFrequencySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (suppressBackupSpinner) return
                viewModel.onBackupFrequencyChanged(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
    }

    private fun syncSpinnerSelectionsFromViewModel() {
        suppressSyncSpinner = true
        binding.syncFrequencySpinner.setSelection(viewModel.gmailSyncFrequencyOrdinal().coerceIn(0, 2))
        suppressSyncSpinner = false
        suppressBackupSpinner = true
        binding.backupFrequencySpinner.setSelection(0)
        suppressBackupSpinner = false
    }

    private fun setupThemeRadios() {
        binding.themeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            if (suppressThemeListener) return@setOnCheckedChangeListener
            val mode = when (checkedId) {
                R.id.radio_theme_light -> ThemeMode.LIGHT
                R.id.radio_theme_dark -> ThemeMode.DARK
                R.id.radio_theme_system -> ThemeMode.SYSTEM
                else -> return@setOnCheckedChangeListener
            }
            viewModel.onThemeSelected(mode)
        }
    }

    private fun bindThemeRadios(theme: ThemeMode) {
        suppressThemeListener = true
        when (theme) {
            ThemeMode.LIGHT -> binding.themeRadioGroup.check(R.id.radio_theme_light)
            ThemeMode.DARK -> binding.themeRadioGroup.check(R.id.radio_theme_dark)
            ThemeMode.SYSTEM -> binding.themeRadioGroup.check(R.id.radio_theme_system)
        }
        suppressThemeListener = false
    }

    private fun setupActions() {
        binding.exportDataButton.setOnClickListener {
            Toast.makeText(requireContext(), R.string.export_data_started, Toast.LENGTH_SHORT).show()
            viewModel.exportAllData()
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

        binding.syncNowButton.setOnClickListener {
            viewModel.syncNow()
        }

        binding.gmailSyncSwitch.setOnCheckedChangeListener { _, checked ->
            if (suppressGmailSwitchListener) return@setOnCheckedChangeListener
            binding.gmailSettingsContainer.visibility = if (checked) View.VISIBLE else View.GONE
            viewModel.onGmailAutoSyncSwitch(checked, binding.syncFrequencySpinner.selectedItemPosition)
        }

        binding.autoBackupSwitch.setOnCheckedChangeListener { _, checked ->
            if (suppressBackupSwitchListener) return@setOnCheckedChangeListener
            binding.backupSettingsContainer.visibility = if (checked) View.VISIBLE else View.GONE
            viewModel.onAutoBackupSwitch(checked, binding.backupFrequencySpinner.selectedItemPosition)
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
            viewModel.gmailAutoSyncEnabled.collect { enabled ->
                suppressGmailSwitchListener = true
                binding.gmailSyncSwitch.isChecked = enabled
                binding.gmailSettingsContainer.visibility = if (enabled) View.VISIBLE else View.GONE
                suppressGmailSwitchListener = false
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.dataDisplayPath.collect { path ->
                binding.dataDirectoryText.text = path
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.lastSyncDisplay.collect { display ->
                binding.lastSyncText.text = getString(R.string.last_sync, display)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.busyExportImport.collect { busy ->
                val alpha = if (busy) 0.5f else 1f
                binding.exportDataButton.alpha = alpha
                binding.importDataButton.alpha = alpha
                binding.exportDataButton.isClickable = !busy
                binding.importDataButton.isClickable = !busy
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
