package com.acesso60.ui

import com.acesso60.ui.state.AssistantEvent
import kotlinx.coroutines.flow.Flow

/**
 * Contrato entre a interface e o restante do app (proposta para CONTRATO-01).
 *
 * A UI **não** interpreta voz nem executa ações: ela chama estes métodos e reage a [events].
 * Quem integra (Núcleo + Android) implementa esta interface.
 *
 * Garantias exigidas do implementador:
 *  - [cancelPending] NÃO pode executar a ação pendente (E2E-04).
 *  - [confirmPending] só executa se houver uma confirmação pendente.
 *  - Falhas chegam como [AssistantEvent.Failed] com um motivo; nunca como exceção para a UI.
 */
interface AssistantController {
    val events: Flow<AssistantEvent>

    fun startListening()
    fun cancelListening()
    fun confirmPending()
    fun cancelPending()
}
