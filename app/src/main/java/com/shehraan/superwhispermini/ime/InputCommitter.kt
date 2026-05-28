package com.shehraan.superwhispermini.ime

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.inputmethod.InputConnection
import com.shehraan.superwhispermini.util.Logger

/**
 * Handles committing text to the focused input field.
 * Primary method is direct insertion via InputConnection.
 * Falls back to clipboard if direct insertion fails.
 */
class InputCommitter(context: Context) {
    
    private val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    
    /**
     * Commit text to the current input connection.
     * 
     * @param inputConnection The current input connection
     * @param text The text to commit
     * @return true if committed directly, false if fell back to clipboard
     */
    fun commitText(inputConnection: InputConnection?, text: String): Boolean {
        if (text.isEmpty()) {
            Logger.d("InputCommitter", "Empty text, skipping")
            return true
        }
        
        if (inputConnection == null) {
            Logger.w("InputCommitter", "No input connection, using clipboard fallback")
            fallbackToClipboard(text)
            return false
        }
        
        // Try direct insertion first
        val committed = inputConnection.commitText(text, 1)
        
        if (committed) {
            Logger.d("InputCommitter", "Text committed directly: $text")
            return true
        } else {
            Logger.w("InputCommitter", "Direct commit failed, using clipboard fallback")
            fallbackToClipboard(text)
            return false
        }
    }
    
    /**
     * Copy text to clipboard as fallback.
     */
    private fun fallbackToClipboard(text: String) {
        try {
            val clip = ClipData.newPlainText("Superwhisper Mini", text)
            clipboardManager.setPrimaryClip(clip)
            Logger.i("InputCommitter", "Text copied to clipboard: $text")
        } catch (e: Exception) {
            Logger.e("InputCommitter", "Failed to copy to clipboard", e)
        }
    }
    
    /**
     * Get clipboard text.
     */
    fun getClipboardText(): String? {
        return clipboardManager.primaryClip
            ?.getItemAt(0)
            ?.text
            ?.toString()
    }
}
