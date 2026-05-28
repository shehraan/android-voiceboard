package com.shehraan.superwhispermini.formatting

/**
 * Message Mode: conservative formatting for messaging contexts.
 * Removes filler words and applies basic formatting.
 */
class MessageModeFormatter : TextFormatter {
    
    // Conservative list of filler words commonly spoken but not needed in text
    private val fillerWords = setOf(
        "um", "uh", "like", "you know", "sort of", "kind of",
        "basically", "literally", "actually", "honestly"
    )
    
    override fun format(text: String): String {
        if (text.isEmpty()) return text
        
        var result = text.trim()
        
        // Remove filler words (case insensitive)
        fillerWords.forEach { filler ->
            val regex = Regex("\\b$filler\\b", RegexOption.IGNORE_CASE)
            result = result.replace(regex, "")
        }
        
        // Fix multiple spaces caused by filler removal
        result = result.replace(Regex("\\s+"), " ").trim()
        
        // Capitalize first letter
        if (result.isNotEmpty()) {
            result = result[0].uppercaseChar() + result.substring(1)
        }
        
        // Add period if no ending punctuation
        if (result.isNotEmpty() && !result.endsWithAny(".", "!", "?")) {
            result += "."
        }
        
        return result
    }
    
    private fun String.endsWithAny(vararg suffixes: String): Boolean {
        return suffixes.any { this.endsWith(it) }
    }
}
