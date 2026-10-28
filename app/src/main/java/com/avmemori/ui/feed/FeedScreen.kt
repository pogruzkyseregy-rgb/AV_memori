package com.avmemori.ui.feed

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridLayoutInfo
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.avmemori.audio.AudioPlayer
import com.avmemori.model.Block
import com.avmemori.model.Sticker
import com.avmemori.ui.media.MediaArea
import com.avmemori.ui.media.StickerLayer
import com.avmemori.ui.theme.AvColors
import com.avmemori.ui.theme.NoteFont
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.sin

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FeedScreen(
    blocks: List<Block>,
    library: Map<Long, Sticker>,
    player: AudioPlayer,
    shakeEvent: Int,
    onOpenMedia: (Block) -> Unit,
    onEdit: (Block) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyStaggeredGridState()
    // Видео играет только в одном блоке: том, который сильнее всего виден на экране.
    val activeVideoId by remember(blocks) {
        derivedStateOf { pickActiveVideo(listState.layoutInfo, blocks) }
    }

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        state = listState,
        modifier = modifier,
        // Большой отступ снизу, чтобы кнопки не закрывали последние блоки.
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 130.dp),
        verticalItemSpacing = 16.dp,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        itemsIndexed(blocks, key = { _, block -> block.id }) { index, block ->
            BlockCard(
                block = block,
                index = index,
                library = library,
                player = player,
                inlineVideo = block.id == activeVideoId,
                shakeEvent = shakeEvent,
                onOpenMedia = onOpenMedia,
                onEdit = onEdit,
            )
        }
    }
}

/** Блок с видео, который сильнее всего виден в окне ленты (или null). */
private fun pickActiveVideo(info: LazyStaggeredGridLayoutInfo, blocks: List<Block>): Long? {
    val start = info.viewportStartOffset
    val end = info.viewportEndOffset
    var best: Long? = null
    var bestVisible = 0
    for (item in info.visibleItemsInfo) {
        val block = blocks.getOrNull(item.index) ?: continue
        if (block.media.none { it.isVideo }) continue
        val visible = minOf(item.offset.y + item.size.height, end) - maxOf(item.offset.y, start)
        if (visible > bestVisible) {
            bestVisible = visible
            best = block.id
        }
    }
    return best
}

@Composable
fun BlockCard(
    block: Block,
    index: Int,
    library: Map<Long, Sticker>,
    player: AudioPlayer,
    inlineVideo: Boolean,
    shakeEvent: Int,
    onOpenMedia: (Block) -> Unit,
    onEdit: (Block) -> Unit,
) {
    // «Порядок в беспорядке»: наклон фиксируется по id, карточки не прыгают при прокрутке.
    val tilt = remember(block.id) { ((block.id * 37L) % 7L).toFloat() - 3f }

    val appear = remember(block.id) { Animatable(0f) }
    LaunchedEffect(block.id) {
        delay(index.coerceAtMost(8) * 60L)
        appear.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessLow),
        )
    }

    // Встряхивание: 1 — покой, 0→1 — дрожь, падение и возврат вверх.
    val shake = remember(block.id) { Animatable(1f) }
    var handled by remember(block.id) { mutableIntStateOf(shakeEvent) }
    LaunchedEffect(shakeEvent) {
        if (shakeEvent != handled) {
            handled = shakeEvent
            shake.snapTo(0f)
            shake.animateTo(1f, tween(durationMillis = 1500, easing = LinearEasing))
        }
    }
    val density = LocalDensity.current
    // Разная высота падения, чтобы блоки не падали синхронно.
    val fallPx = with(density) { 180.dp.toPx() } * (0.8f + (block.id % 4).toFloat() * 0.1f)

    val date = remember(block.createdAt) {
        SimpleDateFormat("d MMM", Locale.forLanguageTag("ru")).format(Date(block.createdAt))
    }
    val musicKey = "music-${block.id}"
    val voiceKey = "voice-${block.id}"
    val open: () -> Unit = { onOpenMedia(block) }
    val inlinePath = if (inlineVideo) block.media.firstOrNull { it.isVideo }?.path else null

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                val p = appear.value
                val (shakeRot, shakeDy) = shakeFrame(shake.value, fallPx)
                rotationZ = tilt * p + shakeRot
                alpha = p
                translationY = (1f - p) * 24.dp.toPx() + shakeDy
            },
        shape = RoundedCornerShape(6.dp),
        color = Color.White,
        shadowElevation = 3.dp,
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            MediaArea(
                media = block.media,
                colorArgb = block.colorArgb,
                onOpen = if (block.media.isEmpty()) null else open,
                inlineVideoPath = inlinePath,
                overlay = { StickerLayer(placed = block.stickers, library = library) },
            )

            if (block.note.isNotBlank()) {
                Text(
                    text = block.note,
                    modifier = Modifier.padding(top = 8.dp, start = 4.dp, end = 4.dp),
                    fontFamily = NoteFont,
                    fontSize = 19.sp,
                    color = AvColors.Ink,
                )
            }

            val music = block.musicPath
            val voice = block.voicePath
            if (music != null || voice != null) {
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (music != null) {
                        FilterChip(
                            selected = player.playingKey == musicKey,
                            onClick = { player.toggle(musicKey, music) },
                            label = {
                                Text(if (player.playingKey == musicKey) "■ музыка" else "♪ музыка")
                            },
                        )
                    }
                    if (voice != null) {
                        FilterChip(
                            selected = player.playingKey == voiceKey,
                            onClick = { player.toggle(voiceKey, voice) },
                            label = {
                                Text(if (player.playingKey == voiceKey) "■ голос" else "● голос")
                            },
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 4.dp, top = 6.dp, bottom = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = date,
                    style = MaterialTheme.typography.labelSmall,
                    color = AvColors.InkSoft,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "изменить",
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onEdit(block) }
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = AvColors.InkSoft,
                )
            }
        }
    }
}

/**
 * Смещение блока при встряхивании: (поворот в градусах, сдвиг по Y в пикселях).
 * Сначала дрожь, потом падение вниз, потом взлёт вверх с перелётом и возврат.
 */
private fun shakeFrame(t: Float, fallPx: Float): Pair<Float, Float> = when {
    t < 0.25f -> {
        val k = t / 0.25f
        Pair(sin(k * 6f * PI.toFloat()) * 9f * (1f - k), 0f)
    }
    t < 0.5f -> {
        val k = (t - 0.25f) / 0.25f
        Pair(0f, fallPx * k * k)
    }
    t < 0.8f -> {
        val k = (t - 0.5f) / 0.3f
        Pair(0f, fallPx * (1f - easeOutBack(k)))
    }
    else -> Pair(0f, 0f)
}

private fun easeOutBack(x: Float): Float {
    val c1 = 1.70158f
    val c3 = c1 + 1f
    val d = x - 1f
    return 1f + c3 * d * d * d + c1 * d * d
}
