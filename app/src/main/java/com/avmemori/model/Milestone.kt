package com.avmemori.model

enum class MilestoneKind { EVENT, ACHIEVEMENT }

/** Событие или достижение: отдельный слой с датой, названием и заметкой. */
data class Milestone(
    val id: Long,
    val kind: MilestoneKind,
    val title: String,
    val note: String,
    val dateMillis: Long,
    val colorArgb: Long,
)
