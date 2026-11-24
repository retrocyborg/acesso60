package com.acesso60.voice

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/** Fala um texto e avisa quando terminou (necessário para só ouvir depois que a fala acabar). */
interface TextSpeaker {
    /** [onDone] é chamado em qualquer thread, inclusive quando a fala falha ou não está disponível. */
    fun speak(text: String, onDone: () -> Unit = {})
    fun stop()
    fun shutdown()
}

/** Chamada de baixo nível ao [TextToSpeech], com identificador de enunciado único. */
object SafeTtsSpeaker {
    fun speak(
        tts: TextToSpeech?,
        prompt: String,
        queueMode: Int = TextToSpeech.QUEUE_FLUSH,
        utteranceId: String = UUID.randomUUID().toString(),
    ): String? {
        if (tts == null || prompt.isBlank()) return null
        return if (tts.speak(prompt, queueMode, Bundle(), utteranceId) == TextToSpeech.SUCCESS) utteranceId else null
    }
}

/** Implementação Android em pt-BR. Se o motor de voz não estiver pronto, o fluxo segue sem áudio. */
class AndroidTextSpeaker(context: Context) : TextSpeaker, TextToSpeech.OnInitListener {
    private val callbacks = ConcurrentHashMap<String, () -> Unit>()
    private val lock = Any()
    private var ready = false
    private var failed = false
    private var pending: Pair<String, () -> Unit>? = null
    private val tts = TextToSpeech(context.applicationContext, this)

    init {
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) = Unit
            override fun onDone(utteranceId: String?) = finish(utteranceId)

            @Suppress("OVERRIDE_DEPRECATION")
            override fun onError(utteranceId: String?) = finish(utteranceId)
            override fun onError(utteranceId: String?, errorCode: Int) = finish(utteranceId)
            override fun onStop(utteranceId: String?, interrupted: Boolean) {
                // stop() cancela de propósito: não dispara o próximo passo do diálogo.
                if (utteranceId != null) callbacks.remove(utteranceId)
            }
        })
    }

    override fun onInit(status: Int) {
        val toSpeak: Pair<String, () -> Unit>?
        synchronized(lock) {
            val language = if (status == TextToSpeech.SUCCESS) tts.setLanguage(Locale("pt", "BR")) else TextToSpeech.ERROR
            ready = status == TextToSpeech.SUCCESS &&
                language != TextToSpeech.LANG_MISSING_DATA && language != TextToSpeech.LANG_NOT_SUPPORTED
            failed = !ready
            toSpeak = pending
            pending = null
        }
        toSpeak?.let { (text, done) -> speak(text, done) }
    }

    override fun speak(text: String, onDone: () -> Unit) {
        synchronized(lock) {
            if (!ready && !failed) {
                pending = text to onDone
                return
            }
        }
        if (!ready || text.isBlank()) {
            onDone()
            return
        }
        val id = UUID.randomUUID().toString()
        callbacks[id] = onDone
        if (SafeTtsSpeaker.speak(tts, text, TextToSpeech.QUEUE_FLUSH, id) == null) {
            callbacks.remove(id)?.invoke()
        }
    }

    override fun stop() {
        synchronized(lock) { pending = null }
        callbacks.clear()
        tts.stop()
    }

    override fun shutdown() {
        stop()
        tts.shutdown()
    }

    private fun finish(utteranceId: String?) {
        if (utteranceId != null) callbacks.remove(utteranceId)?.invoke()
    }
}
