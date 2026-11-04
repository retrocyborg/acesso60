package com.acesso60.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.acesso60.R
import com.acesso60.ui.components.BigButton
import com.acesso60.ui.components.Heading
import com.acesso60.ui.components.ScreenFrame
import com.acesso60.ui.theme.Caution
import com.acesso60.ui.theme.CautionSoft
import com.acesso60.ui.theme.Ink
import com.acesso60.ui.theme.Petrol

/** UI-03 — Confirmação de ação sensível: descrição clara e botões Confirmar / Cancelar. */
@Composable
fun ConfirmationScreen(
    actionDescription: String,
    simulated: Boolean,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenFrame(
        background = CautionSoft,
        contentColor = Ink,
        modifier = modifier,
        actions = {
            BigButton(
                text = stringResource(R.string.ui_confirm),
                onClick = onConfirm,
                color = Petrol,
            )
            BigButton(
                text = stringResource(R.string.ui_cancel),
                onClick = onCancel,
                filled = false,
                color = Ink,
            )
        },
    ) {
        Heading(text = stringResource(R.string.ui_confirm_title), announce = true)

        Box(
            Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(20.dp))
                .border(3.dp, Caution, RoundedCornerShape(20.dp))
                .padding(20.dp),
        ) {
            Text(text = actionDescription, style = MaterialTheme.typography.headlineMedium)
        }

        if (simulated) {
            Text(
                text = stringResource(R.string.ui_confirm_simulated),
                style = MaterialTheme.typography.titleLarge,
            )
        }
        Text(
            text = stringResource(R.string.ui_confirm_cancel_hint),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
