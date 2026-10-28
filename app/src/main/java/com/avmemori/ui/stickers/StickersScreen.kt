package com.avmemori.ui.stickers

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.avmemori.model.Sticker
import com.avmemori.sticker.StickerCutter
import com.avmemori.ui.feed.LocalPhoto
import com.avmemori.ui.theme.AvColors
import com.avmemori.ui.theme.BodyFont
import com.avmemori.ui.theme.NoteFont
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Мастерская стикеров: выбираешь картинку, фон убирается сам.
 * Долгое нажатие на стикер — удалить.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StickersScreen(
    stickers: List<Sticker>,
    onAdd: (Sticker) -> Unit,
    onRemove: (Sticker) -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            busy = true
            scope.launch {
                val path = withContext(Dispatchers.IO) { StickerCutter.cutOutFromUri(context, uri) }
                busy = false
                if (path != null) {
                    val now = System.currentTimeMillis()
                    val own = stickers.count { !it.builtIn }
                    onAdd(Sticker(id = now, name = "стикер ${own + 1}", path = path))
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AvColors.Paper)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "← назад",
                modifier = Modifier.clickable(onClick = onBack).padding(4.dp),
                fontFamily = BodyFont,
                fontSize = 14.sp,
                color = AvColors.InkSoft,
            )
            Spacer(modifier = Modifier.weight(1f))
        }
        Text(
            text = "мастерская стикеров",
            fontFamily = NoteFont,
            fontSize = 30.sp,
            color = AvColors.Ink,
        )
        Text(
            text = "выбери картинку — фон уберу сам, выступающие части останутся. Свои стикеры можно удалить долгим нажатием.",
            fontFamily = BodyFont,
            fontSize = 12.sp,
            color = AvColors.InkSoft,
        )

        Button(
            enabled = !busy,
            onClick = {
                picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
        ) {
            Text(if (busy) "вырезаю…" else "добавить стикер")
        }

        val builtIn = stickers.filter { it.builtIn }
        val own = stickers.filter { !it.builtIn }
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) { SectionTitle("AV") }
            items(builtIn, key = { it.id }) { s -> StickerCell(s, onLongClick = null) }

            item(span = { GridItemSpan(maxLineSpan) }) { SectionTitle("свои") }
            if (own.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = "пока пусто ✿",
                        fontFamily = NoteFont,
                        fontSize = 20.sp,
                        color = AvColors.InkSoft,
                    )
                }
            } else {
                items(own, key = { it.id }) { s -> StickerCell(s, onLongClick = { onRemove(s) }) }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        fontFamily = BodyFont,
        fontSize = 13.sp,
        color = AvColors.InkSoft,
        modifier = Modifier.padding(top = 4.dp),
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun StickerCell(sticker: Sticker, onLongClick: (() -> Unit)?) {
    LocalPhoto(
        path = sticker.path,
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .combinedClickable(onClick = {}, onLongClick = onLongClick)
            .padding(8.dp),
    )
}
