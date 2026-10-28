package com.avmemori.ui.background

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.avmemori.ui.theme.AvColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val blobColors = listOf(
    Color(0xFFF6D5C3), // персиковый
    Color(0xFFE8DAF0), // лавандовый
    Color(0xFFD9E4F0), // небесный
    Color(0xFFDDEBDD), // мятный
    Color(0xFFF6E3C7), // песочный
)

/**
 * Медленно дрейфующие цветные пятна и мерцающие точки.
 * Цикл 60 секунд, перерисовка только в фазе отрисовки (без перекомпоновки).
 */
@Composable
fun AnimatedBackground(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "background")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 60_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "phase",
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(AvColors.Paper)
        val w = size.width
        val h = size.height
        val twoPi = (2 * PI).toFloat()

        blobColors.forEachIndexed { i, color ->
            val a = (phase + i * 0.2f) * twoPi
            val center = Offset(
                x = w * (0.5f + 0.4f * cos(a + i)),
                y = h * (0.5f + 0.35f * sin(a * 0.7f + i * 1.3f)),
            )
            val radius = minOf(w, h) * (0.45f + 0.08f * (i % 3))
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color.copy(alpha = 0.6f), color.copy(alpha = 0f)),
                    center = center,
                    radius = radius,
                ),
                radius = radius,
                center = center,
            )
        }

        for (i in 0 until 16) {
            val x = w * ((i * 0.618f + phase * 0.3f) % 1f)
            val y = h * ((i * 0.381f) % 1f)
            val twinkle = 0.5f + 0.5f * sin((phase * 6f + i) * twoPi)
            drawCircle(
                color = Color.White.copy(alpha = 0.3f + 0.5f * twinkle),
                radius = 2.5f + 2f * twinkle,
                center = Offset(x, y),
            )
        }
    }
}
