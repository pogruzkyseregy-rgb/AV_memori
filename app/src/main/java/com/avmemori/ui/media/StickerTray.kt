package com.avmemori.ui.media

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Arrangement
import com.avmemori.model.Sticker
import com.avmemori.ui.feed.LocalPhoto
import com.avmemori.ui.theme.AvColors
import com.avmemori.ui.theme.BodyFont

/** Лоток стикеров: сначала набор AV, ниже — свои. Тап по стикеру передаёт его в onPick. */
@Composable
fun StickerTray(
    library: List<Sticker>,
    modifier: Modifier = Modifier,
    onPick: (Sticker) -> Unit,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        TraySection(title = "AV", items = library.filter { it.builtIn }, emptyHint = null, onPick = onPick)
        TraySection(
            title = "свои",
            items = library.filter { !it.builtIn },
            emptyHint = "своих стикеров пока нет — сделай их в мастерской (кнопка «стикеры» наверху)",
            onPick = onPick,
        )
    }
}

@Composable
private fun TraySection(
    title: String,
    items: List<Sticker>,
    emptyHint: String?,
    onPick: (Sticker) -> Unit,
) {
    Text(text = title, fontFamily = BodyFont, fontSize = 12.sp, color = AvColors.InkSoft)
    if (items.isEmpty()) {
        if (emptyHint != null) {
            Text(text = emptyHint, fontFamily = BodyFont, fontSize = 12.sp, color = AvColors.InkSoft)
        }
        return
    }
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items.forEach { s ->
            LocalPhoto(
                path = s.path,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White)
                    .clickable { onPick(s) },
            )
        }
    }
}
