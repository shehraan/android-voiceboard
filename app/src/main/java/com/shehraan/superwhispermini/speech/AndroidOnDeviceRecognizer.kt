package com.shehraan.superwhispermini.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.shehraan.superwhispermini.util.Logger

/**
 * Android's on-device speech recognizer implementation.
 * Uses SpeechRecognizer.createOnDeviceSpeechRecognizer() when available.
 */
class AndroidOnDeviceRecognizer(context: Context) : RecognizerEngine {

    private val appContext = context.applicationContext
    private var speechRecognizer: SpeechRecognizer? = null
    private var currentListener: OnDeviceRecognitionListener? = null
    
    override fun isAvailable(): Boolean {
        return SpeechRecognizer.isOnDeviceRecognitionAvailable(appContext)
    }
    
    override fun start(
        onPartial: (String) -> Unit,
        onFinal: (String) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        Logger.d("AndroidOnDeviceRecognizer", "Starting recognition")
        
        // Destroy any existing recognizer
        destroy()
        
        // Small delay to prevent "Recognizer busy" errors
        Thread.sleep(100)
        
        // Create on-device recognizer
        speechRecognizer = SpeechRecognizer.createOnDeviceSpeechRecognizer(appContext)
        
        currentListener = OnDeviceRecognitionListener(onPartial, onFinal, onError)
        speechRecognizer?.setRecognitionListener(currentListener)
        
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, appContext.packageName)
            // Request on-device only
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
        }
        
        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Logger.e("AndroidOnDeviceRecognizer", "Failed to start listening", e)
            onError(e)
        }
    }
    
    override fun stop() {
        Logger.d("AndroidOnDeviceRecognizer", "Stopping recognition")
        speechRecognizer?.stopListening()
    }
    
    override fun cancel() {
        Logger.d("AndroidOnDeviceRecognizer", "Cancelling recognition")
        speechRecognizer?.cancel()
        currentListener = null
    }
    
    override fun destroy() {
        Logger.d("AndroidOnDeviceRecognizer", "Destroying recognizer")
        speechRecognizer?.destroy()
        speechRecognizer = null
        currentListener = null
    }
    
    private inner class OnDeviceRecognitionListener(
        private val onPartial: (String) -> Unit,
        private val onFinal: (String) -> Unit,
        private val onError: (Throwable) -> Unit
    ) : RecognitionListener {
        
        override fun onReadyForSpeech(params: Bundle?) {
            Logger.d("AndroidOnDeviceRecognizer", "Ready for speech")
        }
        
        override fun onBeginningOfSpeech() {
            Logger.d("AndroidOnDeviceRecognizer", "Beginning of speech")
        }
        
        override fun onRmsChanged(rmsdB: Float) {
            // Optional: could expose this for visual feedback
        }
        
        override fun onBufferReceived(buffer: ByteArray?) {
            // Not used
        }
        
        override fun onEndOfSpeech() {
            Logger.d("AndroidOnDeviceRecognizer", "End of speech")
        }
        
        override fun onError(error: Int) {
            val errorMsg = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                SpeechRecognizer.ERROR_NETWORK -> "Network error"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                SpeechRecognizer.ERROR_NO_MATCH -> "No speech match"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy"
                SpeechRecognizer.ERROR_SERVER -> "Server error"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout"
                else -> "Unknown error: $error"
            }
            Logger.e("AndroidOnDeviceRecognizer", "Recognition error: $errorMsg")
            onError(RuntimeException("Speech recognition error: $errorMsg"))
        }
        
        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val finalText = matches?.firstOrNull() ?: ""
            Logger.d("AndroidOnDeviceRecognizer", "Final result: $finalText")
            onFinal(finalText)
        }
        
        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val partialText = matches?.firstOrNull() ?: ""
            if (partialText.isNotEmpty()) {
                onPartial(partialText)
            }
        }
        
        override fun onEvent(eventType: Int, params: Bundle?) {
            // Not used
        }
    }
}
