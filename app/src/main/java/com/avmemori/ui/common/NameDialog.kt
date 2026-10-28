package com.avmemori.ui.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.avmemori.ui.theme.AvColors
import com.avmemori.ui.theme.NoteFont

/** Ввод имени малыша. Показывается при первом запуске и по нажатию на заголовок. */
@Composable
fun NameDialog(
    current: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(current) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AvColors.Paper,
        title = {
            Text(text = "как зовут малыша?", fontFamily = NoteFont, fontSize = 22.sp, color = AvColors.Ink)
        },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(30) },
                singleLine = true,
                placeholder = { Text("например, Соня") },
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(name.trim()) }, enabled = name.isNotBlank()) {
                Text("сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("позже", color = AvColors.InkSoft)
            }
        },
    )
}
