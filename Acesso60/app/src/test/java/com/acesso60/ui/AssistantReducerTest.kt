package com.acesso60.ui

import com.acesso60.ui.state.AssistantEvent
import com.acesso60.ui.state.AssistantReducer
import com.acesso60.ui.state.AssistantScreenState.Confirming
import com.acesso60.ui.state.AssistantScreenState.Idle
import com.acesso60.ui.state.AssistantScreenState.Listening
import com.acesso60.ui.state.AssistantScreenState.Result
import com.acesso60.ui.state.AssistantScreenState.Working
import com.acesso60.ui.state.FailureReason
import com.acesso60.ui.state.Outcome
import org.junit.Assert.assertEquals
import org.junit.Test

class AssistantReducerTest {

    @Test
    fun `ListeningStarted from idle starts listening`() {
        assertEquals(Listening(), AssistantReducer.reduce(Idle, AssistantEvent.ListeningStarted))
    }

    @Test
    fun `ListeningStarted echo keeps the transcript already shown`() {
        val current = Listening("voltar")
        assertEquals(current, AssistantReducer.reduce(current, AssistantEvent.ListeningStarted))
    }

    @Test
    fun `transcript updates only while listening`() {
        assertEquals(
            Listening("abrir"),
            AssistantReducer.reduce(Listening(), AssistantEvent.TranscriptUpdated("abrir")),
        )
        assertEquals(Idle, AssistantReducer.reduce(Idle, AssistantEvent.TranscriptUpdated("abrir")))
    }

    @Test
    fun `sensitive intent moves to confirmation`() {
        val state = AssistantReducer.reduce(
            Listening("enviar mensagem"),
            AssistantEvent.ConfirmationRequired("Enviar mensagem para a Maria", simulated = true),
        )
        assertEquals(Confirming("Enviar mensagem para a Maria", simulated = true), state)
    }

    @Test
    fun `verified completion is success, unverified is not promised as success`() {
        assertEquals(
            Result(Outcome.Success("ok")),
            AssistantReducer.reduce(Working, AssistantEvent.Completed("ok", verified = true)),
        )
        assertEquals(
            Result(Outcome.Unverified("ok")),
            AssistantReducer.reduce(Working, AssistantEvent.Completed("ok", verified = false)),
        )
    }

    @Test
    fun `failure carries only a reason`() {
        assertEquals(
            Result(Outcome.Failure(FailureReason.MICROPHONE_UNAVAILABLE)),
            AssistantReducer.reduce(Listening(), AssistantEvent.Failed(FailureReason.MICROPHONE_UNAVAILABLE)),
        )
    }

    @Test
    fun `cancel from confirmation shows cancelled result`() {
        assertEquals(
            Result(Outcome.Cancelled),
            AssistantReducer.reduce(Confirming("x", true), AssistantEvent.Cancelled),
        )
    }

    @Test
    fun `late events are ignored when idle or already showing a result`() {
        assertEquals(Idle, AssistantReducer.reduce(Idle, AssistantEvent.Failed(FailureReason.NOT_UNDERSTOOD)))
        assertEquals(Idle, AssistantReducer.reduce(Idle, AssistantEvent.Completed("x", verified = true)))
        assertEquals(Idle, AssistantReducer.reduce(Idle, AssistantEvent.Cancelled))

        val cancelled = Result(Outcome.Cancelled)
        assertEquals(cancelled, AssistantReducer.reduce(cancelled, AssistantEvent.Completed("x", verified = true)))
    }
}
