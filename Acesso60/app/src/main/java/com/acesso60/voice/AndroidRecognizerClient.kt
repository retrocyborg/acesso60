package com.acesso60.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log

/** Adaptador fino entre SpeechRecognizer e o contrato testável do módulo voice. */
class AndroidRecognizerClient private constructor(
    private val recognizer: SpeechRecognizer,
) : RecognizerClient {
    override fun setListener(listener: RecognizerClient.Listener) {
        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) = Unit
            override fun onBeginningOfSpeech() = Unit
            override fun onRmsChanged(rmsdB: Float) = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() = Unit
            override fun onEvent(eventType: Int, params: Bundle?) = Unit
            override fun onPartialResults(results: Bundle?) = listener.onPartialResult(results.firstText())
            override fun onResults(results: Bundle?) = listener.onFinalResult(results.firstText())
            override fun onError(error: Int) {
                Log.i(TAG, "SpeechRecognizer error=$error mapped=${mapSpeechError(error)} locale=pt-BR")
                listener.onError(mapSpeechError(error))
            }
        })
    }

    override fun startListening() = recognizer.startListening(
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        },
    )
    override fun stopListening() = recognizer.stopListening()
    override fun cancel() = recognizer.cancel()
    override fun destroy() = recognizer.destroy()

    companion object {
        private const val TAG = "Acesso60.STT"

        fun create(context: Context): RecognizerClient? =
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                AndroidRecognizerClient(SpeechRecognizer.createSpeechRecognizer(context.applicationContext))
            } else {
                null
            }
    }
}

private fun mapSpeechError(error: Int): SpeechFailure = when (error) {
    SpeechRecognizer.ERROR_AUDIO -> SpeechFailure.AUDIO_UNAVAILABLE
    SpeechRecognizer.ERROR_NO_MATCH -> SpeechFailure.NO_SPEECH_DETECTED
    SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED,
    SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE -> SpeechFailure.LANGUAGE_UNAVAILABLE
    SpeechRecognizer.ERROR_NETWORK,
    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> SpeechFailure.NETWORK_UNAVAILABLE
    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> SpeechFailure.RECOGNIZER_BUSY
    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> SpeechFailure.PERMISSION_DENIED
    else -> SpeechFailure.UNKNOWN
}

private fun Bundle?.firstText(): String = this
    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
    ?.firstOrNull()
    ?.trim()
    .orEmpty()
