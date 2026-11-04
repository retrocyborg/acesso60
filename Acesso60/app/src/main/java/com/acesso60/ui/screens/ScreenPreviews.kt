package com.acesso60.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.acesso60.ui.Acesso60Actions
import com.acesso60.ui.Acesso60Content
import com.acesso60.ui.preferences.TextScale
import com.acesso60.ui.state.AssistantScreenState
import com.acesso60.ui.state.AssistantUiState
import com.acesso60.ui.state.FailureReason
import com.acesso60.ui.state.Outcome
import com.acesso60.ui.state.PermissionStatus
import com.acesso60.ui.theme.Acesso60Theme

private val allOk = PermissionStatus(microphoneGranted = true, accessibilityServiceEnabled = true)

@Composable
private fun Preview(state: AssistantUiState, scale: TextScale = TextScale.NORMAL) {
    Acesso60Theme(textScale = scale) {
        Acesso60Content(state, scale, Acesso60Actions())
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 740)
@Composable
private fun CentralPreview() = Preview(AssistantUiState(permissions = allOk))

@Preview(showBackground = true, widthDp = 360, heightDp = 740)
@Composable
private fun CentralSemPermissaoPreview() = Preview(
    AssistantUiState(permissions = PermissionStatus(microphoneGranted = false, accessibilityServiceEnabled = false)),
)

@Preview(showBackground = true, widthDp = 360, heightDp = 740)
@Composable
private fun EscutaPreview() = Preview(
    AssistantUiState(AssistantScreenState.Listening("abrir o whats"), allOk),
)

@Preview(showBackground = true, widthDp = 360, heightDp = 740)
@Composable
private fun ConfirmacaoPreview() = Preview(
    AssistantUiState(AssistantScreenState.Confirming("Enviar uma mensagem para a Maria", simulated = true), allOk),
)

@Preview(showBackground = true, widthDp = 360, heightDp = 740)
@Composable
private fun SucessoPreview() = Preview(
    AssistantUiState(AssistantScreenState.Result(Outcome.Success("Voltei para a tela anterior.")), allOk),
)

@Preview(showBackground = true, widthDp = 360, heightDp = 740)
@Composable
private fun FalhaPreview() = Preview(
    AssistantUiState(AssistantScreenState.Result(Outcome.Failure(FailureReason.NOT_UNDERSTOOD)), allOk),
)

@Preview(showBackground = true, widthDp = 360, heightDp = 740)
@Composable
private fun ConfiguracoesGrandePreview() = Preview(
    AssistantUiState(
        permissions = PermissionStatus(microphoneGranted = true, accessibilityServiceEnabled = false),
        settingsOpen = true,
    ),
    scale = TextScale.EXTRA_LARGE,
)
