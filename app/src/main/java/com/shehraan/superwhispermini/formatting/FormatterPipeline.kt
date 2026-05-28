package com.shehraan.superwhispermini.formatting

import android.content.Context

/**
 * Enum for dictation modes.
 */
enum class DictationMode {
    RAW,        // Minimal cleanup
    MESSAGE     // Conservative formatting with filler removal
}

/**
 * Orchestrates the formatting pipeline:
 * 1. Mode-specific formatting (Raw or Message mode)
 * 2. Custom replacements application
 */
class FormatterPipeline(context: Context) {
    
    private val voiceFormatter = VoiceModeFormatter()
    private val messageFormatter = MessageModeFormatter()
    private val replacementEngine = ReplacementEngine(context)
    
    /**
     * Format text according to the selected mode and apply replacements.
     * 
     * @param text The raw transcription
     * @param mode The dictation mode
     * @return Formatted text with replacements applied
     */
    fun format(text: String, mode: DictationMode = DictationMode.RAW): String {
        // Step 1: Apply replacements first (so "period" becomes "." before sentence capitalization)
        val withReplacements = replacementEngine.applyReplacements(text)
        
        // Step 2: Apply mode-specific formatting
        val modeFormatted = when (mode) {
            DictationMode.RAW -> voiceFormatter.format(withReplacements)
            DictationMode.MESSAGE -> messageFormatter.format(withReplacements)
        }
        
        return modeFormatted
    }
    
    /**
     * Format text with Raw mode (convenience method).
     */
    fun formatRaw(text: String): String = format(text, DictationMode.RAW)
    
    /**
     * Format text with Message mode (convenience method).
     */
    fun formatMessage(text: String): String = format(text, DictationMode.MESSAGE)
}
