package com.shehraan.superwhispermini.history

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: DictationHistoryEntry): Long
    
    @Query("SELECT * FROM dictation_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentEntries(limit: Int = 100): Flow<List<DictationHistoryEntry>>
    
    @Query("SELECT * FROM dictation_history WHERE id = :id")
    suspend fun getEntryById(id: Long): DictationHistoryEntry?
    
    @Query("DELETE FROM dictation_history WHERE id = :id")
    suspend fun deleteEntry(id: Long)
    
    @Query("DELETE FROM dictation_history")
    suspend fun deleteAll()
    
    @Query("SELECT COUNT(*) FROM dictation_history")
    suspend fun getCount(): Int
}
