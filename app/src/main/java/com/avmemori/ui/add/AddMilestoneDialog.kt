package com.avmemori.ui.add

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.avmemori.data.MediaStorage
import com.avmemori.model.Media
import com.avmemori.model.Milestone
import com.avmemori.model.MilestoneKind
import com.avmemori.model.blockPalette
import com.avmemori.ui.theme.AvColors
import com.avmemori.ui.theme.NoteFont
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Добавление события или достижения: название, заметка, дата и фото/видео. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMilestoneDialog(
    kind: MilestoneKind,
    initial: Milestone? = null,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)? = null,
    onSave: (Milestone) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf(initial?.title ?: "") }
    var note by remember { mutableStateOf(initial?.note ?: "") }
    var dateMillis by remember { mutableStateOf(initial?.dateMillis ?: System.currentTimeMillis()) }
    var showDate by remember { mutableStateOf(false) }
    var media by remember { mutableStateOf<List<Media>>(initial?.media ?: emptyList()) }

    val dateLabel = remember(dateMillis) {
        SimpleDateFormat("d MMMM yyyy", Locale.forLanguageTag("ru")).format(Date(dateMillis))
    }

    val mediaPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(maxItems = 20),
    ) { uris ->
        if (uris.isNotEmpty()) scope.launch {
            val imported = withContext(Dispatchers.IO) { MediaStorage.importMedia(context, uris) }
            media = media + imported
        }
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
                    text = when {
                        initial != null -> "изменить"
                        kind == MilestoneKind.EVENT -> "новое событие"
                        else -> "новое достижение"
                    },
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

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = true,
                        onClick = { showDate = true },
                        label = { Text(dateLabel) },
                    )
                    FilterChip(
                        selected = media.isNotEmpty(),
                        onClick = {
                            mediaPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo),
                            )
                        },
                        label = {
                            Text(if (media.isEmpty()) "+ фото и видео" else "фото и видео: ${media.size}")
                        },
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    if (onDelete != null) {
                        TextButton(onClick = onDelete) {
                            Text("удалить", color = AvColors.InkSoft)
                        }
                    }
                    TextButton(onClick = onDismiss) {
                        Text("отмена", color = AvColors.InkSoft)
                    }
                    Button(
                        enabled = title.isNotBlank(),
                        onClick = {
                            val now = System.currentTimeMillis()
                            onSave(
                                Milestone(
                                    id = initial?.id ?: now,
                                    kind = kind,
                                    title = title.trim(),
                                    note = note.trim(),
                                    dateMillis = dateMillis,
                                    colorArgb = initial?.colorArgb ?: blockPalette[(now % blockPalette.size).toInt()],
                                    media = media,
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
