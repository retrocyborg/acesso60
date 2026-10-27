package com.acesso60.ui.state

sealed interface Outcome {
    data class Success(val summary: String?) : Outcome
    data class Unverified(val summary: String?) : Outcome
    data class Failure(val reason: FailureReason) : Outcome
    data object Cancelled : Outcome
}

sealed interface AssistantScreenState {
    data object Idle : AssistantScreenState
    data class Listening(val transcript: String = "") : AssistantScreenState
    data object Working : AssistantScreenState
    data class Confirming(
        val actionDescription: String,
        val simulated: Boolean,
    ) : AssistantScreenState
    data class Result(val outcome: Outcome) : AssistantScreenState
}

data class AssistantUiState(
    val screen: AssistantScreenState = AssistantScreenState.Idle,
    val permissions: PermissionStatus = PermissionStatus(
        microphoneGranted = false,
        accessibilityServiceEnabled = false,
    ),
    val settingsOpen: Boolean = false,
)
