package com.avmemori.model

/** Режим приложения: для мамы с малышом или для пары. Меняет подписи, но не данные. */
enum class AppMode(
    val title: String,
    val defaultName: String,
    val subtitle: String,
    val nameQuestion: String,
    val namePlaceholder: String,
    val settingsNameLabel: String,
    val eventsTitle: String,
    val achievementsTitle: String,
) {
    KIDS(
        title = "дети",
        defaultName = "наш малыш",
        subtitle = "наши моменты",
        nameQuestion = "как зовут малыша?",
        namePlaceholder = "например, Соня",
        settingsNameLabel = "имя малыша",
        eventsTitle = "события",
        achievementsTitle = "достижения",
    ),
    COUPLE(
        title = "влюблённые",
        defaultName = "мы вдвоём",
        subtitle = "наши моменты вдвоём",
        nameQuestion = "как вас зовут?",
        namePlaceholder = "например, Аня и Адонис",
        settingsNameLabel = "имена пары",
        eventsTitle = "свидания",
        achievementsTitle = "вместе",
    ),
}
