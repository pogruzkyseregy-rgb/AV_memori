package com.avmemori.model

/** Рамка для фото в ленте. Настраивается пользователем. widthDp = 0 — без рамки. */
data class PhotoFrame(
    val colorArgb: Long = 0xFFFFFFFFL,
    val widthDp: Float = 0f,
    val radiusDp: Float = 6f,
    val insetDp: Float = 0f,
    val doubleLine: Boolean = false,
)
