package com.shehraan.superwhispermini.speech

sealed class RecognitionState {
    data object Idle : RecognitionState()
    data object Listening : RecognitionState()
    data class Partial(val text: String) : RecognitionState()
    data class Final(val text: String) : RecognitionState()
    data class Error(val throwable: Throwable) : RecognitionState()
}
