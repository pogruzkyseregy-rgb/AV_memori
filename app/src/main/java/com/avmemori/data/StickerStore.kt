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
                },
            )
        }
        File(context.filesDir, FILE_NAME).writeText(array.toString())
    }
}
