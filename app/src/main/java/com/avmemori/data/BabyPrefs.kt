package com.avmemori.data

import android.content.Context
import com.avmemori.model.AppMode
import com.avmemori.model.BackgroundStyle

/** Имя малыша и настройки приложения. */
object BabyPrefs {
    private const val PREFS = "av_prefs"
    private const val KEY_NAME = "baby_name"
    private const val KEY_STYLE = "background_style"
    // Старый флаг «анимированный фон вкл/выкл», нужен только для миграции.
    private const val KEY_ANIMATED = "animated_background"
    private const val KEY_MODE = "app_mode"

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun getName(context: Context): String = prefs(context).getString(KEY_NAME, "").orEmpty()

    fun setName(context: Context, name: String) {
        prefs(context).edit().putString(KEY_NAME, name.trim()).apply()
    }

    /** Режим не выбран — null (покажем выбор). Старые пользователи с именем получают «дети». */
    fun getMode(context: Context): AppMode? {
        val p = prefs(context)
        val saved = p.getString(KEY_MODE, null)
        if (saved != null) return AppMode.values().firstOrNull { it.name == saved }
        return if (getName(context).isNotBlank()) AppMode.KIDS else null
    }

    fun setMode(context: Context, mode: AppMode) {
        prefs(context).edit().putString(KEY_MODE, mode.name).apply()
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
