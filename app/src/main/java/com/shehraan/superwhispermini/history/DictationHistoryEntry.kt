package com.shehraan.superwhispermini.history

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.shehraan.superwhispermini.formatting.DictationMode

/**
 * Data class representing a dictation history entry.
 * Stored in Room database.
 */
@Entity(tableName = "dictation_history")
data class DictationHistoryEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val rawText: String,
    val finalText: String,
    val mode: DictationMode,
    val timestamp: Long,
    val status: DictationStatus,
    val latencyMillis: Long
)

enum class DictationStatus {
    SUCCESS,
    FAILED,
    CANCELLED
}
