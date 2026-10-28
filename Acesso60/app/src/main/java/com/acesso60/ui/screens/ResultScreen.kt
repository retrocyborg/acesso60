package com.acesso60.ui.screens

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.acesso60.R
import com.acesso60.ui.components.BadgeKind
import com.acesso60.ui.components.BigButton
import com.acesso60.ui.components.Heading
import com.acesso60.ui.components.ScreenFrame
import com.acesso60.ui.components.StatusBadge
import com.acesso60.ui.state.FailureReason
import com.acesso60.ui.state.Outcome
import com.acesso60.ui.theme.Alert
import com.acesso60.ui.theme.AlertSoft
import com.acesso60.ui.theme.Caution
import com.acesso60.ui.theme.CautionSoft
import com.acesso60.ui.theme.Ink
import com.acesso60.ui.theme.Petrol
import com.acesso60.ui.theme.Slate
import com.acesso60.ui.theme.SlateSoft
import com.acesso60.ui.theme.Success
import com.acesso60.ui.theme.SuccessSoft

private class ResultLook(
    val background: Color,
    val badge: BadgeKind,
    val badgeColor: Color,
    val title: String,
    val lines: List<String>,
)

@StringRes
private fun FailureReason.messageRes(): Int = when (this) {
    FailureReason.NOT_UNDERSTOOD -> R.string.ui_fail_not_understood
    FailureReason.MICROPHONE_UNAVAILABLE -> R.string.ui_fail_microphone
    FailureReason.NO_SPEECH_DETECTED -> R.string.ui_fail_no_speech
    FailureReason.RECOGNIZER_UNAVAILABLE -> R.string.ui_fail_recognizer
    FailureReason.LANGUAGE_UNAVAILABLE -> R.string.ui_fail_language
    FailureReason.NETWORK_UNAVAILABLE -> R.string.ui_fail_network
    FailureReason.RECOGNIZER_BUSY -> R.string.ui_fail_busy
    FailureReason.PERMISSION_MISSING -> R.string.ui_fail_permission
    FailureReason.SERVICE_DISABLED -> R.string.ui_fail_service
    FailureReason.SERVICE_CONNECTING -> R.string.ui_fail_service_connecting
    FailureReason.SERVICE_CONNECTION_FAILED -> R.string.ui_fail_service_connection
    FailureReason.INVALID_COMMAND -> R.string.ui_fail_invalid
    FailureReason.EXECUTION_ERROR -> R.string.ui_fail_execution
}

@Composable
private fun Outcome.look(): ResultLook = when (this) {
    is Outcome.Success -> ResultLook(
        SuccessSoft, BadgeKind.SUCCESS, Success,
        stringResource(R.string.ui_result_success_title),
        listOf(summary ?: stringResource(R.string.ui_result_success_body)),
    )

    // Sem verificação do resultado, a tela NÃO promete sucesso.
    is Outcome.Unverified -> ResultLook(
        CautionSoft, BadgeKind.WARNING, Caution,
        stringResource(R.string.ui_result_unverified_title),
        listOfNotNull(summary, stringResource(R.string.ui_result_unverified_body)),
    )

    is Outcome.Failure -> ResultLook(
        AlertSoft, BadgeKind.FAILURE, Alert,
        stringResource(R.string.ui_result_failure_title),
        listOf(stringResource(reason.messageRes())),
    )

    Outcome.Cancelled -> ResultLook(
        SlateSoft, BadgeKind.CANCELLED, Slate,
        stringResource(R.string.ui_result_cancelled_title),
        listOf(stringResource(R.string.ui_result_cancelled_body)),
    )
}

/** UI-04 — Resultado: sucesso, falha, cancelamento ou "não verificado", com opção de tentar de novo. */
@Composable
fun ResultScreen(
    outcome: Outcome,
    onRetry: () -> Unit,
    onDone: () -> Unit,
    onOpenSettings: () -> Unit,
    onRequestMicrophone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val look = outcome.look()

    ScreenFrame(
        background = look.background,
        contentColor = Ink,
        modifier = modifier,
        actions = { ResultActions(outcome, onRetry, onDone, onOpenSettings, onRequestMicrophone) },
    ) {
        StatusBadge(
            kind = look.badge,
            color = look.badgeColor,
            diameter = 96.dp,
            modifier = Modifier.align(Alignment.Start),
        )
        Heading(text = look.title, announce = true)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            look.lines.forEach { line ->
                Text(text = line, style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
                ))
            }
        }
    }
}

@Composable
private fun ColumnScope.ResultActions(
    outcome: Outcome,
    onRetry: () -> Unit,
    onDone: () -> Unit,
    onOpenSettings: () -> Unit,
    onRequestMicrophone: () -> Unit,
) {
    val backHome = stringResource(R.string.ui_back_home)
    when {
        outcome is Outcome.Failure && outcome.reason == FailureReason.PERMISSION_MISSING -> {
            BigButton(stringResource(R.string.ui_allow_microphone), onRequestMicrophone, color = Petrol)
            BigButton(backHome, onDone, filled = false, color = Ink)
        }

        outcome is Outcome.Failure && outcome.reason == FailureReason.SERVICE_DISABLED -> {
            BigButton(stringResource(R.string.ui_open_settings), onOpenSettings, color = Petrol)
            BigButton(backHome, onDone, filled = false, color = Ink)
        }

        else -> {
            val retry = if (outcome is Outcome.Success) R.string.ui_another_command else R.string.ui_retry
            BigButton(stringResource(retry), onRetry, color = Petrol)
            BigButton(backHome, onDone, filled = false, color = Ink)
        }
    }
}
