package com.shehraan.superwhispermini.formatting

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONObject

/**
 * Handles custom text replacements / mini vocabulary mapping.
 * Stores replacements in DataStore as JSON.
 */
class ReplacementEngine(context: Context) {
    
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "replacements")
    private val dataStore = context.dataStore
    
    private val replacementsKey = stringPreferencesKey("replacement_map")
    
    // Default replacements - minimal vocabulary
    private val defaultReplacements = mapOf(
        "new line" to "\n",
        "new paragraph" to "\n\n",
        "comma" to ",",
        "period" to ".",
        "question mark" to "?",
        "exclamation point" to "!",
        "colon" to ":",
        "semicolon" to ";",
        "dash" to "-",
        "open quote" to """""",
        "close quote" to """""",
        "quote" to """"""
    )
    
    /**
     * Apply replacements to the given text.
     */
    fun applyReplacements(text: String): String {
        val replacements = getAllReplacements()
        var result = text
        
        // Apply each replacement (case insensitive)
        replacements.forEach { (spoken, written) ->
            val regex = Regex("\\b\\Q$spoken\\E\\b", RegexOption.IGNORE_CASE)
            result = result.replace(regex, written)
        }
        
        return result
    }
    
    /**
     * Get all replacements including defaults and custom.
     */
    fun getAllReplacements(): Map<String, String> {
        val custom = runBlocking { getCustomReplacements() }
        return defaultReplacements + custom
    }
    
    /**
     * Add a custom replacement.
     */
    suspend fun addReplacement(spoken: String, written: String) {
        dataStore.edit { preferences ->
            val current = preferences[replacementsKey]?.let { 
                JSONObject(it).toMap() 
            } ?: emptyMap()
            
            val updated = current.toMutableMap()
            updated[spoken.lowercase()] = written
            
            preferences[replacementsKey] = mapToJson(updated).toString()
        }
    }
    
    /**
     * Remove a custom replacement.
     */
    suspend fun removeReplacement(spoken: String) {
        dataStore.edit { preferences ->
            val current = preferences[replacementsKey]?.let { 
                JSONObject(it).toMap() 
            } ?: emptyMap()
            
            val updated = current.toMutableMap()
            updated.remove(spoken.lowercase())
            
            preferences[replacementsKey] = mapToJson(updated).toString()
        }
    }
    
    /**
     * Get only custom replacements.
     */
    private suspend fun getCustomReplacements(): Map<String, String> {
        val jsonString = dataStore.data.first()[replacementsKey] ?: return emptyMap()
        return JSONObject(jsonString).toMap()
    }
    
    private fun JSONObject.toMap(): Map<String, String> {
        val map = mutableMapOf<String, String>()
        keys().forEach { key ->
            map[key] = getString(key)
        }
        return map
    }
    
    private fun mapToJson(map: Map<String, String>): JSONObject {
        val json = JSONObject()
        map.forEach { (k, v) ->
            json.put(k, v)
        }
        return json
    }
}
