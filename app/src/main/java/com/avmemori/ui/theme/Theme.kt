package com.avmemori.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.avmemori.R

object AvColors {
    val Paper = Color(0xFFFBF7F2)
    val Ink = Color(0xFF3B3430)
    val InkSoft = Color(0xFF8A8079)
    val Accent = Color(0xFFD9A7A0)
}

/** Рукописный шрифт MarckScript: заголовки, заметки, названия. Поддерживает кириллицу. */
val NoteFont = FontFamily(Font(R.font.marck_script))

/** Скруглённый шрифт Comfortaa: интерфейс и основной текст. Поддерживает кириллицу. */
val BodyFont = FontFamily(Font(R.font.comfortaa))

private val AvTypography = Typography().withFont(BodyFont)

private fun Typography.withFont(font: FontFamily): Typography = Typography(
    displayLarge = displayLarge.copy(fontFamily = font),
    displayMedium = displayMedium.copy(fontFamily = font),
    displaySmall = displaySmall.copy(fontFamily = font),
    headlineLarge = headlineLarge.copy(fontFamily = font),
    headlineMedium = headlineMedium.copy(fontFamily = font),
    headlineSmall = headlineSmall.copy(fontFamily = font),
    titleLarge = titleLarge.copy(fontFamily = font),
    titleMedium = titleMedium.copy(fontFamily = font),
    titleSmall = titleSmall.copy(fontFamily = font),
    bodyLarge = bodyLarge.copy(fontFamily = font),
    bodyMedium = bodyMedium.copy(fontFamily = font),
    bodySmall = bodySmall.copy(fontFamily = font),
    labelLarge = labelLarge.copy(fontFamily = font),
    labelMedium = labelMedium.copy(fontFamily = font),
    labelSmall = labelSmall.copy(fontFamily = font),
)

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
        typography = AvTypography,
        content = content,
    )
}
