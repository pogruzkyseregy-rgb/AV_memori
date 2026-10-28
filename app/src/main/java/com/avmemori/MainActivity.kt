package com.avmemori

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.avmemori.audio.AudioPlayer
import com.avmemori.data.BabyPrefs
import com.avmemori.data.BlockStore
import com.avmemori.data.MilestoneStore
import com.avmemori.data.StickerStore
import com.avmemori.model.Block
import com.avmemori.model.Milestone
import com.avmemori.model.MilestoneKind
import com.avmemori.sensor.ShakeDetector
import com.avmemori.ui.add.AddMilestoneDialog
import com.avmemori.ui.background.AnimatedBackground
import com.avmemori.ui.common.NameDialog
import com.avmemori.ui.editor.BlockEditor
import com.avmemori.ui.feed.FeedScreen
import com.avmemori.ui.milestones.MilestonesScreen
import com.avmemori.ui.scatter.PhotoScatter
import com.avmemori.ui.scatter.Viewer
import com.avmemori.ui.settings.SettingsScreen
import com.avmemori.ui.shake.ShakeConfetti
import com.avmemori.ui.stickers.StickersScreen
import com.avmemori.ui.theme.AvColors
import com.avmemori.ui.theme.AvTheme
import com.avmemori.ui.theme.BodyFont
import com.avmemori.ui.theme.NoteFont
import com.avmemori.ui.video.VideoPlayer
import com.avmemori.util.Search

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

/** Три слоя главного экрана. */
enum class AvTab(val title: String) {
    FEED("лента"),
    EVENTS("события"),
    ACHIEVEMENTS("достижения"),
}

/** Экраны поверх главного: мастерская стикеров и настройки. */
enum class Screen { MAIN, STICKERS, SETTINGS }

