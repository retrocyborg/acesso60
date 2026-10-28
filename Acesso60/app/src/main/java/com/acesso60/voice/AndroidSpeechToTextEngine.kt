package com.acesso60.voice

import android.content.Context

/** Adaptador Android do reconhecimento de voz; a lógica é testável via RecognizerClient. */
class AndroidSpeechToTextEngine(
    context: Context?,
    private val recognizer: RecognizerClient? = context?.let(AndroidRecognizerClient::create),
    private val audioInputProbe: AudioInputProbe? = context?.let(AudioInputProbe::from),
) : SpeechToTextEngine {

    override fun startListening(listener: SpeechToTextEngine.Listener) {
        val current = recognizer ?: run {
            listener.onError(SpeechFailure.RECOGNIZER_UNAVAILABLE)
            return
        }
        if (audioInputProbe?.hasInputDevice() == false) {
            listener.onError(SpeechFailure.AUDIO_UNAVAILABLE)
            return
        }
        current.setListener(object : RecognizerClient.Listener {
            override fun onPartialResult(text: String) {
                text.trim().takeIf(String::isNotEmpty)?.let(listener::onPartialResult)
            }

            override fun onFinalResult(text: String) {
                text.trim().takeIf(String::isNotEmpty)?.let(listener::onFinalResult)
            }

            override fun onError(reason: SpeechFailure) = listener.onError(reason)
        })
        current.startListening()
    }

    override fun stopListening() {
        recognizer?.stopListening()
    }

    override fun cancel() {
        recognizer?.cancel()
    }

    override fun destroy() {
        recognizer?.destroy()
    }
}
