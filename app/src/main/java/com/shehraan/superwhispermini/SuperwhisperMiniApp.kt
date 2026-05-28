package com.shehraan.superwhispermini

import android.app.Application
import com.shehraan.superwhispermini.history.HistoryRepository

class SuperwhisperMiniApp : Application() {
    
    lateinit var historyRepository: HistoryRepository
        private set
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        historyRepository = HistoryRepository(this)
    }
    
    companion object {
        lateinit var instance: SuperwhisperMiniApp
            private set
    }
}
