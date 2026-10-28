package com.acesso60.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.acesso60.R
import com.acesso60.ui.components.BigButton
import com.acesso60.ui.components.Heading
import com.acesso60.ui.components.MicGlyph
import com.acesso60.ui.components.ScreenFrame
import com.acesso60.ui.theme.Petrol

/** UI-02 — Escuta: microfone ativo bem visível, transcrição e cancelamento. */
@Composable
fun ListeningScreen(
    transcript: String,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenFrame(
        background = Petrol,
        contentColor = Color.White,
        modifier = modifier,
        darkBackground = true,
        horizontalAlignment = Alignment.CenterHorizontally,
        actions = {
            BigButton(
                text = stringResource(R.string.ui_cancel),
                onClick = onCancel,
                filled = false,
                color = Color.White,
            )
        },
    ) {
        PulsingMic()

        Text(
            text = stringResource(R.string.ui_listening_mic_on),
            style = MaterialTheme.typography.titleLarge,
        )
        Heading(
            text = stringResource(R.string.ui_listening_title),
            announce = true,
        )
        Text(
            text = stringResource(R.string.ui_listening_hint),
            style = MaterialTheme.typography.bodyLarge,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Color.White, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.ui_listening_heard),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = transcript.ifBlank { stringResource(R.string.ui_listening_nothing) },
                style = MaterialTheme.typography.headlineMedium,
            )
        }
    }
}

@Composable
private fun PulsingMic() {
    val transition = rememberInfiniteTransition(label = "mic-pulse")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1600, easing = LinearEasing), RepeatMode.Restart),
        label = "mic-pulse-progress",
    )
    Box(
        modifier = Modifier
            .size(200.dp)
            .clearAndSetSemantics { },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val maxRadius = size.minDimension / 2f
            listOf(0f, 0.5f).forEach { offset ->
                val p = (progress + offset) % 1f
                drawCircle(
                    color = Color.White.copy(alpha = (1f - p) * 0.5f),
                    radius = maxRadius * (0.55f + 0.45f * p),
                    style = Stroke(width = 6.dp.toPx()),
                )
            }
        }
        Box(
            modifier = Modifier
                .size(112.dp)
                .background(Color.White, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            MicGlyph(color = Petrol, modifier = Modifier.size(64.dp))
        }
    }
}
