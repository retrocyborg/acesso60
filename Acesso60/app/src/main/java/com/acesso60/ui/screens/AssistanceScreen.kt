package com.acesso60.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.acesso60.R
import com.acesso60.ui.components.BigButton
import com.acesso60.ui.components.Heading
import com.acesso60.ui.components.MicGlyph
import com.acesso60.ui.components.ScreenFrame
import com.acesso60.ui.components.StatusRow
import com.acesso60.ui.state.PermissionStatus
import com.acesso60.ui.theme.Ink
import com.acesso60.ui.theme.Paper
import com.acesso60.ui.theme.Petrol

/** UI-01 — Central de assistência: ação de escuta e situação de permissões/serviço. */
@Composable
fun AssistanceScreen(
    permissions: PermissionStatus,
    onListen: () -> Unit,
    onRequestMicrophone: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val micMissing = !permissions.microphoneGranted
    val serviceOff = !permissions.accessibilityServiceEnabled

    ScreenFrame(
        background = Paper,
        contentColor = Ink,
        modifier = modifier,
        actions = {
            BigButton(
                text = stringResource(R.string.ui_settings),
                onClick = onOpenSettings,
                filled = false,
                color = Petrol,
            )
        },
    ) {
        Heading(stringResource(R.string.ui_central_title))

        Text(
            text = stringResource(
                when {
                    micMissing -> R.string.ui_central_need_mic
                    serviceOff -> R.string.ui_central_service_off
                    else -> R.string.ui_central_ready
                },
            ),
            style = MaterialTheme.typography.bodyLarge,
        )

        SpeakButton(
            micMissing = micMissing,
            onClick = if (micMissing) onRequestMicrophone else onListen,
        )

        StatusRow(
            label = stringResource(R.string.ui_status_microphone),
            value = stringResource(
                if (micMissing) R.string.ui_status_not_granted else R.string.ui_status_granted,
            ),
            ok = !micMissing,
        )
        StatusRow(
            label = stringResource(R.string.ui_status_service),
            value = stringResource(
                if (serviceOff) R.string.ui_status_service_off else R.string.ui_status_service_on,
            ),
            ok = !serviceOff,
        )

        ExamplesBlock()
    }
}

@Composable
private fun ColumnScope.SpeakButton(micMissing: Boolean, onClick: () -> Unit) {
    val label = stringResource(if (micMissing) R.string.ui_allow_microphone else R.string.ui_speak)
    val description = stringResource(
        if (micMissing) R.string.ui_allow_microphone else R.string.ui_speak_description,
    )
    Button(
        onClick = onClick,
        modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .size(200.dp)
            .semantics { contentDescription = description },
        shape = CircleShape,
        contentPadding = PaddingValues(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Petrol, contentColor = Color.White),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            MicGlyph(color = Color.White, modifier = Modifier.size(76.dp))
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ExamplesBlock() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = stringResource(R.string.ui_examples_title),
            style = MaterialTheme.typography.titleLarge,
        )
        listOf(
            R.string.ui_example_back,
            R.string.ui_example_home,
            R.string.ui_example_volume,
            R.string.ui_example_open,
        ).forEach { res ->
            Text(text = stringResource(res), style = MaterialTheme.typography.bodyLarge)
        }
    }
}
