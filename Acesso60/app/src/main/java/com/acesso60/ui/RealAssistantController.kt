package com.acesso60.ui

import br.com.acesso60.core.interpreter.CommandInterpreter
import br.com.acesso60.core.model.Command
import br.com.acesso60.core.model.ExecutionResult
import br.com.acesso60.core.model.InterpretationResult
import br.com.acesso60.core.model.RiskAssessment
import br.com.acesso60.core.model.ValidationResult
import br.com.acesso60.core.policy.RiskPolicy
import br.com.acesso60.core.router.CommandRouter
import br.com.acesso60.core.scripting.ScriptDefinition
import br.com.acesso60.core.scripting.ScriptEngine
import br.com.acesso60.core.scripting.ScriptRegistry
import br.com.acesso60.core.scripting.ScriptRiskEvaluator
import br.com.acesso60.core.validator.CommandValidator
import com.acesso60.ui.state.AssistantEvent
import com.acesso60.ui.state.FailureReason
import com.acesso60.voice.SpeechFailure
import com.acesso60.voice.SpeechToTextEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/** Controlador real da primeira fatia: voz, núcleo compartilhado, rotinas e executor Android. */
class RealAssistantController(
    private val scope: CoroutineScope,
    private val speechToText: SpeechToTextEngine,
    private val interpreter: CommandInterpreter,
    private val validator: CommandValidator,
    private val riskPolicy: RiskPolicy,
    private val router: CommandRouter,
    private val scriptRegistry: ScriptRegistry? = null,
    private val scriptEngine: ScriptEngine? = null,
    private val scriptRiskEvaluator: ScriptRiskEvaluator = ScriptRiskEvaluator(riskPolicy),
) : AssistantController {
    private val _events = MutableSharedFlow<AssistantEvent>(extraBufferCapacity = 32)
    override val events: Flow<AssistantEvent> = _events.asSharedFlow()

    private var sessionId = 0L
    private var pending: PendingAction? = null

    override fun startListening() {
        val currentSession = ++sessionId
        pending = null
        speechToText.cancel()
        emit(AssistantEvent.ListeningStarted)
        speechToText.startListening(object : SpeechToTextEngine.Listener {
            override fun onPartialResult(text: String) {
                if (isCurrent(currentSession)) emit(AssistantEvent.TranscriptUpdated(text))
            }

            override fun onFinalResult(text: String) {
                if (isCurrent(currentSession)) handleTranscript(currentSession, text)
            }

            override fun onError(reason: SpeechFailure) {
                if (isCurrent(currentSession)) emit(AssistantEvent.Failed(reason.toFailureReason()))
            }
        })
    }

    override fun cancelListening() {
        ++sessionId
        pending = null
        speechToText.cancel()
    }

    override fun confirmPending() {
        val candidate = pending ?: return
        if (!isCurrent(candidate.sessionId)) return
        pending = null
        emit(AssistantEvent.Processing)
        when (candidate) {
            is PendingAction.SingleCommand -> emitExecution(router.route(candidate.command, isConfirmed = true))
            is PendingAction.Script -> {
                val engine = scriptEngine
                if (engine != null) {
                    emitExecution(engine.run(candidate.script, isConfirmed = true))
                } else {
                    emit(AssistantEvent.Failed(FailureReason.EXECUTION_ERROR))
                }
            }
        }
    }

    override fun cancelPending() {
        pending = null
        speechToText.cancel()
        emit(AssistantEvent.Cancelled)
    }

    override fun dispose() {
        ++sessionId
        pending = null
        speechToText.cancel()
        speechToText.destroy()
    }

    private fun handleTranscript(currentSession: Long, text: String) {
        emit(AssistantEvent.TranscriptUpdated(text))
        emit(AssistantEvent.Processing)

        // 1. Verifica se corresponde a uma rotina/script declarativo registrado
        val matchedScript = scriptRegistry?.findByAlias(text)
        if (matchedScript != null && scriptEngine != null) {
            handleScript(currentSession, matchedScript, scriptEngine)
            return
        }

        // 2. Se não for script, prossegue para interpretação de comando padrão
        when (val interpretation = interpreter.interpret(text)) {
            is InterpretationResult.Unknown -> emit(AssistantEvent.Failed(FailureReason.NOT_UNDERSTOOD))
            is InterpretationResult.Success -> handleCommand(currentSession, interpretation.command)
        }
    }

    private fun handleScript(currentSession: Long, script: ScriptDefinition, engine: ScriptEngine) {
        when (val risk = scriptRiskEvaluator.evaluate(script)) {
            RiskAssessment.LowRisk -> emitExecution(engine.run(script))
            is RiskAssessment.RequiresConfirmation -> {
                pending = PendingAction.Script(currentSession, script)
                emit(AssistantEvent.ConfirmationRequired(risk.message, simulated = false))
            }
        }
    }

    private fun handleCommand(currentSession: Long, command: Command) {
        when (val validation = validator.validate(command)) {
            is ValidationResult.Invalid -> emit(AssistantEvent.Failed(FailureReason.INVALID_COMMAND))
            is ValidationResult.Valid -> when (val risk = riskPolicy.evaluate(validation.command)) {
                RiskAssessment.LowRisk -> emitExecution(router.route(validation.command))
                is RiskAssessment.RequiresConfirmation -> {
                    pending = PendingAction.SingleCommand(currentSession, validation.command)
                    emit(AssistantEvent.ConfirmationRequired(risk.message, simulated = false))
                }
            }
        }
    }

    private fun emitExecution(result: ExecutionResult) = when (result) {
        is ExecutionResult.Success -> emit(AssistantEvent.Completed(result.message, verified = true))
        ExecutionResult.Denied -> emit(AssistantEvent.Failed(FailureReason.INVALID_COMMAND))
        ExecutionResult.Unavailable -> emit(AssistantEvent.Failed(FailureReason.SERVICE_DISABLED))
        is ExecutionResult.Failure -> emit(AssistantEvent.Failed(FailureReason.EXECUTION_ERROR))
    }

    private fun isCurrent(candidate: Long): Boolean = candidate == sessionId

    private fun emit(event: AssistantEvent) {
        scope.launch { _events.emit(event) }
    }

    private sealed interface PendingAction {
        val sessionId: Long
        data class SingleCommand(override val sessionId: Long, val command: Command) : PendingAction
        data class Script(override val sessionId: Long, val script: ScriptDefinition) : PendingAction
    }

    private fun SpeechFailure.toFailureReason(): FailureReason = when (this) {
        SpeechFailure.NO_SPEECH_DETECTED -> FailureReason.NO_SPEECH_DETECTED
        SpeechFailure.RECOGNIZER_UNAVAILABLE -> FailureReason.RECOGNIZER_UNAVAILABLE
        SpeechFailure.LANGUAGE_UNAVAILABLE -> FailureReason.LANGUAGE_UNAVAILABLE
        SpeechFailure.NETWORK_UNAVAILABLE -> FailureReason.NETWORK_UNAVAILABLE
        SpeechFailure.RECOGNIZER_BUSY -> FailureReason.RECOGNIZER_BUSY
        SpeechFailure.AUDIO_UNAVAILABLE,
        SpeechFailure.PERMISSION_DENIED,
        SpeechFailure.UNKNOWN -> FailureReason.MICROPHONE_UNAVAILABLE
    }
}
