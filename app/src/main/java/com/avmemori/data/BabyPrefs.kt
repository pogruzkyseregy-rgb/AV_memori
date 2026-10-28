package com.avmemori.data

import android.content.Context
import com.avmemori.model.BackgroundStyle

/** Имя малыша и настройки приложения. */
object BabyPrefs {
    private const val PREFS = "av_prefs"
    private const val KEY_NAME = "baby_name"
    private const val KEY_STYLE = "background_style"
    // Старый флаг «анимированный фон вкл/выкл», нужен только для миграции.
    private const val KEY_ANIMATED = "animated_background"

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun getName(context: Context): String = prefs(context).getString(KEY_NAME, "").orEmpty()

    fun setName(context: Context, name: String) {
        prefs(context).edit().putString(KEY_NAME, name.trim()).apply()
    }

    fun getBackgroundStyle(context: Context): BackgroundStyle {
        val p = prefs(context)
        val saved = p.getString(KEY_STYLE, null)
        if (saved != null) {
            BackgroundStyle.values().firstOrNull { it.name == saved }?.let { return it }
        }
        // Раньше был только вкл/выкл: выключенный фон переносим в «без фона».
        return if (p.getBoolean(KEY_ANIMATED, true)) BackgroundStyle.BLOBS else BackgroundStyle.OFF
    }

    fun setBackgroundStyle(context: Context, style: BackgroundStyle) {
        prefs(context).edit().putString(KEY_STYLE, style.name).apply()
    }
}
