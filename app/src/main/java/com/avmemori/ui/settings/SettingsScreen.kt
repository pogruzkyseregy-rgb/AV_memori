package com.avmemori.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.avmemori.ui.theme.AvColors
import com.avmemori.ui.theme.BodyFont
import com.avmemori.ui.theme.NoteFont

/**
 * Настройки: логотип сверху, переключатели и имя, внизу фото с подписью.
 */
@Composable
fun SettingsScreen(
    babyName: String,
    animated: Boolean,
    onRename: () -> Unit,
    onAnimatedChange: (Boolean) -> Unit,
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

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.logo_fox),
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                contentScale = ContentScale.Fit,
            )
            Text(text = "настройки", fontFamily = NoteFont, fontSize = 30.sp, color = AvColors.Ink)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White),
        ) {
            SettingRow(
                title = "имя малыша",
                value = babyName.ifBlank { "не задано" },
                onClick = onRename,
            )
            SettingSwitchRow(
                title = "анимированный фон",
                checked = animated,
                onChange = onAnimatedChange,
            )
        }

        Spacer(modifier = Modifier.size(8.dp))

        Image(
            painter = painterResource(R.drawable.settings_photo),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp)),
            contentScale = ContentScale.FillWidth,
        )
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

@Composable
private fun SettingSwitchRow(title: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = title, fontFamily = BodyFont, fontSize = 16.sp, color = AvColors.Ink)
        Spacer(modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
