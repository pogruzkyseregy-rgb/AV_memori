package com.avmemori.ui.editor

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.avmemori.audio.VoiceRecorder
import com.avmemori.data.MediaStorage
import com.avmemori.model.Block
import com.avmemori.model.PlacedSticker
import com.avmemori.model.Sticker
import com.avmemori.model.blockPalette
import com.avmemori.ui.feed.LocalPhoto
import com.avmemori.ui.media.MediaArea
import com.avmemori.ui.media.StickerLayer
import com.avmemori.ui.media.StickerTray
import com.avmemori.ui.theme.AvColors
import com.avmemori.ui.theme.BodyFont
import com.avmemori.ui.theme.NoteFont
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Редактор блока на весь экран: фото и видео, музыка, голос, стикеры и подпись.
 * initial = null — новый блок, иначе — изменение существующего (id и дата сохраняются).
 */
@Composable
fun BlockEditor(
    initial: Block?,
    library: List<Sticker>,
    onSave: (Block) -> Unit,
    onDelete: (() -> Unit)?,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val libMap = remember(library) { library.associateBy { it.id } }

    var note by remember { mutableStateOf(initial?.note.orEmpty()) }
    var media by remember { mutableStateOf(initial?.media ?: emptyList()) }
    var stickers by remember { mutableStateOf(initial?.stickers ?: emptyList<PlacedSticker>()) }
    var musicPath by remember { mutableStateOf(initial?.musicPath) }
    var voicePath by remember { mutableStateOf(initial?.voicePath) }
    var selected by remember { mutableStateOf<Int?>(null) }
    var recording by remember { mutableStateOf(false) }

    val recorder = remember { VoiceRecorder(context) }
    DisposableEffect(Unit) {
        onDispose { recorder.cancel() }
    }

    val mediaPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(maxItems = 20),
    ) { uris ->
        if (uris.isNotEmpty()) scope.launch {
            val imported = withContext(Dispatchers.IO) { MediaStorage.importMedia(context, uris) }
            media = media + imported
        }
    }

    val musicPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            val path = withContext(Dispatchers.IO) { MediaStorage.copyFromUri(context, uri, "audio") }
            if (path != null) musicPath = path
        }
    }

    fun startVoice() {
        recording = recorder.start()
    }

    val micPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) startVoice()
    }

    fun toggleVoice() {
        if (recording) {
            recording = false
            recorder.stop()?.let { voicePath = it }
        } else if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            startVoice()
        } else {
            micPermission.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    val hasContent = note.isNotBlank() || media.isNotEmpty() || stickers.isNotEmpty() ||
        musicPath != null || voicePath != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AvColors.Paper)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "← назад",
                modifier = Modifier.clickable(onClick = onDismiss).padding(4.dp),
                fontFamily = BodyFont,
                fontSize = 14.sp,
                color = AvColors.InkSoft,
            )
        }

        Text(
            text = if (initial == null) "новый момент" else "изменить момент",
            fontFamily = NoteFont,
            fontSize = 30.sp,
            color = AvColors.Ink,
        )

        MediaArea(
            media = media,
            colorArgb = initial?.colorArgb ?: blockPalette[0],
            onOpen = null,
            overlay = {
                StickerLayer(
                    placed = stickers,
                    library = libMap,
                    interactive = true,
                    selectedIndex = selected,
                    onSelect = { selected = it },
                    onChange = { i, p ->
                        stickers = stickers.toMutableList().apply { set(i, p) }
                    },
                )
            },
        )
        Text(
            text = "тяни стикер, крути и растягивай",
            fontFamily = BodyFont,
            fontSize = 12.sp,
            color = AvColors.InkSoft,
        )

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = media.isNotEmpty(),
                onClick = {
                    mediaPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
                },
                label = { Text(if (media.isEmpty()) "+ фото и видео" else "+ ещё (${media.size})") },
            )
            FilterChip(
                selected = musicPath != null,
                onClick = { musicPicker.launch(arrayOf("audio/*")) },
                label = { Text(if (musicPath != null) "♪ музыка есть" else "♪ музыка") },
            )
            FilterChip(
                selected = recording || voicePath != null,
                onClick = { toggleVoice() },
                label = {
                    Text(
                        when {
                            recording -> "■ стоп"
                            voicePath != null -> "● голос есть"
                            else -> "● голос"
                        },
                    )
                },
            )
        }

        if (media.isNotEmpty() || musicPath != null || voicePath != null) {
            TextButton(onClick = {
                media = emptyList()
                musicPath = null
                voicePath = null
            }) {
                Text("очистить вложения", color = AvColors.InkSoft)
            }
        }

        Text(
            text = "стикеры",
            fontFamily = BodyFont,
            fontSize = 13.sp,
            color = AvColors.InkSoft,
        )
        StickerTray(library = library) { s ->
            stickers = stickers + PlacedSticker(stickerId = s.id)
            selected = stickers.lastIndex
        }

        val selectedNow = selected
        if (selectedNow != null && selectedNow < stickers.size) {
            TextButton(onClick = {
                stickers = stickers.filterIndexed { i, _ -> i != selectedNow }
                selected = null
            }) {
                Text("убрать выбранный стикер", color = AvColors.InkSoft)
            }
        }

        OutlinedTextField(
            value = note,
            onValueChange = { note = it.take(500) },
            label = { Text("пару слов о моменте") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(fontFamily = NoteFont, fontSize = 20.sp),
        )

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            if (onDelete != null) {
                TextButton(onClick = onDelete) {
                    Text("удалить", color = AvColors.Accent)
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(
                enabled = hasContent && !recording,
                onClick = {
                    val now = System.currentTimeMillis()
                    onSave(
                        Block(
                            id = initial?.id ?: now,
                            note = note.trim(),
                            createdAt = initial?.createdAt ?: now,
                            colorArgb = initial?.colorArgb ?: blockPalette[(now % blockPalette.size).toInt()],
                            media = media,
                            stickers = stickers,
                            musicPath = musicPath,
                            voicePath = voicePath,
                        ),
                    )
                },
            ) {
                Text("сохранить")
            }
        }
    }
}

