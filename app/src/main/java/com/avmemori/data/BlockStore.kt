package com.avmemori.data

import android.content.Context
import com.avmemori.model.Block
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/** Сохраняет ленту в JSON во внутреннем хранилище. Позже можно перейти на Room. */
object BlockStore {
    private const val FILE_NAME = "blocks.json"

    fun load(context: Context): List<Block> {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) return emptyList()
        return try {
            val array = JSONArray(file.readText())
            List(array.length()) { i -> array.getJSONObject(i).toBlock() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun save(context: Context, blocks: List<Block>) {
        val array = JSONArray()
        blocks.forEach { array.put(it.toJson()) }
        File(context.filesDir, FILE_NAME).writeText(array.toString())
    }

    private fun Block.toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("note", note)
        put("createdAt", createdAt)
        put("colorArgb", colorArgb)
        put("photos", JSONArray(photos))
        musicPath?.let { put("musicPath", it) }
        voicePath?.let { put("voicePath", it) }
    }

    private fun JSONObject.toBlock(): Block {
        val photos = when {
            has("photos") -> {
                val arr = getJSONArray("photos")
                List(arr.length()) { arr.getString(it) }
            }
            // Миграция со старой версии, где было одно фото.
            optStringOrNull("photoPath") != null -> listOf(optStringOrNull("photoPath")!!)
            else -> emptyList()
        }
        return Block(
            id = getLong("id"),
            note = optString("note", ""),
            createdAt = getLong("createdAt"),
            colorArgb = getLong("colorArgb"),
            photos = photos,
            musicPath = optStringOrNull("musicPath"),
            voicePath = optStringOrNull("voicePath"),
        )
    }

    private fun JSONObject.optStringOrNull(key: String): String? =
        if (has(key) && !isNull(key)) getString(key) else null
}
