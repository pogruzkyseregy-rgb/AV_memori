package com.avmemori.ui.scatter

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.avmemori.ui.feed.LocalPhoto
import com.avmemori.ui.theme.AvColors
import com.avmemori.ui.theme.BodyFont

/**
 * Фото на весь экран. Щипок — увеличить, два пальца — повернуть, тап двумя — вернуть вид.
 * Закрывается крестиком или «назад».
 */
@Composable
fun FullPhotoView(path: String, onClose: () -> Unit) {
    BackHandler(onBack = onClose)

    // Фото «раскрывается» поверх остальных: появляется из чуть меньшего размера.
    val appear = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        appear.animateTo(1f, spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow))
    }

    var scale by remember { mutableFloatStateOf(1f) }
    var rotation by remember { mutableFloatStateOf(0f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    fun reset() {
        scale = 1f
        rotation = 0f
        offset = Offset.Zero
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AvColors.Paper)
            .graphicsLayer {
                alpha = appear.value
                val s = 0.85f + 0.15f * appear.value
                scaleX = s
                scaleY = s
            }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, rot ->
                    scale = (scale * zoom).coerceIn(1f, 5f)
                    rotation += rot
                    // Сдвиг только когда фото увеличено, иначе оно остаётся по центру.
                    offset = if (scale > 1f) offset + pan else Offset.Zero
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(onDoubleTap = { _ -> reset() })
            },
        contentAlignment = Alignment.Center,
    ) {
        LocalPhoto(
            path = path,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    rotationZ = rotation
                    translationX = offset.x
                    translationY = offset.y
                },
            contentScale = ContentScale.Fit,
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(16.dp)
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White)
                .clickable(onClick = onClose),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "✕", fontSize = 18.sp, color = AvColors.Ink)
        }

        Text(
            text = "двойной тап — вернуть вид",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
            fontFamily = BodyFont,
            fontSize = 12.sp,
            color = AvColors.InkSoft,
        )
    }
}
