package com.shehraan.superwhispermini.util

/**
 * Simple time metrics helper for tracking latency.
 */
class TimeMetrics {
    
    private var startTime: Long = 0
    
    /**
     * Start timing.
     */
    fun start() {
        startTime = System.currentTimeMillis()
    }
    
    /**
     * Get elapsed time since start in milliseconds.
     */
    fun elapsedMillis(): Long {
        return System.currentTimeMillis() - startTime
    }
    
    /**
     * Reset and start again.
     */
    fun restart() {
        start()
    }
    
    companion object {
        /**
         * Create and start a new TimeMetrics instance.
         */
        fun startNow(): TimeMetrics {
            return TimeMetrics().apply { start() }
        }
    }
}
