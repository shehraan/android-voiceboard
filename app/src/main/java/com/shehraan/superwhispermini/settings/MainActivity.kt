package com.shehraan.superwhispermini.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shehraan.superwhispermini.R
import com.shehraan.superwhispermini.formatting.DictationMode
import kotlinx.coroutines.launch

/**
 * Main Activity for onboarding, settings, and history.
 * 
 * Provides:
 * - Enable keyboard button (opens system settings)
 * - Switch keyboard button (shows input method picker)
 * - History list with copy/retry
 * - Basic settings
 */
class MainActivity : AppCompatActivity() {
    
    private lateinit var viewModel: SettingsViewModel
    private lateinit var historyAdapter: HistoryAdapter
    
    private lateinit var enableKeyboardButton: Button
    private lateinit var switchKeyboardButton: Button
    private lateinit var grantPermissionButton: Button
    private lateinit var voiceModeButton: Button
    private lateinit var messageModeButton: Button
    private lateinit var historyRecyclerView: RecyclerView
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(this, "Microphone permission granted", Toast.LENGTH_SHORT).show()
            updatePermissionButton()
        } else {
            Toast.makeText(this, "Microphone permission required for voice input", Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        viewModel = ViewModelProvider(this)[SettingsViewModel::class.java]
        
        setupViews()
        setupHistory()
        observeViewModel()
    }
    
    override fun onResume() {
        super.onResume()
        viewModel.refreshKeyboardStatus()
        updatePermissionButton()
    }
    
    private fun setupViews() {
        enableKeyboardButton = findViewById(R.id.enableKeyboardButton)
        switchKeyboardButton = findViewById(R.id.switchKeyboardButton)
        grantPermissionButton = findViewById(R.id.grantPermissionButton)
        voiceModeButton = findViewById(R.id.voiceModeButton)
        messageModeButton = findViewById(R.id.messageModeButton)
        historyRecyclerView = findViewById(R.id.historyRecyclerView)
        
        enableKeyboardButton.setOnClickListener {
            val intent = viewModel.openKeyboardSettings()
            startActivity(intent)
        }
        
        switchKeyboardButton.setOnClickListener {
            viewModel.showInputMethodPicker()
        }
        
        grantPermissionButton.setOnClickListener {
            requestMicrophonePermission()
        }
        
        voiceModeButton.setOnClickListener {
            viewModel.setDictationMode(DictationMode.VOICE)
        }
        
        messageModeButton.setOnClickListener {
            viewModel.setDictationMode(DictationMode.MESSAGE)
        }
    }
    
    private fun setupHistory() {
        historyAdapter = HistoryAdapter(
            onCopyClick = { text ->
                copyToClipboard(text)
            },
            onDeleteClick = { id ->
                viewModel.deleteHistoryEntry(id)
            }
        )
        
        historyRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = historyAdapter
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isKeyboardEnabled.collect { enabled ->
                    enableKeyboardButton.isEnabled = !enabled
                    enableKeyboardButton.text = if (enabled) {
                        "Keyboard Enabled"
                    } else {
                        "Enable Keyboard"
                    }
                }
            }
        }
        
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.historyEntries.collect { entries ->
                    historyAdapter.submitList(entries)
                }
            }
        }
        
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentMode.collect { mode ->
                    updateModeButtons(mode)
                }
            }
        }
    }
    
    private fun updateModeButtons(currentMode: DictationMode) {
        when (currentMode) {
            DictationMode.VOICE -> {
                voiceModeButton.isEnabled = false
                messageModeButton.isEnabled = true
                voiceModeButton.alpha = 0.5f
                messageModeButton.alpha = 1.0f
            }
            DictationMode.MESSAGE -> {
                voiceModeButton.isEnabled = true
                messageModeButton.isEnabled = false
                voiceModeButton.alpha = 1.0f
                messageModeButton.alpha = 0.5f
            }
        }
    }
    
    private fun updatePermissionButton() {
        val hasPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        
        grantPermissionButton.isEnabled = !hasPermission
        grantPermissionButton.text = if (hasPermission) {
            "Microphone Permission Granted"
        } else {
            "Grant Microphone Permission"
        }
    }
    
    private fun requestMicrophonePermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                Toast.makeText(this, "Microphone permission already granted", Toast.LENGTH_SHORT).show()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) -> {
                Toast.makeText(this, "This app needs microphone access to transcribe speech", Toast.LENGTH_LONG).show()
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }
    
    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("Superwhisper", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }
}
