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
 * 
 * This implementation reuses the same SpeechRecognizer instance to avoid
 * ERROR_RECOGNIZER_BUSY (error 11) which occurs when creating/destroying
 * too quickly.
 */
class AndroidOnDeviceRecognizer(context: Context) : RecognizerEngine {

    private val appContext = context.applicationContext
    private var currentListener: OnDeviceRecognitionListener? = null
    private var isRecognizing = false
    
    companion object {
        private var sharedRecognizer: SpeechRecognizer? = null
        private var sharedContext: android.content.Context? = null
        
        @Synchronized
        fun getSharedRecognizer(context: android.content.Context): SpeechRecognizer? {
            if (sharedRecognizer == null || sharedContext !== context.applicationContext) {
                sharedContext = context.applicationContext
                sharedRecognizer = SpeechRecognizer.createOnDeviceSpeechRecognizer(sharedContext!!)
            }
            return sharedRecognizer
        }
        
        @Synchronized
        fun destroyShared() {
            sharedRecognizer?.destroy()
            sharedRecognizer = null
            sharedContext = null
        }
    }
    
    override fun isAvailable(): Boolean {
        return SpeechRecognizer.isOnDeviceRecognitionAvailable(appContext)
    }
    
    override fun start(
        onPartial: (String) -> Unit,
        onFinal: (String) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        Logger.d("AndroidOnDeviceRecognizer", "Starting recognition")
        
        // If already recognizing, stop first
        if (isRecognizing) {
            Logger.d("AndroidOnDeviceRecognizer", "Already recognizing, stopping first")
            stop()
            // Wait for it to fully stop
            Thread.sleep(300)
        }
        
        val recognizer = getSharedRecognizer(appContext)
        if (recognizer == null) {
            onError(RuntimeException("Failed to create SpeechRecognizer"))
            return
        }
        
        currentListener = OnDeviceRecognitionListener(onPartial, onFinal, onError)
        recognizer.setRecognitionListener(currentListener)
        
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, appContext.packageName)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
        }
        
        try {
            recognizer.startListening(intent)
            isRecognizing = true
        } catch (e: Exception) {
            Logger.e("AndroidOnDeviceRecognizer", "Failed to start listening", e)
            isRecognizing = false
            onError(e)
        }
    }
    
    override fun stop() {
        Logger.d("AndroidOnDeviceRecognizer", "Stopping recognition")
        if (!isRecognizing) {
            Logger.d("AndroidOnDeviceRecognizer", "Not currently recognizing")
            return
        }
        
        try {
            sharedRecognizer?.stopListening()
        } catch (e: Exception) {
            Logger.e("AndroidOnDeviceRecognizer", "Error stopping listening", e)
        }
        isRecognizing = false
    }
    
    override fun cancel() {
        Logger.d("AndroidOnDeviceRecognizer", "Cancelling recognition")
        try {
            sharedRecognizer?.cancel()
        } catch (e: Exception) {
            Logger.e("AndroidOnDeviceRecognizer", "Error cancelling", e)
        }
        isRecognizing = false
        currentListener = null
    }
    
    override fun destroy() {
        Logger.d("AndroidOnDeviceRecognizer", "Destroying recognizer")
        destroyShared()
        isRecognizing = false
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
            // Volume level changed - could use for visual feedback
        }
        
        override fun onBufferReceived(buffer: ByteArray?) {}
        
        override fun onEndOfSpeech() {
            Logger.d("AndroidOnDeviceRecognizer", "End of speech")
            isRecognizing = false
        }
        
        override fun onError(error: Int) {
            isRecognizing = false
            val errorMessage = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Audio error"
                SpeechRecognizer.ERROR_CLIENT -> "Client error"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                SpeechRecognizer.ERROR_NETWORK -> "Network error"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                SpeechRecognizer.ERROR_NO_MATCH -> "No speech match"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy"
                SpeechRecognizer.ERROR_SERVER -> "Server error"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout"
                else -> "Unknown error: $error"
            }
            Logger.e("AndroidOnDeviceRecognizer", "Recognition error: $errorMessage")
            onError(RuntimeException("Speech recognition error: $errorMessage"))
        }
        
        override fun onResults(results: Bundle?) {
            isRecognizing = false
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                val text = matches[0]
                Logger.d("AndroidOnDeviceRecognizer", "Final result: $text")
                onFinal(text)
            } else {
                Logger.d("AndroidOnDeviceRecognizer", "No results")
                onFinal("")
            }
        }
        
        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                val text = matches[0]
                Logger.d("AndroidOnDeviceRecognizer", "Partial result: $text")
                onPartial(text)
            }
        }
        
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }
}
