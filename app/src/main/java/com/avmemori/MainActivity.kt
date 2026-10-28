package com.avmemori

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.avmemori.audio.AudioPlayer
import com.avmemori.data.BabyPrefs
import com.avmemori.data.BlockStore
import com.avmemori.ui.add.AddBlockDialog
import com.avmemori.ui.common.NameDialog
import com.avmemori.ui.feed.FeedScreen
import com.avmemori.ui.theme.AvColors
import com.avmemori.ui.theme.AvTheme
import com.avmemori.ui.theme.NoteFont

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AvTheme {
                AvApp()
            }
        }
    }
}

@Composable
fun AvApp() {
    val context = LocalContext.current
    val player = remember { AudioPlayer() }

    var babyName by remember { mutableStateOf(BabyPrefs.getName(context)) }
    var blocks by remember { mutableStateOf(BlockStore.load(context)) }
    var showAdd by remember { mutableStateOf(false) }
    // Имя спрашиваем один раз при первом запуске.
    var showName by remember { mutableStateOf(babyName.isBlank()) }

    DisposableEffect(Unit) {
        onDispose { player.stop() }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = AvColors.Paper) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                Header(
                    name = babyName,
                    onClick = { showName = true },
                )

                if (blocks.isEmpty()) {
                    EmptyState(modifier = Modifier.weight(1f))
                } else {
                    FeedScreen(
                        blocks = blocks,
                        player = player,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            FloatingActionButton(
                onClick = { showAdd = true },
                containerColor = AvColors.Accent,
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(20.dp),
            ) {
                Text(text = "+", fontSize = 26.sp)
            }
        }
    }

    if (showAdd) {
        AddBlockDialog(
            onDismiss = { showAdd = false },
            onSave = { block ->
                blocks = listOf(block) + blocks
                BlockStore.save(context, blocks)
                showAdd = false
            },
        )
    }

    if (showName) {
        NameDialog(
            current = babyName,
            onSave = { name ->
                babyName = name
                BabyPrefs.setName(context, name)
                showName = false
            },
            onDismiss = { showName = false },
        )
    }
}

@Composable
private fun Header(name: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .clickable(onClick = onClick)
            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 8.dp),
    ) {
        Text(
            text = name.ifBlank { "наш малыш" },
            fontFamily = NoteFont,
            fontSize = 30.sp,
            color = AvColors.Ink,
        )
        Text(
            text = "наши моменты",
            fontSize = 12.sp,
            color = AvColors.InkSoft,
        )
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "пока пусто,\nдобавь первый момент ✿",
            fontFamily = NoteFont,
            fontSize = 20.sp,
            color = AvColors.InkSoft,
        )
    }
}
