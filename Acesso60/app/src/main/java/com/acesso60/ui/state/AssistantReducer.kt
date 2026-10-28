package com.acesso60.ui.state

import com.acesso60.ui.state.AssistantScreenState.Confirming
import com.acesso60.ui.state.AssistantScreenState.Idle
import com.acesso60.ui.state.AssistantScreenState.Listening
import com.acesso60.ui.state.AssistantScreenState.Result
import com.acesso60.ui.state.AssistantScreenState.Working

/**
 * Função pura: (estado atual, evento) -> novo estado.
 *
 * Eventos que não fazem sentido no estado atual são ignorados. Isso evita, por exemplo,
 * que um resultado tardio reabra a tela depois de o usuário já ter cancelado.
 */
object AssistantReducer {

    private fun isActive(state: AssistantScreenState) =
        state is Listening || state is Working || state is Confirming

    fun reduce(current: AssistantScreenState, event: AssistantEvent): AssistantScreenState =
        when (event) {
            AssistantEvent.ListeningStarted ->
                if (current is Idle || current is Result) Listening() else current

            is AssistantEvent.TranscriptUpdated ->
                if (current is Listening) current.copy(transcript = event.text) else current

            AssistantEvent.Processing ->
                if (current is Listening || current is Working) Working else current

            is AssistantEvent.ConfirmationRequired ->
                if (current is Listening || current is Working) {
                    Confirming(event.actionDescription, event.simulated)
                } else {
                    current
                }

            is AssistantEvent.Completed ->
                if (isActive(current)) {
                    Result(
                        if (event.verified) Outcome.Success(event.summary)
                        else Outcome.Unverified(event.summary),
                    )
                } else {
                    current
                }

            is AssistantEvent.Failed ->
                if (isActive(current)) Result(Outcome.Failure(event.reason)) else current

            AssistantEvent.Cancelled ->
                if (isActive(current)) Result(Outcome.Cancelled) else current
        }
}
