package com.avmemori.model

/** Варианты анимированного фона, которые выбирает мама в настройках. */
enum class BackgroundStyle(val title: String) {
    BLOBS("пастель"),
    QUANTUM("квант"),
    FLOWERS("цветочки"),
    CARS("машинки"),
    OFF("без фона"),
}
