package com.avmemori.ui.frames

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.avmemori.model.PhotoFrame

/**
 * Обрамляет фото рамкой пользователя: цветное поле, белое поле (отступ) и при желании двойная линия.
 * При нулевой толщине и отступе выглядит как обычное фото.
 */
@Composable
fun FramedMedia(frame: PhotoFrame, content: @Composable () -> Unit) {
    val innerRadius = (frame.radiusDp - frame.widthDp).coerceAtLeast(0f).dp
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(frame.radiusDp.dp))
            .background(Color(frame.colorArgb))
            .padding(frame.widthDp.dp),
    ) {
        val inner = Modifier.fillMaxWidth()
        val lined = if (frame.doubleLine) {
            inner.padding(3.dp).border(1.dp, Color.White, RoundedCornerShape(innerRadius)).padding(2.dp)
        } else {
            inner
        }
        Box(modifier = lined) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(innerRadius))
                    .background(Color.White)
                    .padding(frame.insetDp.dp),
            ) {
                content()
            }
        }
    }
}
