package com.acesso60.voice

/** Contrato testável para o cliente Android de reconhecimento de fala. */
interface RecognizerClient {
    interface Listener {
        fun onPartialResult(text: String)
        fun onFinalResult(text: String)
        fun onError(reason: SpeechFailure)
    }

    fun setListener(listener: Listener)
    fun startListening()
    fun stopListening()
    fun cancel()
    fun destroy()
}
