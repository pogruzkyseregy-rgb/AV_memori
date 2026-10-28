package com.avmemori.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.avmemori.R
import com.avmemori.model.AppMode
import com.avmemori.model.BackgroundStyle
import com.avmemori.model.BlockStyle
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.launch
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.awaitPointerEvent
import androidx.compose.ui.input.pointer.awaitPointerEventScope
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin
import com.avmemori.ui.theme.AvColors
import com.avmemori.ui.theme.BodyFont
import com.avmemori.ui.theme.NoteFont

/**
 * Настройки: логотип сверху, имя и выбор фона, внизу фото с подписью.
 */
@Composable
fun SettingsScreen(
    babyName: String,
    style: BackgroundStyle,
    mode: AppMode,
    blockStyle: BlockStyle,
    onRename: () -> Unit,
    onStyleChange: (BackgroundStyle) -> Unit,
    onModeChange: (AppMode) -> Unit,
    onBlockStyleChange: (BlockStyle) -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AvColors.Paper)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "← назад",
                modifier = Modifier.clickable(onClick = onBack).padding(4.dp),
                fontFamily = BodyFont,
                fontSize = 14.sp,
                color = AvColors.InkSoft,
            )
        }

        Text(
            text = "настройки",
            modifier = Modifier.fillMaxWidth(),
            fontFamily = NoteFont,
            fontSize = 30.sp,
            color = AvColors.Ink,
            textAlign = TextAlign.Center,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White),
        ) {
            SettingRow(
                title = mode.settingsNameLabel,
                value = babyName.ifBlank { "не задано" },
                onClick = onRename,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White)
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = "режим", fontFamily = BodyFont, fontSize = 16.sp, color = AvColors.Ink)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppMode.values().forEach { option ->
                    FilterChip(
                        selected = option == mode,
                        onClick = { onModeChange(option) },
                        label = { Text(option.title) },
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White)
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = "оформление блоков", fontFamily = BodyFont, fontSize = 16.sp, color = AvColors.Ink)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                BlockStyle.values().forEach { option ->
                    FilterChip(
                        selected = option == blockStyle,
                        onClick = { onBlockStyleChange(option) },
                        label = { Text(option.title) },
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White)
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = "анимированный фон", fontFamily = BodyFont, fontSize = 16.sp, color = AvColors.Ink)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                BackgroundStyle.values().forEach { option ->
                    FilterChip(
                        selected = option == style,
                        onClick = { onStyleChange(option) },
                        label = { Text(option.title) },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.size(8.dp))

        // Тап или приближение: фото рвётся на горизонтальные полосы, на секунду появляется логотип AV.
        // Жесты не перехватывают движение пальца, поэтому страницу можно листать во время глитча.
        val glitch = remember { Animatable(1f) }
        val scope = rememberCoroutineScope()
        val trigger: () -> Unit = {
            if (glitch.value > 0.99f) {
                scope.launch {
                    glitch.snapTo(0f)
                    glitch.animateTo(1f, tween(durationMillis = 900, easing = LinearEasing))
                }
            }
        }
        val photo = ImageBitmap.imageResource(R.drawable.settings_photo)
        val logo = ImageBitmap.imageResource(R.drawable.av_logo)
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(photo.width.toFloat() / photo.height)
                .clip(RoundedCornerShape(18.dp))
                .pointerInput(Unit) { detectTapGestures(onTap = { trigger() }) }
                .pointerInput(Unit) {
                    // Наблюдаем за вторым пальцем без потребления событий: прокрутка остаётся рабочей.
                    awaitPointerEventScope {
                        var lastSpread = 0f
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            val pressed = event.changes.filter { it.pressed }
                            if (pressed.size >= 2) {
                                val spread = (pressed[0].position - pressed[1].position).getDistance()
                                if (lastSpread > 0f && abs(spread - lastSpread) > 12f) trigger()
                                lastSpread = spread
                            } else {
                                lastSpread = 0f
                            }
                        }
                    }
                },
        ) {
            val t = glitch.value
            val k = 1f - t
            val w = size.width
            val h = size.height
            if (k <= 0.001f) {
                drawImage(image = photo, dstSize = IntSize(w.roundToInt(), h.roundToInt()))
            } else {
                // Полосы смещаются по случайным правилам, которые меняются каждый кадр.
                val frame = (t * 24f).toInt()
                val bands = 9
                val bandH = h / bands
                for (b in 0 until bands) {
                    val shift = glitchHash(b, frame)
                    val dx = if (shift > 0.55f) (glitchHash(b + 50, frame) - 0.5f) * w * 0.14f * k else 0f
                    val top = b * bandH
                    val srcTop = (top / h * photo.height).roundToInt()
                    val srcH = (bandH / h * photo.height).roundToInt().coerceAtLeast(1)
                    drawImage(
                        image = photo,
                        srcOffset = IntOffset(0, srcTop),
                        srcSize = IntSize(photo.width, srcH),
                        dstOffset = IntOffset(dx.roundToInt(), top.roundToInt()),
                        dstSize = IntSize(w.roundToInt(), bandH.roundToInt() + 1),
                    )
                }
            }
            val la = logoAlpha(t)
            if (la > 0f) {
                val lw = w * 0.5f
                val lh = lw * logo.height / logo.width
                drawImage(
                    image = logo,
                    dstOffset = IntOffset(((w - lw) / 2f).roundToInt(), ((h - lh) / 2f).roundToInt()),
                    dstSize = IntSize(lw.roundToInt(), lh.roundToInt()),
                    alpha = la,
                    colorFilter = ColorFilter.tint(AvColors.Ink),
                )
            }
        }
        Text(
            text = "с любовью AV_memori",
            modifier = Modifier.fillMaxWidth(),
            fontFamily = NoteFont,
            fontSize = 22.sp,
            color = AvColors.InkSoft,
            textAlign = TextAlign.Center,
        )
    }
}

/** Псевдослучайное число 0..1 для полос глитча: одинаковые входы дают одинаковый результат. */
private fun glitchHash(a: Int, b: Int): Float {
    val x = sin(a * 12.9898f + b * 78.233f) * 43758.5453f
    return x - floor(x)
}

/** Логотип: появляется на вспышке глитча и гаснет к концу. t = 1 — покой. */
private fun logoAlpha(t: Float): Float = when {
    t < 0.15f -> t / 0.15f
    t < 0.7f -> 1f
    t < 1f -> (1f - t) / 0.3f
    else -> 0f
}

@Composable
private fun SettingRow(title: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = title, fontFamily = BodyFont, fontSize = 16.sp, color = AvColors.Ink)
        Spacer(modifier = Modifier.weight(1f))
        Text(text = value, fontFamily = NoteFont, fontSize = 20.sp, color = AvColors.InkSoft)
    }
}
