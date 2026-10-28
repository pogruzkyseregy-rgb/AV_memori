package com.avmemori.ui.frames

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
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
import com.avmemori.model.blockPalette
import com.avmemori.model.PhotoFrame
import com.avmemori.ui.theme.AvColors
import com.avmemori.ui.theme.BodyFont
import com.avmemori.ui.theme.NoteFont
import kotlin.math.roundToInt

/** Конструктор рамки: цвет, толщина, скругление, белое поле и двойная линия. Превью — сверху. */
@Composable
fun FrameDesignerScreen(
    frame: PhotoFrame,
    onChange: (PhotoFrame) -> Unit,
    onBack: () -> Unit,
) {
    val palette: List<Long> = blockPalette + listOf(0xFF3B3430L, 0xFFD4AF37L, 0xFFFFFFFFL)

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
        Text(
            text = "← назад",
            modifier = Modifier.clickable(onClick = onBack).padding(4.dp),
            fontFamily = BodyFont,
            fontSize = 14.sp,
            color = AvColors.InkSoft,
        )
        Text(
            text = "рамка для фото",
            modifier = Modifier.fillMaxWidth(),
            fontFamily = NoteFont,
            fontSize = 30.sp,
            color = AvColors.Ink,
            textAlign = TextAlign.Center,
        )

        FramedMedia(frame = frame) {
            Image(
                painter = painterResource(R.drawable.settings_photo),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(240.dp),
            )
        }

        FrameCard {
            Text(text = "цвет", fontFamily = BodyFont, fontSize = 16.sp, color = AvColors.Ink)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                palette.forEach { c ->
                    Box34(
                        color = c,
                        selected = c == frame.colorArgb,
                        onClick = { onChange(frame.copy(colorArgb = c)) },
                    )
                }
            }
        }

        FrameCard {
            FrameSlider(
                title = "толщина",
                value = frame.widthDp,
                range = 0f..16f,
                onValue = { onChange(frame.copy(widthDp = it)) },
            )
            FrameSlider(
                title = "скругление",
                value = frame.radiusDp,
                range = 0f..32f,
                onValue = { onChange(frame.copy(radiusDp = it)) },
            )
            FrameSlider(
                title = "белое поле",
                value = frame.insetDp,
                range = 0f..20f,
                onValue = { onChange(frame.copy(insetDp = it)) },
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "двойная линия", fontFamily = BodyFont, fontSize = 16.sp, color = AvColors.Ink)
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = frame.doubleLine,
                    onCheckedChange = { onChange(frame.copy(doubleLine = it)) },
                )
            }
        }

        Text(
            text = "сбросить",
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clip(CircleShape)
                .clickable { onChange(PhotoFrame()) }
                .padding(horizontal = 16.dp, vertical = 8.dp),
            fontFamily = BodyFont,
            fontSize = 14.sp,
            color = AvColors.InkSoft,
        )
    }
}

@Composable
private fun FrameCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        content()
    }
}

@Composable
private fun FrameSlider(title: String, value: Float, range: ClosedFloatingPointRange<Float>, onValue: (Float) -> Unit) {
    Text(
        text = "$title: ${value.roundToInt()}",
        fontFamily = BodyFont,
        fontSize = 14.sp,
        color = AvColors.Ink,
    )
    Slider(value = value, onValueChange = onValue, valueRange = range)
}

@Composable
private fun Box34(color: Long, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(Color(color))
            .then(if (selected) Modifier.border(2.dp, AvColors.Ink, CircleShape) else Modifier)
            .clickable(onClick = onClick),
    )
}
