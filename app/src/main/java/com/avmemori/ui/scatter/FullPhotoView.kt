package com.avmemori.ui.scatter

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.avmemori.model.Media
import com.avmemori.model.PlacedSticker
import com.avmemori.model.Sticker
import com.avmemori.ui.feed.LocalPhoto
import com.avmemori.ui.media.StickerLayer
import com.avmemori.ui.media.StickerTray
import com.avmemori.ui.theme.AvColors
import com.avmemori.ui.theme.BodyFont
import com.avmemori.ui.theme.NoteFont
import com.avmemori.util.Share

/**
 * Фото на весь экран.
 * Лицевая сторона: щипок — увеличить, два пальца — повернуть, двойной тап — вернуть вид.
 * Одиночный тап переворачивает фото: сзади подпись и стикеры только для чтения.
 * Редактирование — отдельно, кнопками «подписать» и «изменить».
 */
@Composable
fun FullPhotoView(
    media: Media,
    library: List<Sticker>,
    onChange: (Media) -> Unit,
    onClose: () -> Unit,
) {
    var flipped by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf(false) }
    val flip by animateFloatAsState(
        targetValue = if (flipped) 180f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "flip",
    )
    // «Назад»: сначала выходим из редактирования, потом переворачиваем, и только потом закрываем.
    BackHandler {
        when {
            editing -> editing = false
            flipped -> flipped = false
            else -> onClose()
        }
    }

    // Фото «раскрывается» поверх остальных: появляется из чуть меньшего размера.
    val appear = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        appear.animateTo(1f, spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow))
    }
    val density = LocalDensity.current.density

    Box(
        modifier = Modifier
            .fillMaxSize()
            // Поверх всех фото россыпи: раскрытое фото закрывает остальные.
            .zIndex(1000f)
            .background(AvColors.Paper)
            .graphicsLayer {
                alpha = appear.value
                val s = 0.85f + 0.15f * appear.value
                scaleX = s
                scaleY = s
                rotationY = flip
                cameraDistance = 12f * density
            },
    ) {
        if (flip < 90f) {
            FrontSide(
                media = media,
                onTap = { flipped = true },
                onSign = {
                    editing = true
                    flipped = true
                },
                onClose = onClose,
            )
        } else if (editing) {
            BackSide(
                media = media,
                library = library,
                onChange = onChange,
                onDone = { editing = false },
                // Обратная сторона повёрнута вместе с контейнером, поэтому разворачиваем её обратно.
                modifier = Modifier.graphicsLayer { rotationY = 180f },
            )
        } else {
            BackView(
                media = media,
                library = library,
                onTap = { flipped = false },
                onEdit = { editing = true },
                modifier = Modifier.graphicsLayer { rotationY = 180f },
            )
        }
    }
}

@Composable
private fun FrontSide(
    media: Media,
    onTap: () -> Unit,
    onSign: () -> Unit,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    var scale by remember { mutableFloatStateOf(1f) }
    var rotation by remember { mutableFloatStateOf(0f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(modifier = Modifier.fillMaxSize()) {
        LocalPhoto(
            path = media.path,
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, rot ->
                        scale = (scale * zoom).coerceIn(1f, 5f)
                        rotation += rot
                        // Сдвиг только когда фото увеличено, иначе оно остаётся по центру.
                        offset = if (scale > 1f) offset + pan else Offset.Zero
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { _ -> onTap() },
                        onDoubleTap = { _ ->
                            scale = 1f
                            rotation = 0f
                            offset = Offset.Zero
                        },
                    )
                }
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

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "тап — перевернуть · двойной тап — вернуть вид",
                fontFamily = BodyFont,
                fontSize = 12.sp,
                color = AvColors.InkSoft,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ActionPill(label = "поделиться") {
                    Share.shareMedia(context, listOf(media), media.caption.takeIf { it.isNotBlank() })
                }
                ActionPill(label = "подписать", onClick = onSign)
            }
        }
    }
}

/** Обратная сторона только для чтения: подпись и стикеры. Тап — вернуть фото. */
@Composable
private fun BackView(
    media: Media,
    library: List<Sticker>,
    onTap: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val libMap = remember(library) { library.associateBy { it.id } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp)
            .clickable(onClick = onTap),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = media.caption.ifBlank { "без подписи" },
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            fontFamily = NoteFont,
            fontSize = 26.sp,
            color = if (media.caption.isBlank()) AvColors.InkSoft else AvColors.Ink,
            textAlign = TextAlign.Center,
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center,
        ) {
            LocalPhoto(
                path = media.path,
                modifier = Modifier.fillMaxSize().alpha(0.2f),
                contentScale = ContentScale.Fit,
            )
            StickerLayer(
                placed = media.stickers,
                library = libMap,
                interactive = false,
            )
        }

        Text(
            text = "тап — вернуть фото",
            fontFamily = BodyFont,
            fontSize = 12.sp,
            color = AvColors.InkSoft,
        )
        ActionPill(label = "изменить", onClick = onEdit)
    }
}

/** Редактирование подписи и стикеров. Открывается кнопками «подписать» и «изменить». */
@Composable
private fun BackSide(
    media: Media,
    library: List<Sticker>,
    onChange: (Media) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selected by remember { mutableStateOf<Int?>(null) }
    val libMap = remember(library) { library.associateBy { it.id } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            ActionPill(label = "готово", onClick = onDone)
            val sel = selected
            if (sel != null && sel < media.stickers.size) {
                ActionPill(label = "убрать стикер") {
                    onChange(media.copy(stickers = media.stickers.filterIndexed { i, _ -> i != sel }))
                    selected = null
                }
            }
        }

        TextField(
            value = media.caption,
            onValueChange = { onChange(media.copy(caption = it)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "подпись к фото…",
                    fontFamily = NoteFont,
                    fontSize = 22.sp,
                    color = AvColors.InkSoft,
                )
            },
            textStyle = TextStyle(fontFamily = NoteFont, fontSize = 22.sp, color = AvColors.Ink),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
            ),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center,
        ) {
            // Фото едва видно как ориентир, стикеры читаются поверх него.
            LocalPhoto(
                path = media.path,
                modifier = Modifier.fillMaxSize().alpha(0.2f),
                contentScale = ContentScale.Fit,
            )
            StickerLayer(
                placed = media.stickers,
                library = libMap,
                interactive = true,
                selectedIndex = selected,
                onSelect = { selected = it },
                onChange = { i, p ->
                    onChange(media.copy(stickers = media.stickers.toMutableList().also { it[i] = p }))
                },
            )
        }

        StickerTray(library = library) { s ->
            onChange(media.copy(stickers = media.stickers + PlacedSticker(stickerId = s.id)))
            selected = media.stickers.size
        }
    }
}

@Composable
private fun ActionPill(label: String, onClick: () -> Unit) {
    Text(
        text = label,
        modifier = Modifier
            .clip(CircleShape)
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        fontFamily = BodyFont,
        fontSize = 14.sp,
        color = AvColors.Ink,
    )
}
