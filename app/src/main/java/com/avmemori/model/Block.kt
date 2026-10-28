package com.avmemori.model

/**
 * Один момент в ленте. Все вложения необязательны и могут быть в любом сочетании:
 * фото, музыка, голосовое и подпись.
 * Файлы копируются во внутреннее хранилище приложения, поэтому работают
 * на любой версии Android без лишних разрешений.
 */
data class Block(
    val id: Long,
    val note: String,
    val createdAt: Long,
    val colorArgb: Long,
    val photoPath: String? = null,
    val musicPath: String? = null,
    val voicePath: String? = null,
) {
    val isEmpty: Boolean
        get() = note.isBlank() && photoPath == null && musicPath == null && voicePath == null
}

// Пастельные цвета для «стикеров» без фото.
val blockPalette = listOf(
    0xFFF6D5C3, 0xFFD9E4F0, 0xFFE8DAF0, 0xFFF3EAD7,
    0xFFDDEBDD, 0xFFF6E3C7, 0xFFD6E8E6,
)
