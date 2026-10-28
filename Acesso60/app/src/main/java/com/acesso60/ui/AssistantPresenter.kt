package com.acesso60.ui

import com.acesso60.ui.state.AssistantEvent
import com.acesso60.ui.state.AssistantReducer
import com.acesso60.ui.state.AssistantScreenState
import com.acesso60.ui.state.AssistantUiState
import com.acesso60.ui.state.FailureReason
import com.acesso60.ui.state.Outcome
import com.acesso60.ui.state.PermissionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Lógica de estado da UI, sem dependência de Android — por isso é testável na JVM.
 * Todas as ações do usuário passam por aqui e só são aceitas no estado correto.
 */
class AssistantPresenter(
    private val controller: AssistantController,
    private val readPermissions: () -> PermissionStatus,
) {
    private val _uiState = MutableStateFlow(AssistantUiState(permissions = readPermissions()))
    val uiState: StateFlow<AssistantUiState> = _uiState.asStateFlow()

    private val screen: AssistantScreenState get() = _uiState.value.screen

    private fun setScreen(newScreen: AssistantScreenState) =
        _uiState.update { it.copy(screen = newScreen) }

    /** Evento vindo do Núcleo/Android. */
    fun onEvent(event: AssistantEvent) =
        _uiState.update { it.copy(screen = AssistantReducer.reduce(it.screen, event)) }

    fun refreshPermissions() =
        _uiState.update { it.copy(permissions = readPermissions()) }

    fun onListenRequested() {
        refreshPermissions()
        val current = screen
        if (current !is AssistantScreenState.Idle && current !is AssistantScreenState.Result) return

        if (!_uiState.value.permissions.microphoneGranted) {
            setScreen(AssistantScreenState.Result(Outcome.Failure(FailureReason.PERMISSION_MISSING)))
            return
        }
        setScreen(AssistantScreenState.Listening())
        controller.startListening()
    }

    fun onCancelListening() {
        if (screen !is AssistantScreenState.Listening) return
        setScreen(AssistantScreenState.Idle)
        controller.cancelListening()
    }

    fun onConfirm() {
        if (screen !is AssistantScreenState.Confirming) return
        // Sai de Confirming antes de chamar o controller: um segundo toque não confirma de novo.
        setScreen(AssistantScreenState.Working)
        controller.confirmPending()
    }

    fun onCancelConfirmation() {
        if (screen !is AssistantScreenState.Confirming) return
        setScreen(AssistantScreenState.Result(Outcome.Cancelled))
        controller.cancelPending()
    }

    fun onDismissResult() {
        if (screen is AssistantScreenState.Result) setScreen(AssistantScreenState.Idle)
    }

    fun onOpenSettings() {
        val current = screen
        if (current !is AssistantScreenState.Idle && current !is AssistantScreenState.Result) return
        _uiState.update { it.copy(screen = AssistantScreenState.Idle, settingsOpen = true) }
    }

    fun onCloseSettings() {
        _uiState.update { it.copy(settingsOpen = false, permissions = readPermissions()) }
    }
}
