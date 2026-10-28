package com.avmemori.ui.add

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.core.content.ContextCompat
import com.avmemori.audio.VoiceRecorder
import com.avmemori.data.MediaStorage
import com.avmemori.model.Block
import com.avmemori.model.blockPalette
import com.avmemori.ui.theme.AvColors
import com.avmemori.ui.theme.NoteFont
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Создание момента: фото, музыка, голосовое и подпись в любом сочетании.
 * Фото и музыка копируются во внутреннее хранилище сразу после выбора.
 */
@Composable
fun AddBlockDialog(
    onDismiss: () -> Unit,
    onSave: (Block) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val recorder = remember { VoiceRecorder(context) }

    var note by remember { mutableStateOf("") }
    var photoPath by remember { mutableStateOf<String?>(null) }
    var musicPath by remember { mutableStateOf<String?>(null) }
    var voicePath by remember { mutableStateOf<String?>(null) }
    var recording by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }

    // Если диалог закрыли во время записи, запись отменяем.
    DisposableEffect(Unit) {
        onDispose { recorder.cancel() }
    }

    // Фото: Photo Picker на Android 13+, на старых версиях откроется системный выбор.
    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            scope.launch {
                busy = true
                val path = withContext(Dispatchers.IO) {
                    MediaStorage.copyFromUri(context, uri, "photo", "jpg")
                }
                if (path != null) photoPath = path
                busy = false
            }
        }
    }

    // Музыка: системный выбор файлов (SAF) — не нужны разрешения на чтение хранилища.
    val musicPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            scope.launch {
                busy = true
                val path = withContext(Dispatchers.IO) {
                    MediaStorage.copyFromUri(context, uri, "music", "audio")
                }
                if (path != null) musicPath = path
                busy = false
            }
        }
    }

    val micPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted && recorder.start()) recording = true
    }

    fun toggleRecording() {
        if (recording) {
            recording = false
            voicePath = recorder.stop() ?: voicePath
        } else {
            val hasMic = ContextCompat.checkSelfPermission(
                context, Manifest.permission.RECORD_AUDIO,
            ) == PackageManager.PERMISSION_GRANTED
            if (hasMic) {
                if (recorder.start()) recording = true
            } else {
                micPermission.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    val canSave = !busy && !recording &&
        (note.isNotBlank() || photoPath != null || musicPath != null || voicePath != null)

    fun save() {
        val now = System.currentTimeMillis()
        onSave(
            Block(
                id = now,
                note = note.trim(),
                createdAt = now,
                colorArgb = blockPalette[(now % blockPalette.size).toInt()],
                photoPath = photoPath,
                musicPath = musicPath,
                voicePath = voicePath,
            ),
        )
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
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = "новый момент",
                    fontFamily = NoteFont,
                    fontSize = 24.sp,
                    color = AvColors.Ink,
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = photoPath != null,
                        onClick = {
                            photoPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                            )
                        },
                        label = { Text(if (photoPath != null) "фото ✓" else "фото") },
                    )
                    FilterChip(
                        selected = musicPath != null,
                        onClick = { musicPicker.launch(arrayOf("audio/*")) },
                        label = { Text(if (musicPath != null) "музыка ✓" else "музыка") },
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = recording || voicePath != null,
                        onClick = { toggleRecording() },
                        label = {
                            Text(
                                when {
                                    recording -> "■ остановить"
                                    voicePath != null -> "голос ✓ (перезаписать)"
                                    else -> "● записать голос"
                                },
                            )
                        },
                    )
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("подпись") },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(fontFamily = NoteFont, fontSize = 16.sp),
                )

                if (busy) {
                    Text(text = "сохраняю файл…", color = AvColors.InkSoft)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("отмена", color = AvColors.InkSoft)
                    }
                    Button(onClick = { save() }, enabled = canSave) {
                        Text("сохранить")
                    }
                }
            }
        }
    }
}
