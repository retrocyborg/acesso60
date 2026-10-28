package com.acesso60.ui

import com.acesso60.ui.state.AssistantEvent
import com.acesso60.ui.state.AccessibilityStatus
import com.acesso60.ui.state.AssistantScreenState.Confirming
import com.acesso60.ui.state.AssistantScreenState.Idle
import com.acesso60.ui.state.AssistantScreenState.Listening
import com.acesso60.ui.state.AssistantScreenState.Result
import com.acesso60.ui.state.AssistantScreenState.Working
import com.acesso60.ui.state.FailureReason
import com.acesso60.ui.state.Outcome
import com.acesso60.ui.state.PermissionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Assert.assertEquals
import org.junit.Test

private class FakeController : AssistantController {
    override val events: Flow<AssistantEvent> = emptyFlow()
    var startCalls = 0
    var cancelListeningCalls = 0
    var confirmCalls = 0
    var cancelPendingCalls = 0
    override fun startListening() { startCalls++ }
    override fun cancelListening() { cancelListeningCalls++ }
    override fun confirmPending() { confirmCalls++ }
    override fun cancelPending() { cancelPendingCalls++ }
}

class AssistantPresenterTest {
    private val controller = FakeController()
    private var permissions = PermissionStatus(microphoneGranted = true, accessibilityServiceEnabled = true)
    private val presenter = AssistantPresenter(controller) { permissions }

    private val screen get() = presenter.uiState.value.screen

    @Test
    fun `listening without microphone permission fails and never starts the controller`() {
        permissions = permissions.copy(microphoneGranted = false)

        presenter.onListenRequested()

        assertEquals(Result(Outcome.Failure(FailureReason.PERMISSION_MISSING)), screen)
        assertEquals(0, controller.startCalls)
    }

    @Test
    fun `listening without accessibility service fails and never starts the controller`() {
        permissions = permissions.copy(accessibilityStatus = AccessibilityStatus.DISABLED)

        presenter.onListenRequested()

        assertEquals(
            Result(Outcome.Failure(FailureReason.SERVICE_DISABLED)),
            presenter.uiState.value.screen,
        )
        assertEquals(0, controller.startCalls)
    }

    @Test
    fun `listening while accessibility is connecting is blocked`() {
        permissions = PermissionStatus(true, AccessibilityStatus.CONNECTING)

        presenter.onListenRequested()

        assertEquals(Result(Outcome.Failure(FailureReason.SERVICE_CONNECTING)), screen)
        assertEquals(0, controller.startCalls)
    }

    @Test
    fun `listening after accessibility connection timeout is blocked`() {
        permissions = PermissionStatus(true, AccessibilityStatus.CONNECTION_FAILED)

        presenter.onListenRequested()

        assertEquals(Result(Outcome.Failure(FailureReason.SERVICE_CONNECTION_FAILED)), screen)
        assertEquals(0, controller.startCalls)
    }

    @Test
    fun `connected accessibility clears stale connection failure`() {
        permissions = PermissionStatus(true, AccessibilityStatus.CONNECTION_FAILED)
        presenter.onListenRequested()

        permissions = PermissionStatus(true, AccessibilityStatus.CONNECTED)
        presenter.refreshPermissions()

        assertEquals(Idle, screen)
    }

    @Test
    fun `refresh after enabling accessibility dismisses stale permission failure`() {
        permissions = permissions.copy(accessibilityStatus = AccessibilityStatus.DISABLED)
        presenter.onListenRequested()

        permissions = permissions.copy(accessibilityStatus = AccessibilityStatus.CONNECTED)
        presenter.refreshPermissions()

        assertEquals(Idle, screen)
        assertEquals(true, presenter.uiState.value.permissions.accessibilityServiceEnabled)
    }

    @Test
    fun `refresh keeps permission failure while accessibility remains disabled`() {
        permissions = permissions.copy(accessibilityStatus = AccessibilityStatus.DISABLED)
        presenter.onListenRequested()

        presenter.refreshPermissions()

        assertEquals(Result(Outcome.Failure(FailureReason.SERVICE_DISABLED)), screen)
    }

    @Test
    fun `refresh after granting microphone dismisses stale microphone failure`() {
        permissions = permissions.copy(microphoneGranted = false)
        presenter.onListenRequested()

        permissions = permissions.copy(microphoneGranted = true)
        presenter.refreshPermissions()

        assertEquals(Idle, screen)
        assertEquals(true, presenter.uiState.value.permissions.microphoneGranted)
    }

    @Test
    fun `listening with permission starts the controller once`() {
        presenter.onListenRequested()

        assertEquals(Listening(), screen)
        assertEquals(1, controller.startCalls)
    }

    // E2E-04 (metade da interface): cancelar uma ação sensível não executa nada.
    @Test
    fun `cancelling a sensitive confirmation never confirms`() {
        presenter.onListenRequested()
        presenter.onEvent(AssistantEvent.ConfirmationRequired("Enviar mensagem", simulated = true))
        assertEquals(Confirming("Enviar mensagem", simulated = true), screen)

        presenter.onCancelConfirmation()

        assertEquals(Result(Outcome.Cancelled), screen)
        assertEquals(0, controller.confirmCalls)
        assertEquals(1, controller.cancelPendingCalls)
    }

    @Test
    fun `confirm is ignored outside the confirmation state`() {
        presenter.onConfirm()
        assertEquals(Idle, screen)
        assertEquals(0, controller.confirmCalls)
    }

    @Test
    fun `double tap on confirm confirms only once`() {
        presenter.onListenRequested()
        presenter.onEvent(AssistantEvent.ConfirmationRequired("Enviar mensagem", simulated = true))

        presenter.onConfirm()
        presenter.onConfirm()

        assertEquals(Working, screen)
        assertEquals(1, controller.confirmCalls)
    }

    @Test
    fun `late failure after the user cancelled listening is ignored`() {
        presenter.onListenRequested()
        presenter.onCancelListening()
        presenter.onEvent(AssistantEvent.Failed(FailureReason.NOT_UNDERSTOOD))

        assertEquals(Idle, screen)
        assertEquals(1, controller.cancelListeningCalls)
    }

    @Test
    fun `retry from a failure starts listening again`() {
        presenter.onListenRequested()
        presenter.onEvent(AssistantEvent.Failed(FailureReason.NOT_UNDERSTOOD))
        presenter.onListenRequested()

        assertEquals(Listening(), screen)
        assertEquals(2, controller.startCalls)
    }

    @Test
    fun `settings open from idle and close restoring idle`() {
        presenter.onOpenSettings()
        assertEquals(true, presenter.uiState.value.settingsOpen)

        presenter.onCloseSettings()
        assertEquals(false, presenter.uiState.value.settingsOpen)
        assertEquals(Idle, screen)
    }

    @Test
    fun `settings cannot open while confirming`() {
        presenter.onListenRequested()
        presenter.onEvent(AssistantEvent.ConfirmationRequired("x", simulated = true))

        presenter.onOpenSettings()

        assertEquals(false, presenter.uiState.value.settingsOpen)
    }
}
