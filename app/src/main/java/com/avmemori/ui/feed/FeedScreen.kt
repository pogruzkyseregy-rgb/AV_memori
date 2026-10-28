package com.avmemori.ui.feed

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.avmemori.model.Block
import com.avmemori.model.BlockKind
import com.avmemori.ui.theme.AvColors
import com.avmemori.ui.theme.NoteFont
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FeedScreen(blocks: List<Block>, modifier: Modifier = Modifier) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        modifier = modifier
            .fillMaxSize()
            .background(AvColors.Paper),
        contentPadding = PaddingValues(16.dp),
        verticalItemSpacing = 16.dp,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        itemsIndexed(blocks, key = { _, block -> block.id }) { index, block ->
            BlockCard(block = block, index = index)
        }
    }
}

@Composable
fun BlockCard(block: Block, index: Int) {
    // «Порядок в беспорядке»: наклон и высота фиксируются по id,
    // поэтому при прокрутке карточки не прыгают.
    val tilt = remember(block.id) { ((block.id * 37L) % 7L).toFloat() - 3f }
    val photoHeight = remember(block.id) { (((block.id * 13L) % 3L) + 2L).toInt() * 40 }

    val appear = remember(block.id) { Animatable(0f) }
    LaunchedEffect(block.id) {
        delay(index * 60L)
        appear.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = 0.75f,
                stiffness = Spring.StiffnessLow,
            ),
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                val p = appear.value
                rotationZ = tilt * p
                alpha = p
                translationY = (1f - p) * 24.dp.toPx()
            },
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        shadowElevation = 3.dp,
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Место под фото. Позже здесь будет реальный снимок.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(photoHeight.dp)
                    .background(Color(block.colorArgb), RoundedCornerShape(4.dp)),
            ) {
                // Метка типа блока: фото / музыка / голос / заметка
                Text(
                    text = kindLabel(block.kind),
                    modifier = Modifier.padding(6.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = AvColors.InkSoft,
                )
            }

            Text(
                text = block.note,
                modifier = Modifier.padding(top = 8.dp, start = 4.dp, end = 4.dp),
                fontFamily = NoteFont,
                fontStyle = FontStyle.Normal,
                fontSize = 15.sp,
                color = AvColors.Ink,
            )

            Text(
                text = block.date,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                color = AvColors.InkSoft,
            )
        }
    }
}

private fun kindLabel(kind: BlockKind): String = when (kind) {
    BlockKind.PHOTO -> "фото"
    BlockKind.PHOTO_MUSIC -> "♪ фото с музыкой"
    BlockKind.VOICE -> "● голосовое"
    BlockKind.NOTE -> "заметка"
}
