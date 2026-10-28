package com.avmemori.model

enum class BlockKind { PHOTO, PHOTO_MUSIC, VOICE, NOTE }

data class Block(
    val id: Long,
    val kind: BlockKind,
    val note: String,
    val date: String,
    val colorArgb: Long,
)

// Временные демо-данные, пока нет базы (Room добавим на следующем шаге).
val sampleBlocks = listOf(
    Block(1, BlockKind.PHOTO, "первая улыбка ☀️", "12 мар", 0xFFF6D5C3),
    Block(2, BlockKind.VOICE, "первое «мама»", "02 апр", 0xFFD9E4F0),
    Block(3, BlockKind.PHOTO_MUSIC, "наша любимая песенка", "21 май", 0xFFE8DAF0),
    Block(4, BlockKind.NOTE, "сегодня он заснул у меня на груди, не двигалась полчаса", "03 июн", 0xFFF3EAD7),
    Block(5, BlockKind.PHOTO, "первые шаги, держится за диван", "18 сен", 0xFFDDEBDD),
    Block(6, BlockKind.PHOTO, "в парке, листья", "07 окт", 0xFFF6E3C7),
    Block(7, BlockKind.VOICE, "смех в ванной", "22 окт", 0xFFD6E8E6),
)
