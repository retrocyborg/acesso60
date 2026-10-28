package com.acesso60.voice

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AndroidSpeechToTextEngineTest {
    @Test
    fun `unavailable recognizer reports an error without attempting to listen`() {
        val engine = AndroidSpeechToTextEngine(null, null)
        val listener = RecordingListener()

        engine.startListening(listener)

        assertEquals(listOf(SpeechFailure.RECOGNIZER_UNAVAILABLE), listener.errors)
    }

    @Test
    fun `unavailable audio input reports an error without attempting to listen`() {
        val recognizer = FakeRecognizerClient()
        val engine = AndroidSpeechToTextEngine(null, recognizer, AudioInputProbe { false })
        val listener = RecordingListener()

        engine.startListening(listener)

        assertEquals(listOf(SpeechFailure.AUDIO_UNAVAILABLE), listener.errors)
        assertEquals(0, recognizer.startCalls)
    }

    @Test
    fun `available audio input allows recognition to start`() {
        val recognizer = FakeRecognizerClient()
        val engine = AndroidSpeechToTextEngine(null, recognizer, AudioInputProbe { true })

        engine.startListening(RecordingListener())

        assertEquals(1, recognizer.startCalls)
    }

    @Test
    fun `forwards non-empty partial and final results`() {
        val recognizer = FakeRecognizerClient()
        val engine = AndroidSpeechToTextEngine(null, recognizer)
        val listener = RecordingListener()

        engine.startListening(listener)
        recognizer.emitPartial("  vol")
        recognizer.emitFinal(" voltar ")

        assertEquals(listOf("vol"), listener.partial)
        assertEquals(listOf("voltar"), listener.final)
    }

    @Test
    fun `empty results do not reach the application`() {
        val recognizer = FakeRecognizerClient()
        val engine = AndroidSpeechToTextEngine(null, recognizer)
        val listener = RecordingListener()

        engine.startListening(listener)
        recognizer.emitPartial("")
        recognizer.emitFinal("   ")

        assertTrue(listener.partial.isEmpty())
        assertTrue(listener.final.isEmpty())
    }

    @Test
    fun `recognizer errors are converted to a generic listener error`() {
        val recognizer = FakeRecognizerClient()
        val engine = AndroidSpeechToTextEngine(null, recognizer)
        val listener = RecordingListener()

        engine.startListening(listener)
        recognizer.emitError(SpeechFailure.UNKNOWN)

        assertEquals(listOf(SpeechFailure.UNKNOWN), listener.errors)
    }

    @Test
    fun `whitespace around partial and final results is normalized`() {
        val recognizer = FakeRecognizerClient()
        val engine = AndroidSpeechToTextEngine(null, recognizer)
        val listener = RecordingListener()

        engine.startListening(listener)
        recognizer.emitPartial("  inicio  ")
        recognizer.emitFinal("  voltar  ")

        assertEquals(listOf("inicio"), listener.partial)
        assertEquals(listOf("voltar"), listener.final)
    }

    @Test
    fun `lifecycle operations are delegated`() {
        val recognizer = FakeRecognizerClient()
        val engine = AndroidSpeechToTextEngine(null, recognizer)

        engine.startListening(RecordingListener())
        engine.stopListening()
        engine.cancel()
        engine.destroy()

        assertEquals(1, recognizer.startCalls)
        assertEquals(1, recognizer.stopCalls)
        assertEquals(1, recognizer.cancelCalls)
        assertEquals(1, recognizer.destroyCalls)
    }

    private class RecordingListener : SpeechToTextEngine.Listener {
        val partial = mutableListOf<String>()
        val final = mutableListOf<String>()
        val errors = mutableListOf<SpeechFailure>()
        override fun onPartialResult(text: String) { partial += text }
        override fun onFinalResult(text: String) { final += text }
        override fun onError(reason: SpeechFailure) { errors += reason }
    }

    private class FakeRecognizerClient : RecognizerClient {
        private var listener: RecognizerClient.Listener? = null
        var startCalls = 0
        var stopCalls = 0
        var cancelCalls = 0
        var destroyCalls = 0
        override fun setListener(listener: RecognizerClient.Listener) { this.listener = listener }
        override fun startListening() { startCalls++ }
        override fun stopListening() { stopCalls++ }
        override fun cancel() { cancelCalls++ }
        override fun destroy() { destroyCalls++ }
        fun emitPartial(text: String) { listener?.onPartialResult(text) }
        fun emitFinal(text: String) { listener?.onFinalResult(text) }
        fun emitError(reason: SpeechFailure) { listener?.onError(reason) }
    }
}
