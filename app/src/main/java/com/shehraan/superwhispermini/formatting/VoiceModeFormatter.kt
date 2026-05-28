package com.shehraan.superwhispermini.formatting

/**
 * Voice Mode: minimal cleanup.
 * Just basic capitalization and spacing fixes.
 */
class VoiceModeFormatter : TextFormatter {
    
    override fun format(text: String): String {
        if (text.isEmpty()) return text
        
        return text
            .trim()
            // Ensure sentence starts with capital letter
            .capitalizeSentences()
            // Fix double spaces
            .replace(Regex("\\s+"), " ")
    }
    
    private fun String.capitalizeSentences(): String {
        if (this.isEmpty()) return this
        
        val sb = StringBuilder()
        var capitalizeNext = true
        
        for (char in this) {
            if (capitalizeNext && char.isLetter()) {
                sb.append(char.uppercaseChar())
                capitalizeNext = false
            } else {
                sb.append(char)
            }
            
            if (char == '.' || char == '!' || char == '?') {
                capitalizeNext = true
            } else if (!char.isWhitespace() && char != '.' && char != '!' && char != '?') {
                // Reset if we see a non-whitespace after sentence end
                // but actually we need to wait for next letter
            }
        }
        
        return sb.toString()
    }
}
