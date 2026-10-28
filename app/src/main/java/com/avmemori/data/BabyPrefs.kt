package com.avmemori.data

import android.content.Context

/** Имя малыша и другие личные настройки. */
object BabyPrefs {
    private const val PREFS = "av_prefs"
    private const val KEY_NAME = "baby_name"

    fun getName(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_NAME, "")
            .orEmpty()

    fun setName(context: Context, name: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_NAME, name.trim())
            .apply()
    }
}
