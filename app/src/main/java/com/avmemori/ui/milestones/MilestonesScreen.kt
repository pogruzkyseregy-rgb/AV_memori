package com.avmemori.ui.milestones

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.avmemori.model.Milestone
import com.avmemori.ui.common.EditIcon
import com.avmemori.ui.common.IconTap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import com.avmemori.model.MilestoneKind
import com.avmemori.ui.media.MediaArea
import com.avmemori.ui.theme.AvColors
import com.avmemori.ui.theme.NoteFont
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Лента событий или достижений (выбирается через kind). */
@Composable
fun MilestonesScreen(
    items: List<Milestone>,
    kind: MilestoneKind,
    onOpenMedia: (Milestone) -> Unit,
    onEdit: (Milestone) -> Unit,
    modifier: Modifier = Modifier,
) {
    val list = remember(items, kind) {
        items.filter { it.kind == kind }.sortedByDescending { it.dateMillis }
    }

    if (list.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = if (kind == MilestoneKind.EVENT) "здесь будут события ✦" else "здесь будут достижения ★",
                fontFamily = NoteFont,
                fontSize = 22.sp,
                color = AvColors.InkSoft,
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 130.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        itemsIndexed(list, key = { _, m -> m.id }) { index, m ->
            MilestoneCard(milestone = m, index = index, onOpenMedia = onOpenMedia, onEdit = onEdit)
        }
    }
}

@Composable
private fun MilestoneCard(
    milestone: Milestone,
    index: Int,
    onOpenMedia: (Milestone) -> Unit,
    onEdit: (Milestone) -> Unit,
) {
    val appear = remember(milestone.id) { Animatable(0f) }
    LaunchedEffect(milestone.id) {
        delay(index.coerceAtMost(8) * 70L)
        appear.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow),
        )
    }

    val date = remember(milestone.dateMillis) {
        SimpleDateFormat("d MMMM yyyy", Locale.forLanguageTag("ru")).format(Date(milestone.dateMillis))
    }
    val glyph = if (milestone.kind == MilestoneKind.EVENT) "✦" else "★"

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                val p = appear.value
                alpha = p
                translationX = (1f - p) * 40.dp.toPx()
            },
        shape = RoundedCornerShape(18.dp),
        color = Color(milestone.colorArgb),
        shadowElevation = 2.dp,
        // Особая рамка: золотистая обводка и тонкая внутренняя линия — для важных моментов.
        border = BorderStroke(2.dp, AvColors.Accent),
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .border(1.dp, AvColors.Accent.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                .padding(12.dp),
        ) {
            if (milestone.media.isNotEmpty()) {
                MediaArea(
                    media = milestone.media,
                    colorArgb = milestone.colorArgb,
                    onOpen = { onOpenMedia(milestone) },
                )
                Spacer(modifier = Modifier.padding(top = 10.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = glyph, fontSize = 22.sp, color = AvColors.Ink)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = milestone.title,
                    fontFamily = NoteFont,
                    fontSize = 26.sp,
                    color = AvColors.Ink,
                )
            }
            if (milestone.note.isNotBlank()) {
                Text(
                    text = milestone.note,
                    modifier = Modifier.padding(top = 6.dp),
                    fontSize = 15.sp,
                    color = AvColors.Ink,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = date,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium,
                    color = AvColors.InkSoft,
                )
                IconTap(description = "изменить", onClick = { onEdit(milestone) }) { EditIcon() }
            }
        }
    }
}
