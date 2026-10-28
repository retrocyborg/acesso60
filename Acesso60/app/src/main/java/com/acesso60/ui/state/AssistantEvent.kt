package com.acesso60.ui.state

/**
 * Motivos de falha que a interface sabe comunicar.
 *
 * A UI só recebe este enum — nunca uma exceção ou mensagem técnica — para que
 * nenhum stack trace possa chegar à tela (BETA-12).
 */
enum class FailureReason {
    NOT_UNDERSTOOD,
    MICROPHONE_UNAVAILABLE,
    NO_SPEECH_DETECTED,
    RECOGNIZER_UNAVAILABLE,
    LANGUAGE_UNAVAILABLE,
    NETWORK_UNAVAILABLE,
    RECOGNIZER_BUSY,
    PERMISSION_MISSING,
    SERVICE_DISABLED,
    SERVICE_CONNECTING,
    SERVICE_CONNECTION_FAILED,
    INVALID_COMMAND,
    EXECUTION_ERROR,
}

/**
 * Eventos que Núcleo/Android enviam para a interface (parte da proposta de CONTRATO-01).
 */
sealed interface AssistantEvent {
    data object ListeningStarted : AssistantEvent

    data class TranscriptUpdated(val text: String) : AssistantEvent

    /** Comando reconhecido e em processamento/execução. */
    data object Processing : AssistantEvent

    /** Ação sensível: a UI deve pedir confirmação antes de qualquer execução. */
    data class ConfirmationRequired(
        val actionDescription: String,
        val simulated: Boolean = true,
    ) : AssistantEvent

    /**
     * @param summary texto curto e amigável, já pronto para o usuário (nunca mensagem de exceção).
     * @param verified `true` somente se o resultado foi de fato verificado. Sem verificação,
     *   a UI não promete sucesso.
     */
    data class Completed(val summary: String? = null, val verified: Boolean) : AssistantEvent

    data class Failed(val reason: FailureReason) : AssistantEvent

    data object Cancelled : AssistantEvent
}
