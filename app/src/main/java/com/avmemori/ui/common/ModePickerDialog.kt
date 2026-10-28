package com.avmemori.ui.common

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import com.avmemori.model.AppMode
import com.avmemori.ui.theme.AvColors
import com.avmemori.ui.theme.BodyFont
import com.avmemori.ui.theme.NoteFont

/** Выбор режима при первом запуске: дети или влюблённые. Закрыть без выбора нельзя. */
@Composable
fun ModePickerDialog(onPick: (AppMode) -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        containerColor = AvColors.Paper,
        title = {
            Text(text = "кто будет вести дневник?", fontFamily = NoteFont, fontSize = 22.sp, color = AvColors.Ink)
        },
        text = {
            Text(
                text = "от этого зависят подписи в приложении. Позже можно поменять в настройках.",
                fontFamily = BodyFont,
                fontSize = 13.sp,
                color = AvColors.InkSoft,
            )
        },
        confirmButton = {
            TextButton(onClick = { onPick(AppMode.COUPLE) }) { Text("влюблённые") }
        },
        dismissButton = {
            TextButton(onClick = { onPick(AppMode.KIDS) }) { Text("дети") }
        },
    )
}
