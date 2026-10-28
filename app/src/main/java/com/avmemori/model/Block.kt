package com.avmemori.model

/**
 * Один момент в ленте. Все вложения необязательны и могут быть в любом сочетании:
 * фото и видео, стикеры, музыка, голосовое и подпись.
 */
data class Block(
    val id: Long,
    val note: String,
    val createdAt: Long,
    val colorArgb: Long,
    val media: List<Media> = emptyList(),
    val stickers: List<PlacedSticker> = emptyList(),
    val musicPath: String? = null,
    val voicePath: String? = null,
) {
    val isEmpty: Boolean
        get() = note.isBlank() && media.isEmpty() && stickers.isEmpty() &&
            musicPath == null && voicePath == null
}

// Пастельные цвета для «стикеров» и карточек событий.
val blockPalette = listOf(
    0xFFF6D5C3, 0xFFD9E4F0, 0xFFE8DAF0, 0xFFF3EAD7,
    0xFFDDEBDD, 0xFFF6E3C7, 0xFFD6E8E6,
)
