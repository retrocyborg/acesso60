package com.acesso60.ui

import br.com.acesso60.core.executor.CommandExecutor
import br.com.acesso60.core.interpreter.CommandInterpreter
import br.com.acesso60.core.model.Command
import br.com.acesso60.core.model.ExecutionResult
import br.com.acesso60.core.policy.RiskPolicy
import br.com.acesso60.core.router.CommandRouter
import br.com.acesso60.core.validator.CommandValidator
import br.com.acesso60.core.scripting.ScriptDefinition
import br.com.acesso60.core.scripting.ScriptEngine
import br.com.acesso60.core.scripting.ScriptRegistry
import br.com.acesso60.core.scripting.ScriptStep
import com.acesso60.ui.state.AssistantEvent
import com.acesso60.voice.SpeechToTextEngine
import com.acesso60.voice.SpeechFailure
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.junit.Assert.assertTrue
import org.junit.Test

class RealAssistantControllerTest {
    @Test
    fun `real controller routes back through the shared core`() {
        val scope = CoroutineScope(Dispatchers.Unconfined)
        val speech = FakeSpeechEngine()
        val executor = FakeExecutor()
        val controller = controller(scope, speech, executor)
        val events = mutableListOf<AssistantEvent>()
        scope.launch { controller.events.collect { events += it } }

        controller.startListening()
        speech.emitFinal("voltar")

        assertTrue(events.contains(AssistantEvent.ListeningStarted))
        assertTrue(events.contains(AssistantEvent.Processing))
        assertTrue(events.any { it is AssistantEvent.Completed && it.verified })
        assertTrue(executor.commands == listOf(Command.Back))
    }

    @Test
    fun `late result after cancellation is ignored`() {
        val scope = CoroutineScope(Dispatchers.Unconfined)
        val speech = FakeSpeechEngine()
        val executor = FakeExecutor()
        val controller = controller(scope, speech, executor)
        val events = mutableListOf<AssistantEvent>()
        scope.launch { controller.events.collect { events += it } }

        controller.startListening()
        controller.cancelListening()
        speech.emitFinal("voltar")

        assertTrue(executor.commands.isEmpty())
        assertTrue(events.none { it is AssistantEvent.Completed })
    }

    @Test
    fun `unknown transcript emits a recoverable interpretation failure`() {
        val scope = CoroutineScope(Dispatchers.Unconfined)
        val speech = FakeSpeechEngine()
        val controller = controller(scope, speech, FakeExecutor())
        val events = mutableListOf<AssistantEvent>()
        scope.launch { controller.events.collect { events += it } }

        controller.startListening()
        speech.emitFinal("frase que não existe")

        assertTrue(events.contains(AssistantEvent.Failed(com.acesso60.ui.state.FailureReason.NOT_UNDERSTOOD)))
    }

    @Test
    fun `sensitive transcript requires confirmation before execution`() {
        val scope = CoroutineScope(Dispatchers.Unconfined)
        val speech = FakeSpeechEngine()
        val executor = FakeExecutor()
        val controller = controller(scope, speech, executor)
        val events = mutableListOf<AssistantEvent>()
        scope.launch { controller.events.collect { events += it } }

        controller.startListening()
        speech.emitFinal("abrir whatsapp")

        assertTrue(events.any { it is AssistantEvent.ConfirmationRequired && !it.simulated })
        assertTrue(executor.commands.isEmpty())

        controller.confirmPending()

        assertTrue(executor.commands.isNotEmpty())
    }

    @Test
    fun `speech error emits microphone failure without execution`() {
        val scope = CoroutineScope(Dispatchers.Unconfined)
        val speech = FakeSpeechEngine()
        val executor = FakeExecutor()
        val controller = controller(scope, speech, executor)
        val events = mutableListOf<AssistantEvent>()
        scope.launch { controller.events.collect { events += it } }

        controller.startListening()
        speech.emitError()

        assertTrue(events.contains(AssistantEvent.Failed(com.acesso60.ui.state.FailureReason.MICROPHONE_UNAVAILABLE)))
        assertTrue(executor.commands.isEmpty())
    }

