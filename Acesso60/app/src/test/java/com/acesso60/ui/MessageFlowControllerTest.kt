package com.acesso60.ui

import br.com.acesso60.core.executor.CommandExecutor
import br.com.acesso60.core.interpreter.CommandInterpreter
import br.com.acesso60.core.messaging.VoiceMessageCoordinator
import br.com.acesso60.core.model.Command
import br.com.acesso60.core.model.ExecutionResult
import br.com.acesso60.core.policy.RiskPolicy
import br.com.acesso60.core.router.CommandRouter
import br.com.acesso60.core.validator.CommandValidator
import com.acesso60.ui.state.AssistantEvent
import com.acesso60.voice.SpeechFailure
import com.acesso60.voice.SpeechToTextEngine
import com.acesso60.voice.TextSpeaker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MessageFlowControllerTest {

    @Test
    fun `dialogo completo prepara SendMessage e so executa depois da confirmacao`() {
        val h = Harness()

        h.controller.startListening()
        h.speech.emitFinal("mandar mensagem para João")
        h.speech.emitFinal("WhatsApp")
        h.speech.emitFinal("Chego em 10 minutos")

        // Antes de confirmar: nada executado, confirmação pedida com o conteúdo.
        assertTrue(h.executor.commands.isEmpty())
        val confirmation = h.events.filterIsInstance<AssistantEvent.ConfirmationRequired>().single()
        assertTrue(confirmation.actionDescription.contains("Chego em 10 minutos"))

        h.controller.confirmPending()
        assertEquals(listOf<Command>(Command.SendMessage("João", "whatsapp", "Chego em 10 minutos")), h.executor.commands)
        val completed = h.events.filterIsInstance<AssistantEvent.Completed>().single()
        assertFalse("envio não é verificado", completed.verified)
    }

    @Test
    fun `falar apenas enviar mensagem pergunta o destinatario e completa o dialogo`() {
        val h = Harness()

        h.controller.startListening()
        h.speech.emitFinal("enviar mensagem")
        // Deve perguntar o destinatário
        val firstPrompt = h.events.filterIsInstance<AssistantEvent.Prompt>().first()
        assertTrue(firstPrompt.text.contains("Para quem você quer enviar a mensagem?"))

        h.speech.emitFinal("Maria")
        h.speech.emitFinal("WhatsApp")
        h.speech.emitFinal("Chego já")

        // Antes de confirmar: nada executado
        assertTrue(h.executor.commands.isEmpty())
        val confirmation = h.events.filterIsInstance<AssistantEvent.ConfirmationRequired>().single()
        assertTrue(confirmation.actionDescription.contains("Chego já"))

        h.controller.confirmPending()
        assertEquals(listOf<Command>(Command.SendMessage("Maria", "whatsapp", "Chego já")), h.executor.commands)
    }

    @Test
    fun `cada pergunta e falada e a escuta so recomeca depois que a fala termina`() {
        val h = Harness(autoFinishSpeech = false)

        h.controller.startListening()
        assertEquals(1, h.speech.startCount)
        h.speech.emitFinal("mandar mensagem para Maria")

        assertEquals(1, h.speaker.spoken.size)
        assertEquals("microfone não reabre durante a fala", 1, h.speech.startCount)
        assertTrue(h.events.any { it is AssistantEvent.Prompt })

        h.speaker.finishLast()
        assertEquals(2, h.speech.startCount)
    }

    @Test
    fun `app invalido repete a pergunta e nao avanca`() {
        val h = Harness()
        h.controller.startListening()
        h.speech.emitFinal("mandar mensagem para João")
        h.speech.emitFinal("YouTube")

        assertEquals(2, h.events.filterIsInstance<AssistantEvent.Prompt>().size)
        assertTrue(h.events.none { it is AssistantEvent.ConfirmationRequired })
        assertTrue(h.executor.commands.isEmpty())
    }

    @Test
    fun `cancelar no meio do dialogo encerra sem confirmar nem executar`() {
        val h = Harness()
        h.controller.startListening()
        h.speech.emitFinal("mandar mensagem para João")
        h.speech.emitFinal("cancelar")

        assertTrue(h.events.any { it == AssistantEvent.Cancelled })
        assertTrue(h.executor.commands.isEmpty())
    }

    @Test
    fun `cancelPending apos confirmacao nao envia`() {
        val h = Harness()
        h.controller.startListening()
        h.speech.emitFinal("mandar mensagem para João")
        h.speech.emitFinal("telegram")
        h.speech.emitFinal("Oi")
        h.controller.cancelPending()
        h.controller.confirmPending()

        assertTrue(h.executor.commands.isEmpty())
    }

    @Test
    fun `sem coordenador o comportamento anterior e mantido`() {
        val h = Harness(withCoordinator = false)
        h.controller.startListening()
        h.speech.emitFinal("mandar mensagem para João")

        assertTrue(h.events.none { it is AssistantEvent.Prompt })
        assertTrue(h.events.any { it is AssistantEvent.Failed })
    }

    private class Harness(
        autoFinishSpeech: Boolean = true,
        withCoordinator: Boolean = true,
    ) {
        val scope = CoroutineScope(Dispatchers.Unconfined)
        val speech = FakeSpeech()
        val speaker = FakeSpeaker(autoFinishSpeech)
        val executor = RecordingExecutor()
        val events = mutableListOf<AssistantEvent>()
        val controller = RealAssistantController(
            scope = scope,
            speechToText = speech,
            interpreter = CommandInterpreter(),
            validator = CommandValidator(),
            riskPolicy = RiskPolicy(),
            router = CommandRouter(CommandValidator(), RiskPolicy(), executor),
            messageCoordinator = if (withCoordinator) VoiceMessageCoordinator() else null,
            speaker = speaker,
        )

        init {
            scope.launch { controller.events.collect { events += it } }
        }
    }

    private class FakeSpeech : SpeechToTextEngine {
        private var listener: SpeechToTextEngine.Listener? = null
        var startCount = 0
        override fun startListening(listener: SpeechToTextEngine.Listener) {
            this.listener = listener
            startCount++
        }
        override fun stopListening() = Unit
        override fun cancel() = Unit
        fun emitFinal(text: String) = listener?.onFinalResult(text)
        @Suppress("unused") fun emitError() = listener?.onError(SpeechFailure.UNKNOWN)
    }

    private class FakeSpeaker(private val autoFinish: Boolean) : TextSpeaker {
        val spoken = mutableListOf<String>()
        private val callbacks = mutableListOf<() -> Unit>()
        override fun speak(text: String, onDone: () -> Unit) {
            spoken += text
            if (autoFinish) onDone() else callbacks += onDone
        }
        fun finishLast() = callbacks.removeAt(callbacks.size - 1).invoke()
        override fun stop() = Unit
        override fun shutdown() = Unit
    }

    private class RecordingExecutor : CommandExecutor {
        val commands = mutableListOf<Command>()
        override fun execute(command: Command): ExecutionResult {
            commands += command
            return ExecutionResult.Success("ok")
        }
    }
}
