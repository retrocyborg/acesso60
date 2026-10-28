package com.acesso60.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.acesso60.ui.theme.Petrol

/** Botão grande: altura mínima de 72dp (alvo de toque bem acima dos 48dp recomendados). */
@Composable
fun BigButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    filled: Boolean = true,
    color: Color = Petrol,
    onColor: Color = Color.White,
) {
    val shape = RoundedCornerShape(20.dp)
    val size = modifier
        .fillMaxWidth()
        .heightIn(min = 72.dp)
    if (filled) {
        Button(
            onClick = onClick,
            modifier = size,
            shape = shape,
            colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = onColor),
        ) {
            Text(text, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = size,
            shape = shape,
            border = BorderStroke(3.dp, color),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.Transparent,
                contentColor = color,
            ),
        ) {
            Text(text, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
        }
    }
}
