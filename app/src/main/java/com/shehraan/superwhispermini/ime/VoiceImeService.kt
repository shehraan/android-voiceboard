package com.shehraan.superwhispermini.ime

import android.Manifest
import android.content.pm.PackageManager
import android.inputmethodservice.InputMethodService
import android.os.Handler
import android.os.Looper
import android.os.Vibrator
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import com.shehraan.superwhispermini.SuperwhisperMiniApp
import com.shehraan.superwhispermini.formatting.DictationMode
import com.shehraan.superwhispermini.formatting.FormatterPipeline
import com.shehraan.superwhispermini.history.DictationStatus
import com.shehraan.superwhispermini.speech.AndroidOnDeviceRecognizer
import com.shehraan.superwhispermini.speech.RecognizerEngine
import com.shehraan.superwhispermini.util.Logger
import com.shehraan.superwhispermini.util.TimeMetrics

/**
 * Voice Input Method Service - the core IME.
 *
 * Responsibilities:
 * - Own the IME lifecycle
 * - Inflate the mic-first keyboard UI
 * - Handle press-and-hold mic
 * - Show partial transcript previews
 * - Commit final text to focused field
 * - Save to local history
 *
 * Does NOT contain recognition or formatting logic directly.
 */
class VoiceImeService : InputMethodService(), ImeKeyboardView.KeyboardListener {

    private lateinit var keyboardView: ImeKeyboardView
    private lateinit var recognizerEngine: RecognizerEngine
    private lateinit var formatterPipeline: FormatterPipeline
    private lateinit var inputCommitter: InputCommitter
    private val mainHandler = Handler(Looper.getMainLooper())

    private var currentPartialText: String = ""
    private var finalRawText: String = ""
    private var timeMetrics: TimeMetrics? = null
    private var isRecording: Boolean = false

    override fun onCreate() {
        super.onCreate()
        Logger.d("VoiceImeService", "onCreate")

        recognizerEngine = AndroidOnDeviceRecognizer(this)
        formatterPipeline = FormatterPipeline(this)
        inputCommitter = InputCommitter(this)

        // Check if on-device recognition is available
        if (!recognizerEngine.isAvailable()) {
            Logger.w("VoiceImeService", "On-device recognition not available on this device")
        }
    }

    override fun onCreateInputView(): View {
        Logger.d("VoiceImeService", "onCreateInputView")
        keyboardView = ImeKeyboardView(this)
        keyboardView.keyboardListener = this
        return keyboardView
    }

    override fun onStartInputView(info: EditorInfo, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        Logger.d("VoiceImeService", "onStartInputView")
        keyboardView.clearPreview()
        keyboardView.clearStatus()
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        Logger.d("VoiceImeService", "onFinishInputView")
        if (isRecording) {
            stopRecording()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Logger.d("VoiceImeService", "onDestroy")
        recognizerEngine.destroy()
    }

    // -- KeyboardListener Implementation --

    override fun onMicButtonPressed() {
        Logger.d("VoiceImeService", "Mic button pressed")

        if (!hasRecordAudioPermission()) {
            keyboardView.showError("Microphone permission required")
            return
        }

        if (!recognizerEngine.isAvailable()) {
            keyboardView.showError("On-device recognition unavailable")
            return
        }

        // Haptic feedback on press
        vibrate(50)

        startRecording()
    }

    override fun onMicButtonReleased() {
        Logger.d("VoiceImeService", "Mic button released")
        if (isRecording) {
            stopRecording()
        }
    }

    override fun onModeToggled(mode: DictationMode) {
        Logger.d("VoiceImeService", "Mode toggled to: $mode")
    }

    override fun onSettingsClicked() {
        Logger.d("VoiceImeService", "Settings clicked - opening MainActivity")
        val intent = android.content.Intent(this, com.shehraan.superwhispermini.settings.MainActivity::class.java).apply {
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    override fun onBackspaceClicked() {
        currentInputConnection?.deleteSurroundingText(1, 0)
    }

    // -- Recording Logic --

    private fun startRecording() {
        Logger.d("VoiceImeService", "Starting recording")
        isRecording = true
        currentPartialText = ""
        finalRawText = ""
        timeMetrics = TimeMetrics.startNow()

        recognizerEngine.start(
            onPartial = { partialText ->
                mainHandler.post {
                    if (isRecording) {
                        currentPartialText = partialText
                        keyboardView.setPreviewText(partialText)
                    }
                }
            },
            onFinal = { finalText ->
                mainHandler.post {
                    handleFinalResult(finalText)
                }
            },
            onError = { throwable ->
                mainHandler.post {
                    handleError(throwable)
                }
            }
        )
    }

    private fun stopRecording() {
        Logger.d("VoiceImeService", "Stopping recording")
        isRecording = false
        recognizerEngine.stop()
    }

    private fun handleFinalResult(rawText: String) {
        Logger.d("VoiceImeService", "Final result: $rawText")

        finalRawText = rawText
        val latency = timeMetrics?.elapsedMillis() ?: 0

        // Format the text
        val mode = keyboardView.getCurrentMode()
        val formattedText = formatterPipeline.format(rawText, mode)

        Logger.d("VoiceImeService", "Formatted text: $formattedText")

        // Commit the text
        val committed = commitText(formattedText)

        // Save to history
        saveToHistory(rawText, formattedText, mode, committed, latency)

        // Clear preview
        keyboardView.clearPreview()

        // Haptic feedback on completion
        vibrate(30)
    }

    private fun handleError(throwable: Throwable) {
        Logger.e("VoiceImeService", "Recognition error", throwable)
        keyboardView.showError("Error: ${throwable.message}")
        keyboardView.clearPreview()
        isRecording = false

        // Save failed attempt
        saveToHistory(
            rawText = currentPartialText,
            finalText = "",
            mode = keyboardView.getCurrentMode(),
            committed = false,
            latency = timeMetrics?.elapsedMillis() ?: 0,
            status = DictationStatus.FAILED
        )
    }

    private fun commitText(text: String): Boolean {
        val committed = inputCommitter.commitText(currentInputConnection, text)
        if (!committed) {
            keyboardView.showError("Copied to clipboard")
        }
        return committed
    }

    private fun saveToHistory(
        rawText: String,
        finalText: String,
        mode: DictationMode,
        committed: Boolean,
        latency: Long,
        status: DictationStatus = DictationStatus.SUCCESS
    ) {
        val app = application as? SuperwhisperMiniApp
        val finalStatus = if (status == DictationStatus.FAILED) {
            DictationStatus.FAILED
        } else if (committed) {
            DictationStatus.SUCCESS
        } else {
            DictationStatus.SUCCESS
        }

        app?.historyRepository?.saveEntry(
            rawText = rawText,
            finalText = finalText,
            mode = mode,
            status = finalStatus,
            latencyMillis = latency
        )
    }

    private fun hasRecordAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun vibrate(milliseconds: Long) {
        try {
            val vibrator = getSystemService(VIBRATOR_SERVICE) as? Vibrator
            vibrator?.vibrate(milliseconds)
        } catch (e: Exception) {
            // Ignore vibration errors
        }
    }
}
