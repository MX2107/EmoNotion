package com.emonotion.app.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.emonotion.app.R

class SettingsFragment : Fragment() {
    
    private lateinit var exportDataButton: LinearLayout
    private lateinit var importDataButton: LinearLayout
    private lateinit var gmailSyncSwitch: Switch
    private lateinit var gmailSettingsContainer: LinearLayout
    private lateinit var syncFrequencySpinner: Spinner
    private lateinit var syncNowButton: LinearLayout
    private lateinit var lastSyncText: TextView
    private lateinit var autoBackupSwitch: Switch
    private lateinit var backupSettingsContainer: LinearLayout
    private lateinit var backupFrequencySpinner: Spinner
    private lateinit var dataDirectoryText: TextView
    private lateinit var changeDirectoryButton: LinearLayout
    private lateinit var versionText: TextView
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupClickListeners()
        setupSwitchListeners()
        updateUI()
    }
    
    private fun initViews(view: View) {
        try {
            exportDataButton = view.findViewById(R.id.export_data_button)
            importDataButton = view.findViewById(R.id.import_data_button)
            gmailSyncSwitch = view.findViewById(R.id.gmail_sync_switch)
            gmailSettingsContainer = view.findViewById(R.id.gmail_settings_container)
            syncFrequencySpinner = view.findViewById(R.id.sync_frequency_spinner)
            syncNowButton = view.findViewById(R.id.sync_now_button)
            lastSyncText = view.findViewById(R.id.last_sync_text)
            autoBackupSwitch = view.findViewById(R.id.auto_backup_switch)
            backupSettingsContainer = view.findViewById(R.id.backup_settings_container)
            backupFrequencySpinner = view.findViewById(R.id.backup_frequency_spinner)
            dataDirectoryText = view.findViewById(R.id.data_directory_text)
            changeDirectoryButton = view.findViewById(R.id.change_directory_button)
            versionText = view.findViewById(R.id.version_text)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun setupClickListeners() {
        exportDataButton.setOnClickListener {
        }
        
        importDataButton.setOnClickListener {
        }
        
        syncNowButton.setOnClickListener {
            updateLastSyncText()
        }
    }
    
    private fun setupSwitchListeners() {
        gmailSyncSwitch.setOnCheckedChangeListener { _, isChecked ->
            gmailSettingsContainer.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
        
        autoBackupSwitch.setOnCheckedChangeListener { _, isChecked ->
            backupSettingsContainer.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
        
        changeDirectoryButton.setOnClickListener {
        }
    }
    
    private fun updateUI() {
        try {
            showDemoSettings()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun showDemoSettings() {
        try {
            versionText.text = "Версия: 1.0.0"
            
            gmailSyncSwitch.isChecked = false
            autoBackupSwitch.isChecked = true
            dataDirectoryText.text = "/storage/emulated/0/Android/data/com.emonotion.app/files"
            
            gmailSettingsContainer.visibility = View.GONE
            backupSettingsContainer.visibility = View.VISIBLE
            lastSyncText.text = "Последняя синхронизация: никогда"
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun updateLastSyncText() {
        try {
            lastSyncText.text = "Последняя синхронизация: только что"
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
