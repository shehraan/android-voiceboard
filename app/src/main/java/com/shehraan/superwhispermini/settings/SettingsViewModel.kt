package com.shehraan.superwhispermini.settings

import android.app.Application
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shehraan.superwhispermini.SuperwhisperMiniApp
import com.shehraan.superwhispermini.formatting.DictationMode
import com.shehraan.superwhispermini.history.DictationHistoryEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * ViewModel for settings screen.
 */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val context = application.applicationContext
    private val dataStore = context.dataStore
    
    private val historyRepository = (application as SuperwhisperMiniApp).historyRepository
    
    private val _isKeyboardEnabled = MutableStateFlow(false)
    val isKeyboardEnabled: StateFlow<Boolean> = _isKeyboardEnabled
    
    private val _isKeyboardDefault = MutableStateFlow(false)
    val isKeyboardDefault: StateFlow<Boolean> = _isKeyboardDefault
    
    private val _historyEntries = MutableStateFlow<List<DictationHistoryEntry>>(emptyList())
    val historyEntries: StateFlow<List<DictationHistoryEntry>> = _historyEntries
    
    val currentMode: Flow<DictationMode> = dataStore.data.map { prefs ->
        val modeName = prefs[PreferencesKeys.DICTATION_MODE] ?: DictationMode.VOICE.name
        DictationMode.valueOf(modeName)
    }
    
    init {
        refreshKeyboardStatus()
        loadHistory()
    }
    
    /**
     * Check if keyboard is enabled in system settings.
     */
    fun refreshKeyboardStatus() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val enabledInputMethods = imm.enabledInputMethodList
        val packageName = context.packageName
        
        _isKeyboardEnabled.value = enabledInputMethods.any { 
            it.packageName == packageName 
        }
    }
    
    /**
     * Open system settings to enable keyboard.
     */
    fun openKeyboardSettings(): Intent {
        return Intent(Settings.ACTION_INPUT_METHOD_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
    
    /**
     * Show input method picker.
     */
    fun showInputMethodPicker(): Intent? {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showInputMethodPicker()
        return null
    }
    
    /**
     * Set the current dictation mode.
     */
    fun setDictationMode(mode: DictationMode) {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[PreferencesKeys.DICTATION_MODE] = mode.name
            }
        }
    }
    
    /**
     * Load recent history entries.
     */
    fun loadHistory() {
        viewModelScope.launch {
            historyRepository.getRecentEntries(50).collect { entries ->
                _historyEntries.value = entries
            }
        }
    }
    
    /**
     * Clear all history.
     */
    fun clearHistory() {
        historyRepository.deleteAll()
    }
    
    /**
     * Delete a specific history entry.
     */
    fun deleteHistoryEntry(id: Long) {
        historyRepository.deleteEntry(id)
    }
    
    private object PreferencesKeys {
        val DICTATION_MODE = stringPreferencesKey("dictation_mode")
    }
}
