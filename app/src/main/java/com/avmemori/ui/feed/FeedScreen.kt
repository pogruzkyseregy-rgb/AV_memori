package com.avmemori.ui.feed

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FeedScreen(
    blocks: List<Block>,
    library: Map<Long, Sticker>,
    player: AudioPlayer,
    onOpenMedia: (Block) -> Unit,
    onEdit: (Block) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
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
                onOpenMedia = onOpenMedia,
                onEdit = onEdit,
            )
        }
    }
}

@Composable
fun BlockCard(
    block: Block,
    index: Int,
    library: Map<Long, Sticker>,
    player: AudioPlayer,
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

    val date = remember(block.createdAt) {
        SimpleDateFormat("d MMM", Locale.forLanguageTag("ru")).format(Date(block.createdAt))
    }
    val musicKey = "music-${block.id}"
    val voiceKey = "voice-${block.id}"
    val open: () -> Unit = { onOpenMedia(block) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                val p = appear.value
                rotationZ = tilt * p
                alpha = p
                translationY = (1f - p) * 24.dp.toPx()
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
