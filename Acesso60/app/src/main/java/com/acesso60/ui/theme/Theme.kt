package com.acesso60.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.acesso60.ui.preferences.TextScale

private val Acesso60ColorScheme = lightColorScheme(
    primary = Petrol,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    background = Paper,
    onBackground = Ink,
    surface = androidx.compose.ui.graphics.Color.White,
    onSurface = Ink,
    error = Alert,
    onError = androidx.compose.ui.graphics.Color.White,
    outline = OutlineGray,
)

@Composable
fun Acesso60Theme(
    textScale: TextScale = TextScale.NORMAL,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    // Multiplica o ajuste de fonte do sistema pela escolha do usuário.
    val scaled = Density(density.density, density.fontScale * textScale.factor)
    CompositionLocalProvider(LocalDensity provides scaled) {
        MaterialTheme(
            colorScheme = Acesso60ColorScheme,
            typography = Acesso60Typography,
            content = content,
        )
    }
}
