package com.acesso60.ui.simulation

import com.acesso60.ui.AssistantController
import com.acesso60.ui.state.AssistantEvent
import com.acesso60.ui.state.FailureReason
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * Executor SIMULADO para desenvolver e demonstrar a interface antes da integração.
 * Não interpreta voz real e não executa nada no aparelho.
 *
 * Cada toque em "Falar" percorre um cenário diferente, em ciclo:
 * sucesso, ação sensível (com confirmação), fala não entendida,
 * resultado não verificado e erro de execução.
 */
class SimulatedAssistantController(
    private val scope: CoroutineScope,
    private val stepDelayMs: Long = 1_200L,
) : AssistantController {

    private enum class Scenario {
        SUCCESS, SENSITIVE, NOT_UNDERSTOOD, UNVERIFIED, EXECUTION_ERROR
    }

    private val _events = MutableSharedFlow<AssistantEvent>(extraBufferCapacity = 32)
    override val events: Flow<AssistantEvent> = _events.asSharedFlow()

    private var job: Job? = null
    private var nextScenario = 0
    private var confirmationPending = false
    private val executed = mutableListOf<String>()

    /** Ações "executadas" na simulação. Cancelar uma ação sensível deve deixar isto vazio. */
    val executedActions: List<String> get() = executed.toList()

    override fun startListening() {
        job?.cancel()
        confirmationPending = false
        val scenario = Scenario.entries[nextScenario++ % Scenario.entries.size]

        job = scope.launch {
            _events.emit(AssistantEvent.ListeningStarted)
            delay(stepDelayMs)
            when (scenario) {
                Scenario.SUCCESS -> {
                    _events.emit(AssistantEvent.TranscriptUpdated("voltar"))
                    delay(stepDelayMs)
                    _events.emit(AssistantEvent.Processing)
                    delay(stepDelayMs)
                    executed += "voltar"
                    _events.emit(AssistantEvent.Completed("Voltei para a tela anterior.", verified = true))
                }

                Scenario.SENSITIVE -> {
                    _events.emit(AssistantEvent.TranscriptUpdated("enviar mensagem para a Maria"))
                    delay(stepDelayMs)
                    confirmationPending = true
                    _events.emit(
                        AssistantEvent.ConfirmationRequired(
                            actionDescription = "Enviar uma mensagem para a Maria",
                            simulated = true,
                        ),
                    )
                }

                Scenario.NOT_UNDERSTOOD -> {
                    _events.emit(AssistantEvent.TranscriptUpdated("mmm… bla"))
                    delay(stepDelayMs)
                    _events.emit(AssistantEvent.Failed(FailureReason.NOT_UNDERSTOOD))
                }

                Scenario.UNVERIFIED -> {
                    _events.emit(AssistantEvent.TranscriptUpdated("aumentar volume"))
                    delay(stepDelayMs)
                    _events.emit(AssistantEvent.Processing)
                    delay(stepDelayMs)
                    executed += "aumentar volume"
                    _events.emit(AssistantEvent.Completed("Pedi para aumentar o volume.", verified = false))
                }

                Scenario.EXECUTION_ERROR -> {
                    _events.emit(AssistantEvent.TranscriptUpdated("abrir calculadora"))
                    delay(stepDelayMs)
                    _events.emit(AssistantEvent.Processing)
                    delay(stepDelayMs)
                    _events.emit(AssistantEvent.Failed(FailureReason.EXECUTION_ERROR))
                }
            }
        }
    }

    override fun cancelListening() {
        job?.cancel()
        confirmationPending = false
    }

    override fun confirmPending() {
        if (!confirmationPending) return
        confirmationPending = false
        job = scope.launch {
            _events.emit(AssistantEvent.Processing)
            delay(stepDelayMs)
            executed += "enviar mensagem (simulado)"
            _events.emit(
                AssistantEvent.Completed("Mensagem simulada. Nada foi enviado de verdade.", verified = true),
            )
        }
    }

    override fun cancelPending() {
        if (!confirmationPending) return
        confirmationPending = false
        job?.cancel()
        _events.tryEmit(AssistantEvent.Cancelled)
    }
}
