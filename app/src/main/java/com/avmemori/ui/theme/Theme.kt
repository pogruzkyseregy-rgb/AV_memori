package com.avmemori.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle

object AvColors {
    val Paper = Color(0xFFFBF7F2)
    val Ink = Color(0xFF3B3430)
    val InkSoft = Color(0xFF8A8079)
    val Accent = Color(0xFFD9A7A0)
}

// Рукописный шрифт для заметок. Системный Cursive — временно;
// позже подключим свой файл .ttf из res/font.
val NoteFont = FontFamily.Cursive

@Composable
fun AvTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            background = AvColors.Paper,
            surface = Color.White,
            primary = AvColors.Accent,
            onBackground = AvColors.Ink,
            onSurface = AvColors.Ink,
        ),
        content = content,
    )
}
