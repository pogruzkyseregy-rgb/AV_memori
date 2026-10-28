package com.avmemori.model

/** Фото или видео, скопированное во внутреннее хранилище приложения. */
data class Media(
    val path: String,
    val isVideo: Boolean = false,
    /** Для видео — кадр-превью (jpg). Для фото не нужен. */
    val thumbPath: String? = null,
)

/** Стикер из мастерской: картинка с прозрачным фоном. */
data class Sticker(
    val id: Long,
    val name: String,
    val path: String,
)

/**
 * Стикер, поставленный на блок.
 * x и y — центр стикера в долях от размера области (от 0 до 1).
 */
data class PlacedSticker(
    val stickerId: Long,
    val x: Float = 0.5f,
    val y: Float = 0.5f,
    val scale: Float = 1f,
    val rotation: Float = 0f,
)
