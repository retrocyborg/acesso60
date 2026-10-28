package com.acesso60.voice

enum class SpeechFailure {
    RECOGNIZER_UNAVAILABLE,
    AUDIO_UNAVAILABLE,
    NO_SPEECH_DETECTED,
    LANGUAGE_UNAVAILABLE,
    NETWORK_UNAVAILABLE,
    RECOGNIZER_BUSY,
    PERMISSION_DENIED,
    UNKNOWN,
}

interface SpeechToTextEngine {
    interface Listener {
        fun onPartialResult(text: String)
        fun onFinalResult(text: String)
        fun onError(reason: SpeechFailure)
    }

    fun startListening(listener: Listener)
    fun stopListening()
    fun cancel()
    fun destroy() = Unit
}
