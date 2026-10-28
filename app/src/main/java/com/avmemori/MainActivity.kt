package com.avmemori

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.avmemori.audio.AudioPlayer
import com.avmemori.data.BabyPrefs
import com.avmemori.data.BlockStore
import com.avmemori.data.MilestoneStore
import com.avmemori.model.Block
import com.avmemori.model.MilestoneKind
import com.avmemori.ui.add.AddBlockDialog
import com.avmemori.ui.add.AddMilestoneDialog
import com.avmemori.ui.background.AnimatedBackground
import com.avmemori.ui.common.NameDialog
import com.avmemori.ui.feed.FeedScreen
import com.avmemori.ui.milestones.MilestonesScreen
import com.avmemori.ui.scatter.PhotoScatter
import com.avmemori.ui.theme.AvColors
import com.avmemori.ui.theme.AvTheme
import com.avmemori.ui.theme.BodyFont
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

/** Три слоя приложения. */
enum class AvTab(val title: String) {
    FEED("лента"),
    EVENTS("события"),
    ACHIEVEMENTS("достижения"),
}

@Composable
fun AvApp() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val player = remember { AudioPlayer() }

    var babyName by remember { mutableStateOf(BabyPrefs.getName(context)) }
    var blocks by remember { mutableStateOf(BlockStore.load(context)) }
    var milestones by remember { mutableStateOf(MilestoneStore.load(context)) }
    var tab by remember { mutableStateOf(AvTab.FEED) }
    var openedBlock by remember { mutableStateOf<Block?>(null) }
    var showAddBlock by remember { mutableStateOf(false) }
    var showAddMilestone by remember { mutableStateOf(false) }
    // Имя спрашиваем один раз при первом запуске.
    var showName by remember { mutableStateOf(babyName.isBlank()) }

    // Звук останавливается, когда приложение уходит с экрана или закрывается.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) player.stop()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            player.stop()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedBackground()

        Column(modifier = Modifier.fillMaxSize()) {
            Header(name = babyName, onClick = { showName = true })

            when (tab) {
                AvTab.FEED -> if (blocks.isEmpty()) {
                    EmptyHint(modifier = Modifier.weight(1f))
                } else {
                    FeedScreen(
                        blocks = blocks,
                        player = player,
                        onOpenPhotos = { openedBlock = it },
                        modifier = Modifier.weight(1f),
                    )
                }
                AvTab.EVENTS -> MilestonesScreen(
                    items = milestones,
                    kind = MilestoneKind.EVENT,
                    modifier = Modifier.weight(1f),
                )
                AvTab.ACHIEVEMENTS -> MilestonesScreen(
                    items = milestones,
                    kind = MilestoneKind.ACHIEVEMENT,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // Нижняя панель вкладок.
        TabBar(
            selected = tab,
            onSelect = { tab = it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 12.dp),
        )

        FloatingActionButton(
            onClick = {
                if (tab == AvTab.FEED) showAddBlock = true else showAddMilestone = true
            },
            containerColor = AvColors.Accent,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 20.dp, bottom = 84.dp),
        ) {
            Text(text = "+", fontSize = 26.sp)
        }

        // Разлёт фото поверх всего, когда блок открыт.
        openedBlock?.let { block ->
            PhotoScatter(
                photos = block.photos,
                seed = block.id,
                onClose = { openedBlock = null },
            )
        }
    }

    if (showAddBlock) {
        AddBlockDialog(
            onDismiss = { showAddBlock = false },
            onSave = { block ->
                blocks = listOf(block) + blocks
                BlockStore.save(context, blocks)
                showAddBlock = false
            },
        )
    }

    if (showAddMilestone) {
        val kind = if (tab == AvTab.ACHIEVEMENTS) MilestoneKind.ACHIEVEMENT else MilestoneKind.EVENT
        AddMilestoneDialog(
            kind = kind,
            onDismiss = { showAddMilestone = false },
            onSave = { milestone ->
                milestones = listOf(milestone) + milestones
                MilestoneStore.save(context, milestones)
                showAddMilestone = false
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
            fontSize = 34.sp,
            color = AvColors.Ink,
        )
        Text(
            text = "наши моменты",
            fontFamily = BodyFont,
            fontSize = 12.sp,
            color = AvColors.InkSoft,
        )
    }
}

@Composable
private fun EmptyHint(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "пока пусто,\nдобавь первый момент ✿",
            fontFamily = NoteFont,
            fontSize = 22.sp,
            color = AvColors.InkSoft,
        )
    }
}

@Composable
private fun TabBar(
    selected: AvTab,
    onSelect: (AvTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.85f))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        AvTab.values().forEach { tab ->
            val active = tab == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (active) AvColors.Accent.copy(alpha = 0.35f) else Color.Transparent)
                    .clickable { onSelect(tab) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            ) {
                Text(text = tab.title, fontSize = 14.sp, color = AvColors.Ink)
            }
        }
    }
}
