package com.avmemori.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.avmemori.ui.theme.AvColors

/** Круглая область нажатия 32 dp с иконкой внутри. */
@Composable
fun IconTap(description: String, onClick: () -> Unit, icon: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        icon()
    }
}

/** Иконка «поделиться»: три точки, соединённые линиями. */
@Composable
fun ShareIcon(color: Color = AvColors.InkSoft) {
    Canvas(modifier = Modifier.size(18.dp)) {
        val w = size.width
        val h = size.height
        val sw = 1.6.dp.toPx()
        val a = Offset(w * 0.75f, h * 0.25f)
        val b = Offset(w * 0.25f, h * 0.5f)
        val c = Offset(w * 0.75f, h * 0.75f)
        drawLine(color = color, start = a, end = b, strokeWidth = sw, cap = StrokeCap.Round)
        drawLine(color = color, start = b, end = c, strokeWidth = sw, cap = StrokeCap.Round)
        val r = w * 0.14f
        listOf(a, b, c).forEach { p ->
            drawCircle(color = color, radius = r, center = p, style = Stroke(width = sw))
        }
    }
}

/** Иконка «изменить»: карандаш. */
@Composable
fun EditIcon(color: Color = AvColors.InkSoft) {
    Canvas(modifier = Modifier.size(18.dp)) {
        val w = size.width
        val h = size.height
        val sw = 1.6.dp.toPx()
        val body = Path().apply {
            moveTo(w * 0.2f, h * 0.8f)
            lineTo(w * 0.28f, h * 0.56f)
            lineTo(w * 0.68f, h * 0.16f)
            lineTo(w * 0.84f, h * 0.32f)
            lineTo(w * 0.44f, h * 0.72f)
            close()
        }
        drawPath(path = body, color = color, style = Stroke(width = sw, join = StrokeJoin.Round))
        drawLine(
            color = color,
            start = Offset(w * 0.56f, h * 0.28f),
            end = Offset(w * 0.72f, h * 0.44f),
            strokeWidth = sw,
            cap = StrokeCap.Round,
        )
    }
}
