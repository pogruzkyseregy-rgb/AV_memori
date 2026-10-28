package com.avmemori.data

import android.content.Context
import com.avmemori.model.AppMode
import com.avmemori.model.BackgroundStyle
import com.avmemori.model.BlockStyle
import com.avmemori.model.PhotoFrame

/** Имя малыша и настройки приложения. */
object BabyPrefs {
    private const val PREFS = "av_prefs"
    private const val KEY_NAME = "baby_name"
    private const val KEY_STYLE = "background_style"
    // Старый флаг «анимированный фон вкл/выкл», нужен только для миграции.
    private const val KEY_ANIMATED = "animated_background"
    private const val KEY_MODE = "app_mode"
    private const val KEY_BLOCK_STYLE = "block_style"
    private const val KEY_AUTOPLAY = "autoplay"
    private const val KEY_FRAME_COLOR = "frame_color"
    private const val KEY_FRAME_WIDTH = "frame_width"
    private const val KEY_FRAME_RADIUS = "frame_radius"
    private const val KEY_FRAME_INSET = "frame_inset"
    private const val KEY_FRAME_DOUBLE = "frame_double"

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

    fun getBlockStyle(context: Context): BlockStyle {
        val saved = prefs(context).getString(KEY_BLOCK_STYLE, null)
        return BlockStyle.values().firstOrNull { it.name == saved } ?: BlockStyle.CLASSIC
    }

    fun setBlockStyle(context: Context, style: BlockStyle) {
        prefs(context).edit().putString(KEY_BLOCK_STYLE, style.name).apply()
    }

    fun getAutoplay(context: Context): Boolean = prefs(context).getBoolean(KEY_AUTOPLAY, true)

    fun setAutoplay(context: Context, on: Boolean) {
        prefs(context).edit().putBoolean(KEY_AUTOPLAY, on).apply()
    }

    fun getFrame(context: Context): PhotoFrame {
        val p = prefs(context)
        val d = PhotoFrame()
        return PhotoFrame(
            colorArgb = p.getLong(KEY_FRAME_COLOR, d.colorArgb),
            widthDp = p.getFloat(KEY_FRAME_WIDTH, d.widthDp),
            radiusDp = p.getFloat(KEY_FRAME_RADIUS, d.radiusDp),
            insetDp = p.getFloat(KEY_FRAME_INSET, d.insetDp),
            doubleLine = p.getBoolean(KEY_FRAME_DOUBLE, d.doubleLine),
        )
    }

    fun setFrame(context: Context, frame: PhotoFrame) {
        prefs(context).edit()
            .putLong(KEY_FRAME_COLOR, frame.colorArgb)
            .putFloat(KEY_FRAME_WIDTH, frame.widthDp)
            .putFloat(KEY_FRAME_RADIUS, frame.radiusDp)
            .putFloat(KEY_FRAME_INSET, frame.insetDp)
            .putBoolean(KEY_FRAME_DOUBLE, frame.doubleLine)
            .apply()
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
