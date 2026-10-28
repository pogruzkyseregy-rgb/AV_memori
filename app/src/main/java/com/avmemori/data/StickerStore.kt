package com.avmemori.data

import android.content.Context
import com.avmemori.model.Sticker
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/** Библиотека стикеров из мастерской. Картинки лежат в filesDir/stickers. */
object StickerStore {
    private const val FILE_NAME = "stickers.json"

    fun stickersDir(context: Context): File = File(context.filesDir, "stickers").apply { mkdirs() }

    /** Встроенные стикеры AV получают фиксированные id, чтобы не пересекаться с пользовательскими. */
    private const val BUILT_IN_BASE_ID = 1_000L

    /**
     * Загружает библиотеку и добавляет встроенный набор AV, если его ещё нет.
     * Картинки копируются из assets один раз в filesDir/stickers.
     */
    fun loadWithBuiltIn(context: Context): List<Sticker> {
        val current = load(context)
        val names = try {
            context.assets.list("stickers")?.sorted().orEmpty()
        } catch (e: Exception) {
            emptyList()
        }
        val existing = current.map { it.id }.toSet()
        val added = names.mapIndexedNotNull { index, asset ->
            val id = BUILT_IN_BASE_ID + index
            if (id in existing) return@mapIndexedNotNull null
            val target = File(stickersDir(context), "av_$asset")
            try {
                if (!target.exists()) {
                    context.assets.open("stickers/$asset").use { input ->
                        target.outputStream().use { input.copyTo(it) }
                    }
                }
                Sticker(id = id, name = "AV ${index + 1}", path = target.absolutePath, builtIn = true)
            } catch (e: Exception) {
                null
            }
        }
        val result = current + added
        if (added.isNotEmpty()) save(context, result)
        return result
    }

    fun load(context: Context): List<Sticker> {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) return emptyList()
        return try {
            val array = JSONArray(file.readText())
            List(array.length()) { i ->
                val o = array.getJSONObject(i)
                Sticker(
                    id = o.getLong("id"),
                    name = o.optString("name", ""),
                    path = o.getString("path"),
                    builtIn = o.optBoolean("builtIn", false),
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun save(context: Context, stickers: List<Sticker>) {
        val array = JSONArray()
        stickers.forEach { s ->
            array.put(
                JSONObject().apply {
                    put("id", s.id)
                    put("name", s.name)
                    put("path", s.path)
                    put("builtIn", s.builtIn)
                },
            )
        }
        File(context.filesDir, FILE_NAME).writeText(array.toString())
    }
}
