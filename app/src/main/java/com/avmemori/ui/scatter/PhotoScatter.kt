package com.avmemori.ui.scatter

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.avmemori.model.Media
import com.avmemori.ui.feed.LocalPhoto
import com.avmemori.ui.media.VideoBadge
import com.avmemori.ui.media.thumbPathOf
import com.avmemori.ui.theme.AvColors
import com.avmemori.ui.theme.NoteFont
import kotlin.math.roundToInt
import kotlin.random.Random

/** Что показываем в разлёте: медиа блока или момента и зерно случайности. */
data class Viewer(val media: List<Media>, val seed: Long)

/**
 * Фото и видео из блока разлетаются по экрану, их можно таскать пальцем.
 * Тап по видео открывает проигрыватель. Закрывается крестиком или «назад».
 */
@Composable
fun PhotoScatter(
    media: List<Media>,
    seed: Long,
    onClose: () -> Unit,
    onOpenVideo: (Media) -> Unit,
) {
    BackHandler(onBack = onClose)

    val density = LocalDensity.current
    var areaPx by remember { mutableStateOf(IntSize.Zero) }

    // Анимация «разлёта» от центра: 0 — все в стопке, 1 — разбросаны.
    val progress = remember(seed) { Animatable(0f) }
    // Целевые позиции (px), углы и порядок отрисовки (последний — сверху).
    val targets = remember(seed) { mutableStateListOf<Offset>() }
    val angles = remember(seed) { mutableStateListOf<Float>() }
    val order = remember(seed) { mutableStateListOf<Int>() }

    val itemWidthPx = areaPx.width * 0.42f
    val itemHeightPx = itemWidthPx * 1.25f
    val topMarginPx = with(density) { 84.dp.toPx() }
    val bottomMarginPx = with(density) { 90.dp.toPx() }

    LaunchedEffect(areaPx) {
        if (areaPx == IntSize.Zero || targets.isNotEmpty()) return@LaunchedEffect
        val rnd = Random(seed)
        val maxX = (areaPx.width - itemWidthPx).coerceAtLeast(0f)
        val maxY = (areaPx.height - topMarginPx - bottomMarginPx - itemHeightPx).coerceAtLeast(0f)
        media.indices.forEach { i ->
            targets.add(Offset(rnd.nextFloat() * maxX, topMarginPx + rnd.nextFloat() * maxY))
            angles.add(rnd.nextFloat() * 24f - 12f)
            order.add(i)
        }
        progress.animateTo(1f, tween(durationMillis = 900, easing = FastOutSlowInEasing))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AvColors.Paper.copy(alpha = 0.97f))
            .onSizeChanged { areaPx = it },
    ) {
        if (targets.size == media.size && areaPx != IntSize.Zero) {
            val t = progress.value
            val startX = (areaPx.width - itemWidthPx) / 2f
            val startY = (areaPx.height - itemHeightPx) / 2f
            val itemW = with(density) { itemWidthPx.toDp() }
            val itemH = with(density) { itemHeightPx.toDp() }

            order.toList().forEach { i ->
                val target = targets[i]
                val x = startX + (target.x - startX) * t
                val y = startY + (target.y - startY) * t
                val item = media[i]
                Surface(
                    modifier = Modifier
                        .offset { IntOffset(x.roundToInt(), y.roundToInt()) }
                        .size(itemW, itemH)
                        .graphicsLayer { rotationZ = angles[i] * t }
                        .pointerInput(i) {
                            detectDragGestures(
                                onDragStart = {
                                    order.remove(i)
                                    order.add(i)
                                },
                                onDrag = { change, drag ->
                                    change.consume()
                                    targets[i] = targets[i] + drag
                                },
                            )
                        }
                        .clickable(enabled = item.isVideo) { onOpenVideo(item) },
                    shape = RoundedCornerShape(4.dp),
                    color = Color.White,
                    shadowElevation = 8.dp,
                ) {
                    Box(modifier = Modifier.padding(8.dp).fillMaxSize()) {
                        LocalPhoto(
                            path = thumbPathOf(item),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                        if (item.isVideo) VideoBadge(Modifier.align(Alignment.Center))
                    }
                }
            }
        }

        // Крестик выхода: поверх всего.
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
            text = "тяни, как хочешь",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
            fontFamily = NoteFont,
            fontSize = 20.sp,
            color = AvColors.InkSoft,
        )
    }
}
