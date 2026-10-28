package com.avmemori.data

import android.content.Context

/** Имя малыша и настройки приложения. */
object BabyPrefs {
    private const val PREFS = "av_prefs"
    private const val KEY_NAME = "baby_name"
    private const val KEY_ANIMATED = "animated_background"

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun getName(context: Context): String = prefs(context).getString(KEY_NAME, "").orEmpty()

    fun setName(context: Context, name: String) {
        prefs(context).edit().putString(KEY_NAME, name.trim()).apply()
    }

    fun isAnimatedBackground(context: Context): Boolean =
        prefs(context).getBoolean(KEY_ANIMATED, true)

    fun setAnimatedBackground(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_ANIMATED, enabled).apply()
    }
}
