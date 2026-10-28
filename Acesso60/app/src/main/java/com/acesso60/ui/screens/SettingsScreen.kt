package com.acesso60.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.acesso60.R
import com.acesso60.ui.components.BigButton
import com.acesso60.ui.components.Heading
import com.acesso60.ui.components.ScreenFrame
import com.acesso60.ui.components.StatusRow
import com.acesso60.ui.preferences.TextScale
import com.acesso60.ui.state.PermissionStatus
import com.acesso60.ui.theme.Ink
import com.acesso60.ui.theme.Paper
import com.acesso60.ui.theme.Petrol

/** UI-05 — Configurações: permissões, como ligar a acessibilidade e tamanho do texto. */
@Composable
fun SettingsScreen(
    permissions: PermissionStatus,
    textScale: TextScale,
    onTextScaleSelected: (TextScale) -> Unit,
    onRequestMicrophone: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onOpenAppSettings: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenFrame(
        background = Paper,
        contentColor = Ink,
        modifier = modifier,
        actions = { BigButton(stringResource(R.string.ui_back), onBack, color = Petrol) },
    ) {
        Heading(stringResource(R.string.ui_settings_title))

        Heading(
            text = stringResource(R.string.ui_settings_permissions),
            style = MaterialTheme.typography.headlineMedium,
        )
        StatusRow(
            label = stringResource(R.string.ui_status_microphone),
            value = stringResource(
                if (permissions.microphoneGranted) R.string.ui_status_granted else R.string.ui_status_not_granted,
            ),
            ok = permissions.microphoneGranted,
        )
        if (!permissions.microphoneGranted) {
            BigButton(stringResource(R.string.ui_allow_microphone), onRequestMicrophone)
            BigButton(stringResource(R.string.ui_open_app_settings), onOpenAppSettings, filled = false)
        }

        StatusRow(
            label = stringResource(R.string.ui_status_service),
            value = stringResource(
                if (permissions.accessibilityServiceEnabled) R.string.ui_status_service_on else R.string.ui_status_service_off,
            ),
            ok = permissions.accessibilityServiceEnabled,
        )
        if (!permissions.accessibilityServiceEnabled) {
            Heading(
                text = stringResource(R.string.ui_settings_a11y_title),
                style = MaterialTheme.typography.titleLarge,
            )
            // Sequência real de passos: a numeração aqui carrega informação.
            Text(stringResource(R.string.ui_settings_a11y_step1), style = MaterialTheme.typography.bodyLarge)
            Text(stringResource(R.string.ui_settings_a11y_step2), style = MaterialTheme.typography.bodyLarge)
            Text(stringResource(R.string.ui_settings_a11y_step3), style = MaterialTheme.typography.bodyLarge)
            Text(stringResource(R.string.ui_settings_a11y_note), style = MaterialTheme.typography.bodyMedium)
            BigButton(stringResource(R.string.ui_open_a11y_settings), onOpenAccessibilitySettings)
        }

        Heading(
            text = stringResource(R.string.ui_settings_text_size),
            style = MaterialTheme.typography.headlineMedium,
        )
        Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            TextScale.entries.forEach { option ->
                val selected = option == textScale
                Row(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 64.dp)
                        .selectable(
                            selected = selected,
                            onClick = { onTextScaleSelected(option) },
                            role = Role.RadioButton,
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = selected, onClick = null)
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = stringResource(option.labelRes()),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}

private fun TextScale.labelRes(): Int = when (this) {
    TextScale.NORMAL -> R.string.ui_text_size_normal
    TextScale.LARGE -> R.string.ui_text_size_large
    TextScale.EXTRA_LARGE -> R.string.ui_text_size_extra_large
}
