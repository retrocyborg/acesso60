package com.acesso60.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.acesso60.R
import com.acesso60.ui.preferences.TextScale
import com.acesso60.ui.state.AssistantScreenState
import com.acesso60.ui.state.AssistantUiState
import com.acesso60.ui.state.FailureReason
import com.acesso60.ui.state.Outcome
import com.acesso60.ui.state.PermissionStatus
import com.acesso60.ui.theme.Acesso60Theme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class Acesso60ContentTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val allOk = PermissionStatus(microphoneGranted = true, accessibilityServiceEnabled = true)

    private fun str(id: Int) = rule.activity.getString(id)

    private fun show(state: AssistantUiState, actions: Acesso60Actions = Acesso60Actions()) {
        rule.setContent {
            Acesso60Theme { Acesso60Content(state, TextScale.NORMAL, actions) }
        }
    }

    @Test
    fun cancellingConfirmationCallsCancelAndNeverConfirm() {
        var confirmed = 0
        var cancelled = 0
        show(
            AssistantUiState(AssistantScreenState.Confirming("Enviar uma mensagem para a Maria", true), allOk),
            Acesso60Actions(onConfirm = { confirmed++ }, onCancelConfirmation = { cancelled++ }),
        )

        rule.onNodeWithText("Enviar uma mensagem para a Maria").assertIsDisplayed()
        rule.onNodeWithText(str(R.string.ui_confirm)).assertIsDisplayed()
        rule.onNodeWithText(str(R.string.ui_cancel)).performClick()

        assertEquals(0, confirmed)
        assertEquals(1, cancelled)
    }

    @Test
    fun everyFailureReasonShowsFriendlyTextWithoutTechnicalDetails() {
        var current by mutableStateOf(FailureReason.NOT_UNDERSTOOD)
        rule.setContent {
            Acesso60Theme {
                Acesso60Content(
                    AssistantUiState(AssistantScreenState.Result(Outcome.Failure(current)), allOk),
                    TextScale.NORMAL,
                    Acesso60Actions(),
                )
            }
        }

        FailureReason.entries.forEach { reason ->
            rule.runOnIdle { current = reason }
            rule.waitForIdle()
            rule.onNodeWithText(str(R.string.ui_result_failure_title)).assertIsDisplayed()
            listOf("Exception", "stack", "at com.", "null").forEach { fragment ->
                assertEquals(
                    "texto técnico '$fragment' apareceu na UI ($reason)",
                    0,
                    rule.onAllNodes(hasText(fragment, substring = true, ignoreCase = true)).fetchSemanticsNodes().size,
                )
            }
        }
    }

    @Test
    fun unverifiedResultDoesNotClaimSuccess() {
        show(AssistantUiState(AssistantScreenState.Result(Outcome.Unverified("Pedi para aumentar o volume.")), allOk))

        rule.onNodeWithText(str(R.string.ui_result_unverified_title)).assertIsDisplayed()
        assertEquals(0, rule.onAllNodes(hasText(str(R.string.ui_result_success_title))).fetchSemanticsNodes().size)
    }

    @Test
    fun missingMicrophoneAsksForPermissionInsteadOfListening() {
        var listened = 0
        var requested = 0
        show(
            AssistantUiState(permissions = PermissionStatus(microphoneGranted = false, accessibilityServiceEnabled = true)),
            Acesso60Actions(onListen = { listened++ }, onRequestMicrophone = { requested++ }),
        )

        rule.onNodeWithText(str(R.string.ui_allow_microphone)).performClick()

        assertEquals(0, listened)
        assertEquals(1, requested)
    }

    @Test
    fun listeningShowsActiveMicrophoneTranscriptAndCancel() {
        var cancelled = 0
        show(
            AssistantUiState(AssistantScreenState.Listening("abrir whatsapp"), allOk),
            Acesso60Actions(onCancelListening = { cancelled++ }),
        )

        rule.onNodeWithText(str(R.string.ui_listening_mic_on)).assertIsDisplayed()
        rule.onNodeWithText("abrir whatsapp").assertIsDisplayed()
        rule.onNodeWithText(str(R.string.ui_cancel)).performClick()

        assertEquals(1, cancelled)
    }
}
