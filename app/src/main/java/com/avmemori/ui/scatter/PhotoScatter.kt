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
import androidx.compose.runtime.key
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
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
import androidx.compose.ui.zIndex
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

/** Место фото на экране до сдвига пальцем: центр и небольшой наклон. */
private data class Spot(val target: Offset, val angle: Float)

/**
 * Фото и видео из блока разлетаются по экрану веером, их можно таскать пальцем.
 * Тап по видео открывает проигрыватель. Закрывается крестиком или «назад».
 *
 * Положение каждого фото считается прямо при отрисовке (по seed и размеру экрана),
 * без отдельных фоновых эффектов, поэтому стопка всегда раскладывается.
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

    // Сдвиги, которые фото получили пальцем (поверх разлёта).
    val dragOffsets = remember(seed) { mutableStateMapOf<Int, Offset>() }
    // Порядок отрисовки: последний — сверху. Тронутое фото поднимается наверх.
    val order = remember(seed) { mutableStateListOf<Int>().apply { addAll(media.indices) } }
    // 0 — все фото в стопке в центре, 1 — разлетелись.
    val progress = remember(seed) { Animatable(0f) }

    LaunchedEffect(seed) {
        progress.animateTo(1f, tween(durationMillis = 900, easing = FastOutSlowInEasing))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AvColors.Paper.copy(alpha = 0.97f))
            .onSizeChanged { areaPx = it },
    ) {
        if (areaPx != IntSize.Zero) {
            val w = areaPx.width.toFloat()
            val h = areaPx.height.toFloat()
            val itemW = w * 0.42f
            val itemH = itemW * 1.25f
            val topM = with(density) { 84.dp.toPx() }
            val bottomM = with(density) { 90.dp.toPx() }

            val spots = remember(seed, areaPx) {
                val rnd = Random(seed)
                val maxX = (w - itemW).coerceAtLeast(0f)
                val maxY = (h - topM - bottomM - itemH).coerceAtLeast(0f)
                List(media.size) {
                    Spot(
                        target = Offset(rnd.nextFloat() * maxX, topM + rnd.nextFloat() * maxY),
                        angle = rnd.nextFloat() * 24f - 12f,
                    )
                }
            }

            val t = progress.value
            val startX = (w - itemW) / 2f
            val startY = (h - itemH) / 2f
            val itemWdp = with(density) { itemW.toDp() }
            val itemHdp = with(density) { itemH.toDp() }

            // Каждое фото — свой постоянный узел (key), порядок наложения задаёт zIndex.
            // Раньше порядок менялся перестановкой детей, и жест сбрасывался — отсюда дёрганье.
            media.indices.forEach { i ->
                key(i) {
                    val spot = spots[i]
                    val drag = dragOffsets[i] ?: Offset.Zero
                    val x = startX + (spot.target.x + drag.x - startX) * t
                    val y = startY + (spot.target.y + drag.y - startY) * t
                    val item = media[i]
                    val layer = order.indexOf(i).toFloat()

                    Surface(
                        modifier = Modifier
                            .zIndex(layer)
                            .offset { IntOffset(x.roundToInt(), y.roundToInt()) }
                            .size(itemWdp, itemHdp)
                            .graphicsLayer { rotationZ = spot.angle * t }
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = {
                                        order.remove(i)
                                        order.add(i)
                                    },
                                    onDrag = { change, delta ->
                                        change.consume()
                                        dragOffsets[i] = (dragOffsets[i] ?: Offset.Zero) + delta
                                    },
                                )
                            }
                            .then(if (item.isVideo) Modifier.clickable { onOpenVideo(item) } else Modifier),
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
