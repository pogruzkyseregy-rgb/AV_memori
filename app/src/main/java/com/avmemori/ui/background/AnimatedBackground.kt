package com.avmemori.ui.background

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.avmemori.model.BackgroundStyle
import com.avmemori.ui.theme.AvColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

private val TWO_PI = (2 * PI).toFloat()
private val DEG = (PI / 180).toFloat()

private fun fract(v: Float): Float = v - floor(v)

/**
 * Анимированный фон. Все стили рисуются на Canvas одним циклом фазы (60 секунд),
 * перекомпоновки экрана не происходит — только перерисовка.
 */
@Composable
fun AnimatedBackground(style: BackgroundStyle, modifier: Modifier = Modifier) {
    if (style == BackgroundStyle.OFF) {
        Box(modifier = modifier.fillMaxSize().background(AvColors.Paper))
        return
    }

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
        when (style) {
            BackgroundStyle.BLOBS -> drawBlobs(phase)
            BackgroundStyle.QUANTUM -> drawQuantum(phase)
            BackgroundStyle.FLOWERS -> drawFlowers(phase)
            BackgroundStyle.CARS -> drawCars(phase)
            BackgroundStyle.OFF -> Unit
        }
    }
}

// Пастельные пятна и мерцающие точки.
private fun DrawScope.drawBlobs(phase: Float) {
    val colors = listOf(
        Color(0xFFF6D5C3),
        Color(0xFFE8DAF0),
        Color(0xFFD9E4F0),
        Color(0xFFDDEBDD),
        Color(0xFFF6E3C7),
    )
    val w = size.width
    val h = size.height
    colors.forEachIndexed { i, color ->
        val a = (phase + i * 0.2f) * TWO_PI
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
        val x = w * fract(i * 0.618f + phase * 0.3f)
        val y = h * fract(i * 0.381f)
        val twinkle = 0.5f + 0.5f * sin((phase * 6f + i) * TWO_PI)
        drawCircle(
            color = Color.White.copy(alpha = 0.3f + 0.5f * twinkle),
            radius = 2.5f + 2f * twinkle,
            center = Offset(x, y),
        )
    }
}

// Квант: орбиты с электронами, волновые функции и частицы.
private fun DrawScope.drawQuantum(phase: Float) {
    val w = size.width
    val h = size.height
    val c = Offset(w / 2f, h / 2f)
    val base = minOf(w, h)
    val palette = listOf(Color(0xFFB9A7E0), Color(0xFF9FC6E8), Color(0xFFE7B4C8))

    for (k in 0 until 3) {
        val rx = base * (0.26f + 0.1f * k)
        val ry = rx * 0.36f
        val angle = 30f + k * 60f + phase * 360f * (if (k % 2 == 0) 1f else -0.5f)
        rotate(degrees = angle, pivot = c) {
            drawOval(
                color = palette[k].copy(alpha = 0.55f),
                topLeft = Offset(c.x - rx, c.y - ry),
                size = Size(rx * 2f, ry * 2f),
                style = Stroke(width = 1.5.dp.toPx()),
            )
        }
        // Электрон летает по орбите.
        val t = fract(phase * (1f + k * 0.5f)) * TWO_PI
        val a = angle * DEG
        val ex = rx * cos(t)
        val ey = ry * sin(t)
        drawCircle(
            color = palette[k],
            radius = 5.dp.toPx(),
            center = Offset(c.x + ex * cos(a) - ey * sin(a), c.y + ex * sin(a) + ey * cos(a)),
        )
    }

    // Ядро.
    drawCircle(Color(0xFFD9A7A0).copy(alpha = 0.6f), radius = base * 0.04f, center = c)

    // Волновые линии.
    for (k in 0 until 2) {
        val path = Path()
        val amp = h * (0.025f + 0.012f * k)
        val yBase = h * (0.2f + 0.6f * k)
        for (i in 0..60) {
            val x = w * i / 60f
            val y = yBase + amp * sin(i / 60f * TWO_PI * (2 + k) + phase * TWO_PI * (k + 1))
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color = palette[k].copy(alpha = 0.5f), style = Stroke(width = 1.5.dp.toPx()))
    }

    for (i in 0 until 40) {
        val x = w * fract(i * 0.37f + phase * 0.4f)
        val y = h * fract(i * 0.61f)
        drawCircle(Color.White.copy(alpha = 0.7f), radius = 1.5.dp.toPx(), center = Offset(x, y))
    }
}

// Цветочки: медленно плывут и вращаются.
private fun DrawScope.drawFlowers(phase: Float) {
    val w = size.width
    val h = size.height
    val petals = listOf(
        Color(0xFFF6C1CC),
        Color(0xFFF9D9B8),
        Color(0xFFD9C8F0),
        Color(0xFFBFE3D2),
    )
    for (i in 0 until 10) {
        val x = w * fract(i * 0.173f + phase * (0.15f + (i % 3) * 0.1f))
        val y = h * (0.08f + 0.84f * fract(i * 0.381f)) + h * 0.02f * sin(phase * TWO_PI * 2f + i)
        val r = minOf(w, h) * (0.035f + 0.02f * (i % 4))
        val rot = phase * 360f * (if (i % 2 == 0) 1f else -1f) + i * 37f
        drawFlower(Offset(x, y), r, rot, petals[i % petals.size])
    }
}

private fun DrawScope.drawFlower(center: Offset, r: Float, rot: Float, petal: Color) {
    for (p in 0 until 5) {
        val a = (rot + p * 72f) * DEG
        drawCircle(
            color = petal.copy(alpha = 0.55f),
            radius = r * 0.5f,
            center = Offset(center.x + r * 0.6f * cos(a), center.y + r * 0.6f * sin(a)),
        )
    }
    drawCircle(Color(0xFFFFF3D6), radius = r * 0.3f, center = center)
}

// Машинки: по дороге едут игрушечные машинки в обе стороны.
private fun DrawScope.drawCars(phase: Float) {
    val w = size.width
    val h = size.height
    val unit = minOf(w, h) * 0.035f
    val colors = listOf(
        Color(0xFFF6A5B5),
        Color(0xFFA8E0C8),
        Color(0xFFA9C8F0),
        Color(0xFFF7C998),
        Color(0xFFC8B4E8),
    )
    val span = w + unit * 12f
    for (i in 0 until 5) {
        val y = h * (0.22f + i * 0.14f)
        val speed = 0.5f + (i % 3) * 0.25f
        val f = fract(phase * speed + i * 0.29f)
        val leftToRight = i % 2 == 0
        val x = if (leftToRight) -unit * 6f + span * f else w + unit * 6f - span * f
        drawCar(x, y, unit, colors[i])
    }
}

private fun DrawScope.drawCar(left: Float, top: Float, unit: Float, body: Color) {
    drawRoundRect(
        color = body.copy(alpha = 0.85f),
        topLeft = Offset(left, top),
        size = Size(unit * 4f, unit * 1.2f),
        cornerRadius = CornerRadius(unit * 0.4f),
    )
    drawRoundRect(
        color = Color.White.copy(alpha = 0.75f),
        topLeft = Offset(left + unit * 1.1f, top - unit * 0.7f),
        size = Size(unit * 2f, unit * 0.8f),
        cornerRadius = CornerRadius(unit * 0.3f),
    )
    val wheel = Color(0xFF3B3430).copy(alpha = 0.8f)
    drawCircle(wheel, radius = unit * 0.5f, center = Offset(left + unit * 0.9f, top + unit * 1.2f))
    drawCircle(wheel, radius = unit * 0.5f, center = Offset(left + unit * 3.1f, top + unit * 1.2f))
}
