package com.acesso60.ui.screens

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.acesso60.R
import com.acesso60.ui.components.Heading
import com.acesso60.ui.components.ScreenFrame
import com.acesso60.ui.theme.Ink
import com.acesso60.ui.theme.Paper
import com.acesso60.ui.theme.Petrol

/** Estado transitório: o pedido foi aceito e está sendo executado. Sem botões, de propósito. */
@Composable
fun WorkingScreen(modifier: Modifier = Modifier) {
    ScreenFrame(
        background = Paper,
        contentColor = Ink,
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(88.dp),
            color = Petrol,
            strokeWidth = 10.dp,
        )
        Heading(text = stringResource(R.string.ui_working_title), announce = true)
        Text(
            text = stringResource(R.string.ui_working_hint),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