    @Test
    fun `registered script routine executes sequentially when triggered by alias`() {
        val scope = CoroutineScope(Dispatchers.Unconfined)
        val speech = FakeSpeechEngine()
        val executor = FakeExecutor()
        val registry = ScriptRegistry()
        val router = CommandRouter(CommandValidator(), RiskPolicy(), executor)
        val engine = ScriptEngine(router = router, sleeper = {})

        val script = ScriptDefinition(
            id = "rotina-1",
            name = "Resetar e Inicio",
            aliases = listOf("reiniciar tudo"),
            steps = listOf(
                ScriptStep.Execute(Command.Back),
                ScriptStep.Delay(100),
                ScriptStep.Execute(Command.Home)
            )
        )
        registry.register(script)

        val controller = RealAssistantController(
            scope = scope,
            speechToText = speech,
            interpreter = CommandInterpreter(),
            validator = CommandValidator(),
            riskPolicy = RiskPolicy(),
            router = router,
            scriptRegistry = registry,
            scriptEngine = engine
        )

        val events = mutableListOf<AssistantEvent>()
        scope.launch { controller.events.collect { events += it } }

        controller.startListening()
        speech.emitFinal("reiniciar tudo")

        assertTrue(events.any { it is AssistantEvent.Completed && it.verified })
        org.junit.Assert.assertEquals(listOf(Command.Back, Command.Home), executor.commands)
    }

    @Test
    fun `registered sensitive script requires confirmation before execution in real controller`() {
        val scope = CoroutineScope(Dispatchers.Unconfined)
        val speech = FakeSpeechEngine()
        val executor = FakeExecutor()
        val registry = ScriptRegistry()
        val router = CommandRouter(CommandValidator(), RiskPolicy(), executor)
        val engine = ScriptEngine(router = router, sleeper = {})

        val script = ScriptDefinition(
            id = "rotina-sensivel",
            name = "Ação Sensível",
            aliases = listOf("apagar tudo"),
            steps = listOf(
                ScriptStep.Execute(Command.SensitiveAction("apagar"))
            )
        )
        registry.register(script)

        val controller = RealAssistantController(
            scope = scope,
            speechToText = speech,
            interpreter = CommandInterpreter(),
            validator = CommandValidator(),
            riskPolicy = RiskPolicy(),
            router = router,
            scriptRegistry = registry,
            scriptEngine = engine
        )

        val events = mutableListOf<AssistantEvent>()
        scope.launch { controller.events.collect { events += it } }

        controller.startListening()
        speech.emitFinal("apagar tudo")

        assertTrue(events.any { it is AssistantEvent.ConfirmationRequired })
        assertTrue(executor.commands.isEmpty())

        controller.confirmPending()

        assertTrue(events.any { it is AssistantEvent.Completed })
        org.junit.Assert.assertEquals(1, executor.commands.size)
    }

    private fun controller(
        scope: CoroutineScope,
        speech: FakeSpeechEngine,
        executor: FakeExecutor,
    ) = RealAssistantController(
        scope = scope,
        speechToText = speech,
        interpreter = CommandInterpreter(),
        validator = CommandValidator(),
        riskPolicy = RiskPolicy(),
        router = CommandRouter(CommandValidator(), RiskPolicy(), executor),
    )

    private class FakeSpeechEngine : SpeechToTextEngine {
        private var listener: SpeechToTextEngine.Listener? = null

        override fun startListening(listener: SpeechToTextEngine.Listener) {
            this.listener = listener
        }

        override fun stopListening() = Unit
        override fun cancel() = Unit
        fun emitFinal(text: String) = listener?.onFinalResult(text)
        fun emitError() = listener?.onError(SpeechFailure.UNKNOWN)
    }

    private class FakeExecutor : CommandExecutor {
        val commands = mutableListOf<Command>()

        override fun execute(command: Command): ExecutionResult {
            commands += command
            return ExecutionResult.Success("ok")
        }
    }
}
