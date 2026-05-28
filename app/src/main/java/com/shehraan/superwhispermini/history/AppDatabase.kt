package com.shehraan.superwhispermini.history

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.shehraan.superwhispermini.formatting.DictationMode

@Database(entities = [DictationHistoryEntry::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "superwhispermini_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class Converters {
    @TypeConverter
    fun fromDictationMode(value: DictationMode): String {
        return value.name
    }
    
    @TypeConverter
    fun toDictationMode(value: String): DictationMode {
        return try {
            DictationMode.valueOf(value)
        } catch (e: IllegalArgumentException) {
            // Handle old database entries that used "VOICE" instead of "RAW"
            if (value == "VOICE") DictationMode.RAW else DictationMode.RAW
        }
    }
    
    @TypeConverter
    fun fromDictationStatus(value: DictationStatus): String {
        return value.name
    }
    
    @TypeConverter
    fun toDictationStatus(value: String): DictationStatus {
        return DictationStatus.valueOf(value)
    }
}
