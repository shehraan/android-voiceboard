package com.shehraan.superwhispermini.history

import android.content.Context
import com.shehraan.superwhispermini.formatting.DictationMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Repository for dictation history.
 * Provides suspend functions and flow for UI consumption.
 */
class HistoryRepository(context: Context) {
    
    private val database = AppDatabase.getDatabase(context)
    private val historyDao = database.historyDao()
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    /**
     * Save a new dictation entry.
     */
    fun saveEntry(
        rawText: String,
        finalText: String,
        mode: DictationMode,
        status: DictationStatus,
        latencyMillis: Long
    ) {
        val entry = DictationHistoryEntry(
            rawText = rawText,
            finalText = finalText,
            mode = mode,
            timestamp = System.currentTimeMillis(),
            status = status,
            latencyMillis = latencyMillis
        )
        
        repositoryScope.launch {
            historyDao.insert(entry)
        }
    }
    
    /**
     * Get recent entries as a Flow.
     */
    fun getRecentEntries(limit: Int = 100): Flow<List<DictationHistoryEntry>> {
        return historyDao.getRecentEntries(limit)
    }
    
    /**
     * Delete an entry by ID.
     */
    fun deleteEntry(id: Long) {
        repositoryScope.launch {
            historyDao.deleteEntry(id)
        }
    }
    
    /**
     * Delete all entries.
     */
    fun deleteAll() {
        repositoryScope.launch {
            historyDao.deleteAll()
        }
    }
    
    /**
     * Get entry count (async).
     */
    suspend fun getCount(): Int = withContext(Dispatchers.IO) {
        historyDao.getCount()
    }
}
