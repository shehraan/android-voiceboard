package com.shehraan.superwhispermini.util

import android.util.Log

/**
 * Simple logging wrapper for the app.
 */
object Logger {
    
    private const val TAG = "SuperwhisperMini"
    private var isDebugMode = true
    
    fun d(tag: String, message: String) {
        if (isDebugMode) {
            Log.d("$TAG:$tag", message)
        }
    }
    
    fun i(tag: String, message: String) {
        Log.i("$TAG:$tag", message)
    }
    
    fun w(tag: String, message: String) {
        Log.w("$TAG:$tag", message)
    }
    
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e("$TAG:$tag", message, throwable)
        } else {
            Log.e("$TAG:$tag", message)
        }
    }
    
    fun setDebugMode(enabled: Boolean) {
        isDebugMode = enabled
    }
}
