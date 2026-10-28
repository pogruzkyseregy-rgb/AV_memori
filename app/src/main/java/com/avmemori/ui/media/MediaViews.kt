package com.avmemori.ui.media

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.avmemori.model.Media
import com.avmemori.model.PlacedSticker
import com.avmemori.model.Sticker
import com.avmemori.ui.feed.LocalPhoto
import com.avmemori.ui.theme.AvColors
import kotlin.math.roundToInt

/** Картинка для показа: у видео это кадр-превью, у фото — сам файл. */
fun thumbPathOf(media: Media): String = media.thumbPath ?: media.path

private val STICKER_SIZE: Dp = 96.dp

/** Значок «▶» поверх видео. */
@Composable
fun VideoBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.9f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "▶", fontSize = 16.sp, color = AvColors.Ink)
    }
}

/** Одно фото или видео в полную ширину. */
@Composable
fun MediaThumb(media: Media, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        LocalPhoto(path = thumbPathOf(media), modifier = Modifier.fillMaxWidth())
        if (media.isVideo) VideoBadge(Modifier.align(Alignment.Center))
    }
}

/**
 * Область медиа блока: фото, стопка фото или цветной «стикер», если фото нет.
 * overlay — стикеры поверх этой области.
 */
@Composable
fun MediaArea(
    media: List<Media>,
    colorArgb: Long,
    onOpen: (() -> Unit)? = null,
    overlay: @Composable BoxScope.() -> Unit = {},
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        when {
            media.isEmpty() -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color(colorArgb), RoundedCornerShape(6.dp)),
            )
            media.size == 1 -> MediaThumb(
                media = media.first(),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .then(if (onOpen != null) Modifier.clickable { onOpen() } else Modifier),
            )
            else -> PhotoPile(media = media, onClick = { onOpen?.invoke() })
        }
        overlay()
    }
}

/** Несколько фото или видео: стопка из двух, с бейджем количества. */
@Composable
private fun PhotoPile(media: List<Media>, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(bottom = 6.dp, end = 6.dp),
    ) {
        LocalPhoto(
            path = thumbPathOf(media[1]),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .fillMaxWidth(0.9f)
                .graphicsLayer { rotationZ = 4f }
                .clip(RoundedCornerShape(4.dp)),
        )
        LocalPhoto(
            path = thumbPathOf(media[0]),
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth(0.9f)
                .graphicsLayer { rotationZ = -3f }
                .clip(RoundedCornerShape(4.dp)),
        )
        Text(
            text = "${media.size} шт.",
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(4.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.9f))
                .padding(horizontal = 8.dp, vertical = 2.dp),
            fontSize = 11.sp,
            color = AvColors.Ink,
        )
    }
}

/**
 * Слой стикеров поверх блока.
 * interactive = true: стикеры можно таскать, масштабировать и поворачивать.
 * Позиции хранятся в долях от размера области, поэтому не зависят от экрана.
 */
@Composable
fun BoxScope.StickerLayer(
    placed: List<PlacedSticker>,
    library: Map<Long, Sticker>,
    interactive: Boolean = false,
    selectedIndex: Int? = null,
    onSelect: (Int?) -> Unit = {},
    onChange: (Int, PlacedSticker) -> Unit = { _, _ -> },
) {
    val density = LocalDensity.current
    var areaPx by remember { mutableStateOf(IntSize.Zero) }
    val basePx = with(density) { STICKER_SIZE.toPx() }

    Box(
        modifier = Modifier
            .matchParentSize()
            .onSizeChanged { areaPx = it }
            .then(
                if (interactive) {
                    Modifier.pointerInput(Unit) { detectTapGestures(onTap = { onSelect(null) }) }
                } else {
                    Modifier
                },
            ),
    ) {
        if (areaPx == IntSize.Zero) return@Box

        placed.forEachIndexed { index, p ->
            val sticker = library[p.stickerId] ?: return@forEachIndexed
            val currentP by rememberUpdatedState(p)
            val left = p.x * areaPx.width - basePx / 2f
            val top = p.y * areaPx.height - basePx / 2f

            Box(
                modifier = Modifier
                    .offset { IntOffset(left.roundToInt(), top.roundToInt()) }
                    .size(STICKER_SIZE)
                    .graphicsLayer {
                        scaleX = p.scale
                        scaleY = p.scale
                        rotationZ = p.rotation
                    }
                    .then(
                        if (selectedIndex == index) {
                            Modifier.border(2.dp, AvColors.Accent, RoundedCornerShape(8.dp))
                        } else {
                            Modifier
                        },
                    )
                    .then(
                        if (interactive) {
                            Modifier
                                .pointerInput(index) {
                                    detectTapGestures(onTap = { onSelect(index) })
                                }
                                .pointerInput(index) {
                                    detectTransformGestures { _, pan, zoom, rot ->
                                        val cur = currentP
                                        onChange(
                                            index,
                                            cur.copy(
                                                x = (cur.x + pan.x / areaPx.width).coerceIn(0f, 1f),
                                                y = (cur.y + pan.y / areaPx.height).coerceIn(0f, 1f),
                                                scale = (cur.scale * zoom).coerceIn(0.3f, 4f),
                                                rotation = cur.rotation + rot,
                                            ),
                                        )
                                    }
                                }
                        } else {
                            Modifier
                        },
                    ),
            ) {
                LocalPhoto(
                    path = sticker.path,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
            }
        }
    }
}