@Composable
fun AvApp() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val player = remember { AudioPlayer() }

    var babyName by remember { mutableStateOf(BabyPrefs.getName(context)) }
    var bgStyle by remember { mutableStateOf(BabyPrefs.getBackgroundStyle(context)) }
    var blocks by remember { mutableStateOf(BlockStore.load(context)) }
    var milestones by remember { mutableStateOf(MilestoneStore.load(context)) }
    var stickers by remember { mutableStateOf(StickerStore.load(context)) }
    val library = remember(stickers) { stickers.associateBy { it.id } }

    var screen by remember { mutableStateOf(Screen.MAIN) }
    var tab by remember { mutableStateOf(AvTab.FEED) }
    var viewer by remember { mutableStateOf<Viewer?>(null) }
    var videoPath by remember { mutableStateOf<String?>(null) }
    var editorOpen by remember { mutableStateOf(false) }
    var editingBlock by remember { mutableStateOf<Block?>(null) }
    var showAddMilestone by remember { mutableStateOf(false) }
    // Имя спрашиваем один раз при первом запуске.
    var showName by remember { mutableStateOf(babyName.isBlank()) }

    // Поиск: строка открывается по кнопке «поиск» в шапке.
    var searchOpen by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }

    // Встряхивание: каждое срабатывание увеличивает счётчик.
    var shakeCount by remember { mutableIntStateOf(0) }
    val canShake = screen == Screen.MAIN && tab == AvTab.FEED && viewer == null &&
        videoPath == null && !editorOpen && !showAddMilestone && !showName
    val canShakeNow by rememberUpdatedState(canShake)
    DisposableEffect(Unit) {
        val detector = ShakeDetector(context) {
            if (canShakeNow) shakeCount++
        }
        detector.start()
        onDispose { detector.stop() }
    }

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

    // «Назад» закрывает верхний слой: редактор, потом просмотр, потом мастерскую/настройки.
    BackHandler(enabled = editorOpen) { editorOpen = false }
    BackHandler(enabled = !editorOpen && screen != Screen.MAIN && viewer == null && videoPath == null) {
        screen = Screen.MAIN
    }

    // Результаты поиска. Пустой запрос — всё.
    val trimmedQuery = query.trim()
    val shownBlocks = remember(blocks, trimmedQuery) {
        if (trimmedQuery.isEmpty()) {
            blocks
        } else {
            blocks.filter { Search.matches(trimmedQuery, listOf(it.note) + Search.dateTexts(it.createdAt)) }
        }
    }
    val shownMilestones = remember(milestones, trimmedQuery) {
        if (trimmedQuery.isEmpty()) {
            milestones
        } else {
            milestones.filter {
                Search.matches(trimmedQuery, listOf(it.title, it.note) + Search.dateTexts(it.dateMillis))
            }
        }
    }
    val allMedia = remember(blocks) { blocks.flatMap { it.media } }

    fun saveBlocks() = BlockStore.save(context, blocks)

    fun deleteEditing() {
        val id = editingBlock?.id ?: return
        blocks = blocks.filter { it.id != id }
        saveBlocks()
        editorOpen = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedBackground(style = bgStyle)

        when (screen) {
            Screen.MAIN -> Column(modifier = Modifier.fillMaxSize()) {
                Header(
                    name = babyName,
                    onNameClick = { showName = true },
                    onSearch = {
                        searchOpen = !searchOpen
                        if (!searchOpen) query = ""
                    },
                    onStickers = { screen = Screen.STICKERS },
                    onSettings = { screen = Screen.SETTINGS },
                )

                if (searchOpen) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it.take(60) },
                        placeholder = { Text("что вспомнить?", fontFamily = BodyFont) },
                        singleLine = true,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp),
                        textStyle = TextStyle(fontFamily = BodyFont, fontSize = 16.sp),
                    )
                }

                when (tab) {
                    AvTab.FEED -> when {
                        blocks.isEmpty() -> EmptyHint(modifier = Modifier.weight(1f))
                        shownBlocks.isEmpty() -> EmptyHint(
                            modifier = Modifier.weight(1f),
                            text = "ничего не нашлось ✿",
                        )
                        else -> FeedScreen(
                            blocks = shownBlocks,
                            library = library,
                            player = player,
                            shakeEvent = shakeCount,
                            onOpenMedia = { block -> viewer = Viewer(block.media, block.id) },
                            onEdit = { block ->
                                editingBlock = block
                                editorOpen = true
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    AvTab.EVENTS -> MilestonesScreen(
                        items = shownMilestones,
                        kind = MilestoneKind.EVENT,
                        onOpenMedia = { m -> viewer = Viewer(m.media, m.id) },
                        modifier = Modifier.weight(1f),
                    )
                    AvTab.ACHIEVEMENTS -> MilestonesScreen(
                        items = shownMilestones,
                        kind = MilestoneKind.ACHIEVEMENT,
                        onOpenMedia = { m -> viewer = Viewer(m.media, m.id) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Screen.STICKERS -> StickersScreen(
                stickers = stickers,
                onAdd = { sticker ->
                    stickers = stickers + sticker
                    StickerStore.save(context, stickers)
                },
                onRemove = { sticker ->
                    stickers = stickers.filter { it.id != sticker.id }
                    StickerStore.save(context, stickers)
                },
                onBack = { screen = Screen.MAIN },
            )

            Screen.SETTINGS -> SettingsScreen(
                babyName = babyName,
                style = bgStyle,
                onRename = { showName = true },
                onStyleChange = { newStyle ->
                    bgStyle = newStyle
                    BabyPrefs.setBackgroundStyle(context, newStyle)
                },
                onBack = { screen = Screen.MAIN },
            )
        }

        if (screen == Screen.MAIN) {
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
                    if (tab == AvTab.FEED) {
                        editingBlock = null
                        editorOpen = true
                    } else {
                        showAddMilestone = true
                    }
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
        }

        // Снимки, вылетающие при встряхивании.
        ShakeConfetti(media = allMedia, trigger = shakeCount)

        // Разлёт фото и видео поверх всего.
        viewer?.let { v ->
            PhotoScatter(
                media = v.media,
                seed = v.seed,
                onClose = { viewer = null },
                onOpenVideo = { videoPath = it.path },
            )
        }

        videoPath?.let { path ->
            VideoPlayer(path = path, onClose = { videoPath = null })
        }

        if (editorOpen) {
            BlockEditor(
                initial = editingBlock,
                library = stickers,
                onSave = { block ->
                    blocks = if (blocks.any { it.id == block.id }) {
                        blocks.map { if (it.id == block.id) block else it }
                    } else {
                        listOf(block) + blocks
                    }
                    saveBlocks()
                    editorOpen = false
                },
                onDelete = if (editingBlock == null) null else ::deleteEditing,
                onDismiss = { editorOpen = false },
            )
        }
    }

    if (showAddMilestone) {
        val kind = if (tab == AvTab.ACHIEVEMENTS) MilestoneKind.ACHIEVEMENT else MilestoneKind.EVENT
        AddMilestoneDialog(
            kind = kind,
            onDismiss = { showAddMilestone = false },
            onSave = { milestone: Milestone ->
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
private fun Header(
    name: String,
    onNameClick: () -> Unit,
    onSearch: () -> Unit,
    onStickers: () -> Unit,
    onSettings: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 20.dp, end = 16.dp, top = 12.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onNameClick),
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
        Pill(label = "поиск", onClick = onSearch)
        Spacer(modifier = Modifier.width(8.dp))
        Pill(label = "стикеры", onClick = onStickers)
        Spacer(modifier = Modifier.width(8.dp))
        Pill(label = "⚙", onClick = onSettings)
    }
}

@Composable
private fun Pill(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.85f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(text = label, fontSize = 13.sp, color = AvColors.Ink)
    }
}

@Composable
private fun EmptyHint(
    modifier: Modifier = Modifier,
    text: String = "пока пусто,\nдобавь первый момент ✿",
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = text,
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
