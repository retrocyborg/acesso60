package com.acesso60.voice

interface SpeechToTextEngine {
    fun startListening()
    fun stopListening()
    fun cancel()
}
