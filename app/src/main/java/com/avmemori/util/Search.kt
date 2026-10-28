package com.avmemori.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Поиск по заметкам, названиям и датам. Регистр букв не важен. */
object Search {
    private val ru = Locale.forLanguageTag("ru")

    /** Все варианты написания даты, по которым можно найти момент: «28 окт», «28 октября 2026», «2026». */
    fun dateTexts(millis: Long): List<String> {
        val date = Date(millis)
        return listOf(
            SimpleDateFormat("d MMM", ru).format(date),
            SimpleDateFormat("d MMMM yyyy", ru).format(date),
            SimpleDateFormat("yyyy", Locale.US).format(date),
        )
    }

    fun matches(query: String, texts: List<String>): Boolean {
        val q = query.trim()
        if (q.isEmpty()) return true
        return texts.any { it.contains(q, ignoreCase = true) }
    }
}
