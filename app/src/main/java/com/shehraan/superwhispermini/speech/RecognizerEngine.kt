package com.shehraan.superwhispermini.speech

/**
 * Abstract interface for speech recognition engines.
 * Allows swapping between Android's built-in recognizer and future
 * implementations like WhisperCpp or Sherpa-Onnx without changing IME code.
 */
interface RecognizerEngine {
    
    /**
     * Check if this engine is available on the device.
     */
    fun isAvailable(): Boolean
    
    /**
     * Start recognition.
     * 
     * @param onPartial Called with interim results while speaking
     * @param onFinal Called with final transcript when recognition completes
     * @param onError Called if an error occurs during recognition
     */
    fun start(
        onPartial: (String) -> Unit,
        onFinal: (String) -> Unit,
        onError: (Throwable) -> Unit
    )
    
    /**
     * Stop recognition and get final results.
     */
    fun stop()
    
    /**
     * Cancel recognition without getting results.
     */
    fun cancel()
    
    /**
     * Clean up resources. Called when the engine is no longer needed.
     */
    fun destroy()
}
