package com.acesso60.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp

enum class BadgeKind { SUCCESS, FAILURE, CANCELLED, WARNING }

/**
 * Selo desenhado em Canvas. Sempre acompanha um texto: nunca é a única indicação do estado.
 * É decorativo para leitores de tela (o texto ao lado é lido).
 */
@Composable
fun StatusBadge(kind: BadgeKind, color: Color, diameter: Dp, modifier: Modifier = Modifier) {
    Canvas(modifier.size(diameter).clearAndSetSemantics { }) {
        val w = this.size.width
        val stroke = Stroke(width = w * 0.09f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawCircle(color)
        when (kind) {
            BadgeKind.SUCCESS -> {
                val check = Path().apply {
                    moveTo(w * 0.28f, w * 0.52f)
                    lineTo(w * 0.44f, w * 0.68f)
                    lineTo(w * 0.74f, w * 0.34f)
                }
                drawPath(check, Color.White, style = stroke)
            }

            BadgeKind.FAILURE -> {
                drawLine(Color.White, Offset(w * 0.32f, w * 0.32f), Offset(w * 0.68f, w * 0.68f), stroke.width, StrokeCap.Round)
                drawLine(Color.White, Offset(w * 0.68f, w * 0.32f), Offset(w * 0.32f, w * 0.68f), stroke.width, StrokeCap.Round)
            }

            BadgeKind.CANCELLED -> {
                drawLine(Color.White, Offset(w * 0.30f, w * 0.50f), Offset(w * 0.70f, w * 0.50f), stroke.width, StrokeCap.Round)
            }

            BadgeKind.WARNING -> {
                drawLine(Color.White, Offset(w * 0.50f, w * 0.26f), Offset(w * 0.50f, w * 0.58f), stroke.width, StrokeCap.Round)
                drawCircle(Color.White, radius = w * 0.055f, center = Offset(w * 0.50f, w * 0.72f))
            }
        }
    }
}

/** Ícone de microfone desenhado em Canvas (decorativo). */
@Composable
fun MicGlyph(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.clearAndSetSemantics { }) {
        val w = size.width
        val h = size.height
        val line = w * 0.06f
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.36f, h * 0.12f),
            size = Size(w * 0.28f, h * 0.46f),
            cornerRadius = CornerRadius(w * 0.14f, w * 0.14f),
        )
        drawArc(
            color = color,
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.24f, h * 0.30f),
            size = Size(w * 0.52f, h * 0.42f),
            style = Stroke(width = line, cap = StrokeCap.Round),
        )
        drawLine(color, Offset(w * 0.50f, h * 0.72f), Offset(w * 0.50f, h * 0.88f), line, StrokeCap.Round)
        drawLine(color, Offset(w * 0.36f, h * 0.88f), Offset(w * 0.64f, h * 0.88f), line, StrokeCap.Round)
    }
}
