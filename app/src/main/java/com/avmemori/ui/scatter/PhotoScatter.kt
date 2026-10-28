package com.avmemori.ui.scatter

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.runtime.key
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

/** Место фото до жестов пальцем: центр и небольшой наклон. */
private data class Spot(val target: Offset, val angle: Float)

/** Жесты пальцем: сдвиг, увеличение и поворот (в градусах). */
private data class Pose(val offset: Offset = Offset.Zero, val scale: Float = 1f, val rotation: Float = 0f)

/**
 * Фото и видео из блока разлетаются по экрану веером.
 * Фото можно таскать, увеличивать щипком и крутить двумя пальцами.
 * Тап по фото открывает его на весь экран, тап по видео — проигрыватель.
 */
@Composable
fun PhotoScatter(
    media: List<Media>,
    seed: Long,
    onClose: () -> Unit,
    onOpenVideo: (Media) -> Unit,
) {
    // Полноэкранное фото открыто — «назад» закрывает его, а не всю россыпь.
    var fullPhoto by remember(seed) { mutableStateOf<Media?>(null) }
    BackHandler(enabled = fullPhoto == null, onBack = onClose)

    val density = LocalDensity.current
    var areaPx by remember { mutableStateOf(IntSize.Zero) }

    // Жесты каждого фото: сдвиг, масштаб и поворот.
    val poses = remember(seed) { mutableStateMapOf<Int, Pose>() }
    // Порядок наложения: последний — сверху. Тронутое фото поднимается наверх.
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
            media.indices.forEach { i ->
                key(i) {
                    val spot = spots[i]
                    val pose = poses[i] ?: Pose()
                    val x = startX + (spot.target.x + pose.offset.x - startX) * t
                    val y = startY + (spot.target.y + pose.offset.y - startY) * t
                    val item = media[i]
                    val layer = order.indexOf(i).toFloat()

                    Surface(
                        modifier = Modifier
                            .zIndex(layer)
                            .offset { IntOffset(x.roundToInt(), y.roundToInt()) }
                            .size(itemWdp, itemHdp)
                            .graphicsLayer {
                                rotationZ = spot.angle * t + pose.rotation
                                scaleX = pose.scale
                                scaleY = pose.scale
                            }
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, rotation ->
                                    val p = poses[i] ?: Pose()
                                    poses[i] = p.copy(
                                        offset = p.offset + pan,
                                        scale = (p.scale * zoom).coerceIn(0.5f, 3f),
                                        rotation = p.rotation + rotation,
                                    )
                                }
                            }
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = { _ ->
                                        order.remove(i)
                                        order.add(i)
                                    },
                                    onTap = { _ ->
                                        if (item.isVideo) onOpenVideo(item) else fullPhoto = item
                                    },
                                )
                            },
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
            text = "тяни, крути, увеличивай",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
            fontFamily = NoteFont,
            fontSize = 20.sp,
            color = AvColors.InkSoft,
        )

        fullPhoto?.let { photo ->
            FullPhotoView(path = photo.path, onClose = { fullPhoto = null })
        }
    }
}
