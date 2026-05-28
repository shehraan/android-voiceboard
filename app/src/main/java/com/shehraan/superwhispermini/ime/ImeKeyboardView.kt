package com.shehraan.superwhispermini.ime

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import com.shehraan.superwhispermini.R
import com.shehraan.superwhispermini.formatting.DictationMode
import com.shehraan.superwhispermini.util.Logger

/**
 * Custom keyboard view for voice input.
 * Features:
 * - Large press-and-hold mic button
 * - Mode toggle (Voice/Message)
 * - Partial transcript preview
 * - Recording state indicator
 */
class ImeKeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    interface KeyboardListener {
        fun onMicButtonPressed()
        fun onMicButtonReleased()
        fun onModeToggled(mode: DictationMode)
        fun onSettingsClicked()
        fun onBackspaceClicked()
    }
    
    var keyboardListener: KeyboardListener? = null
    
    private lateinit var micButton: Button
    private lateinit var modeToggleButton: Button
    private lateinit var settingsButton: Button
    private lateinit var backspaceButton: Button
    private lateinit var previewText: TextView
    private lateinit var statusText: TextView
    
    private var currentMode: DictationMode = DictationMode.VOICE
    private var isRecording = false
    
    init {
        LayoutInflater.from(context).inflate(R.layout.keyboard_view, this, true)
        setupViews()
    }
    
    private fun setupViews() {
        micButton = findViewById(R.id.micButton)
        modeToggleButton = findViewById(R.id.modeToggleButton)
        settingsButton = findViewById(R.id.settingsButton)
        backspaceButton = findViewById(R.id.backspaceButton)
        previewText = findViewById(R.id.previewText)
        statusText = findViewById(R.id.statusText)
        
        // Mic button - press and hold
        micButton.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (!isRecording) {
                        isRecording = true
                        updateRecordingState()
                        keyboardListener?.onMicButtonPressed()
                    }
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (isRecording) {
                        isRecording = false
                        updateRecordingState()
                        keyboardListener?.onMicButtonReleased()
                    }
                    true
                }
                else -> false
            }
        }
        
        // Mode toggle
        modeToggleButton.setOnClickListener {
            currentMode = if (currentMode == DictationMode.VOICE) {
                DictationMode.MESSAGE
            } else {
                DictationMode.VOICE
            }
            updateModeDisplay()
            keyboardListener?.onModeToggled(currentMode)
        }
        
        // Settings
        settingsButton.setOnClickListener {
            keyboardListener?.onSettingsClicked()
        }
        
        // Backspace
        backspaceButton.setOnClickListener {
            keyboardListener?.onBackspaceClicked()
        }
        
        updateRecordingState()
        updateModeDisplay()
    }
    
    /**
     * Update the preview text (partial transcript).
     */
    fun setPreviewText(text: String) {
        previewText.text = text
        previewText.visibility = if (text.isEmpty()) INVISIBLE else VISIBLE
    }
    
    /**
     * Clear the preview.
     */
    fun clearPreview() {
        previewText.text = ""
        previewText.visibility = INVISIBLE
    }
    
    /**
     * Set status text.
     */
    fun setStatus(text: String) {
        statusText.text = text
    }
    
    /**
     * Clear status.
     */
    fun clearStatus() {
        statusText.text = ""
    }
    
    /**
     * Get current mode.
     */
    fun getCurrentMode(): DictationMode = currentMode
    
    /**
     * Set current mode.
     */
    fun setCurrentMode(mode: DictationMode) {
        currentMode = mode
        updateModeDisplay()
    }
    
    private fun updateRecordingState() {
        if (isRecording) {
            micButton.text = "🎙️"
            micButton.setBackgroundColor(Color.parseColor("#FF4444"))
            setStatus("Listening...")
            Logger.d("ImeKeyboardView", "Recording state: ON")
        } else {
            micButton.text = "🎤"
            micButton.setBackgroundColor(Color.parseColor("#4CAF50"))
            setStatus("")
            Logger.d("ImeKeyboardView", "Recording state: OFF")
        }
    }
    
    private fun updateModeDisplay() {
        when (currentMode) {
            DictationMode.VOICE -> {
                modeToggleButton.text = "M"
                statusText.text = "Voice Mode"
            }
            DictationMode.MESSAGE -> {
                modeToggleButton.text = "M"
                statusText.text = "Message Mode"
            }
        }
    }
    
    /**
     * Show error state briefly.
     */
    fun showError(message: String) {
        statusText.text = message
        statusText.setTextColor(Color.RED)
        postDelayed({
            statusText.setTextColor(Color.BLACK)
            clearStatus()
        }, 2000)
    }
}
