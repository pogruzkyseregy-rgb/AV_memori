package com.avmemori.ui.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.avmemori.model.Milestone
import com.avmemori.model.MilestoneKind
import com.avmemori.model.blockPalette
import com.avmemori.ui.theme.AvColors
import com.avmemori.ui.theme.NoteFont
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Добавление события или достижения: название, заметка и дата. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMilestoneDialog(
    kind: MilestoneKind,
    onDismiss: () -> Unit,
    onSave: (Milestone) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var dateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDate by remember { mutableStateOf(false) }

    val dateLabel = remember(dateMillis) {
        SimpleDateFormat("d MMMM yyyy", Locale.forLanguageTag("ru")).format(Date(dateMillis))
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            shape = RoundedCornerShape(20.dp),
            color = AvColors.Paper,
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = if (kind == MilestoneKind.EVENT) "новое событие" else "новое достижение",
                    fontFamily = NoteFont,
                    fontSize = 26.sp,
                    color = AvColors.Ink,
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it.take(60) },
                    label = { Text("название") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(fontFamily = NoteFont, fontSize = 20.sp),
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("пару слов") },
                    modifier = Modifier.fillMaxWidth(),
                )

                Row {
                    FilterChip(
                        selected = true,
                        onClick = { showDate = true },
                        label = { Text(dateLabel) },
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("отмена", color = AvColors.InkSoft)
                    }
                    Button(
                        enabled = title.isNotBlank(),
                        onClick = {
                            val now = System.currentTimeMillis()
                            onSave(
                                Milestone(
                                    id = now,
                                    kind = kind,
                                    title = title.trim(),
                                    note = note.trim(),
                                    dateMillis = dateMillis,
                                    colorArgb = blockPalette[(now % blockPalette.size).toInt()],
                                ),
                            )
                        },
                    ) {
                        Text("сохранить")
                    }
                }
            }
        }
    }

    if (showDate) {
        val state = rememberDatePickerState(initialSelectedDateMillis = dateMillis)
        DatePickerDialog(
            onDismissRequest = { showDate = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { dateMillis = it }
                    showDate = false
                }) {
                    Text("ок")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDate = false }) {
                    Text("отмена", color = AvColors.InkSoft)
                }
            },
        ) {
            DatePicker(state = state)
        }
    }
}
