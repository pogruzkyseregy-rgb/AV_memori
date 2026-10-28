package com.avmemori.ui.shake

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.avmemori.model.Media
import com.avmemori.ui.feed.LocalPhoto
import com.avmemori.ui.media.thumbPathOf
import kotlin.random.Random

/** Один вылетающий снимок: куда летит (доли экрана) и как крутится. */
private data class Flight(val media: Media, val endX: Float, val endY: Float, val spin: Float)

/**
 * Эффект встряхивания: 5–6 случайных снимков из подборки вылетают снизу,
 * кружатся и тают. trigger меняется при каждом встряхивании.
 */
@Composable
fun ShakeConfetti(media: List<Media>, trigger: Int) {
    val flights = remember(trigger, media) {
        media.shuffled().take(Random.nextInt(5, 7)).map {
            Flight(
                media = it,
                endX = Random.nextFloat() * 0.7f + 0.1f,
                endY = Random.nextFloat() * 0.6f + 0.08f,
                spin = Random.nextFloat() * 50f - 25f,
            )
        }
    }
    // 1 — всё улетело, ничего не видно.
    val progress = remember(trigger) { Animatable(1f) }

    LaunchedEffect(trigger) {
        if (trigger > 0) {
            progress.snapTo(0f)
            progress.animateTo(1f, tween(durationMillis = 1800, easing = LinearOutSlowInEasing))
        }
    }

    if (trigger == 0 || flights.isEmpty()) return

    val p = progress.value
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val w = maxWidth.value
        val h = maxHeight.value
        flights.forEachIndexed { i, f ->
            // Каждый снимок стартует чуть позже предыдущего.
            val local = ((p - i * 0.05f) / 0.7f).coerceIn(0f, 1f)
            val startX = w * 0.5f - 60f
            val startY = h - 180f
            val endX = w * f.endX - 60f
            val endY = h * f.endY
            val x = startX + (endX - startX) * local
            val y = startY + (endY - startY) * local

            Box(
                modifier = Modifier
                    .offset(x = x.dp, y = y.dp)
                    .size(120.dp)
                    .graphicsLayer {
                        rotationZ = f.spin * local
                        alpha = (if (local < 0.7f) 1f else (1f - local) / 0.3f).coerceIn(0f, 1f)
                    }
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White)
                    .padding(6.dp),
            ) {
                LocalPhoto(
                    path = thumbPathOf(f.media),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
        }
    }
}
