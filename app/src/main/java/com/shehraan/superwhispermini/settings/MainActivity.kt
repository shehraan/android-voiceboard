package com.shehraan.superwhispermini.settings

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
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
 * - Status text for keyboard enabled/mic permission
 * - Switch keyboard button (shows input method picker)
 * - History list with copy/retry
 * - Mode selection (Raw/Message)
 */
class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: SettingsViewModel
    private lateinit var historyAdapter: HistoryAdapter

    private lateinit var keyboardEnabledStatus: TextView
    private lateinit var permissionGrantedStatus: TextView
    private lateinit var switchKeyboardButton: Button
    private lateinit var voiceModeButton: Button
    private lateinit var messageModeButton: Button
    private lateinit var historyRecyclerView: RecyclerView

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(this, "Microphone permission granted", Toast.LENGTH_SHORT).show()
            updateStatusTexts()
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

        // Long press on status texts to trigger the actions
        keyboardEnabledStatus.setOnLongClickListener {
            val intent = viewModel.openKeyboardSettings()
            startActivity(intent)
            true
        }

        permissionGrantedStatus.setOnLongClickListener {
            requestMicrophonePermission()
            true
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshKeyboardStatus()
        updateStatusTexts()
    }

    private fun setupViews() {
        keyboardEnabledStatus = findViewById(R.id.keyboardEnabledStatus)
        permissionGrantedStatus = findViewById(R.id.permissionGrantedStatus)
        switchKeyboardButton = findViewById(R.id.switchKeyboardButton)
        voiceModeButton = findViewById(R.id.voiceModeButton)
        messageModeButton = findViewById(R.id.messageModeButton)
        historyRecyclerView = findViewById(R.id.historyRecyclerView)

        switchKeyboardButton.setOnClickListener {
            viewModel.showInputMethodPicker()
        }

        voiceModeButton.setOnClickListener {
            viewModel.setDictationMode(DictationMode.RAW)
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
                    updateKeyboardStatusText(enabled)
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

    private fun updateStatusTexts() {
        val hasPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        updatePermissionStatusText(hasPermission)
    }

    private fun updateKeyboardStatusText(enabled: Boolean) {
        keyboardEnabledStatus.text = if (enabled) {
            "✅ Keyboard enabled"
        } else {
            "⌛ Keyboard not enabled (long press to enable)"
        }
        keyboardEnabledStatus.setTextColor(
            if (enabled) ContextCompat.getColor(this, android.R.color.holo_green_dark)
            else ContextCompat.getColor(this, android.R.color.darker_gray)
        )
    }

    private fun updatePermissionStatusText(hasPermission: Boolean) {
        permissionGrantedStatus.text = if (hasPermission) {
            "✅ Microphone permission granted"
        } else {
            "⌛ Microphone permission not granted (long press to grant)"
        }
        permissionGrantedStatus.setTextColor(
            if (hasPermission) ContextCompat.getColor(this, android.R.color.holo_green_dark)
            else ContextCompat.getColor(this, android.R.color.darker_gray)
        )
    }

    private fun updateModeButtons(currentMode: DictationMode) {
        val darkGreen = Color.parseColor("#4CAF50")   // Material Green 500 (ON) - matches Switch Keyboard button
        val lightGreen = Color.parseColor("#90EE90")  // Light green (OFF)
        val black = Color.parseColor("#000000")       // Black text (ON)
        val grey = Color.parseColor("#666666")        // Grey text (OFF)
        
        when (currentMode) {
            DictationMode.RAW -> {
                // Raw is ON: dark green background + black text
                voiceModeButton.backgroundTintList = ColorStateList.valueOf(darkGreen)
                voiceModeButton.setTextColor(black)
                // Message is OFF: light green background + grey text
                messageModeButton.backgroundTintList = ColorStateList.valueOf(lightGreen)
                messageModeButton.setTextColor(grey)
            }
            DictationMode.MESSAGE -> {
                // Raw is OFF: light green background + grey text
                voiceModeButton.backgroundTintList = ColorStateList.valueOf(lightGreen)
                voiceModeButton.setTextColor(grey)
                // Message is ON: dark green background + black text
                messageModeButton.backgroundTintList = ColorStateList.valueOf(darkGreen)
                messageModeButton.setTextColor(black)
            }
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
