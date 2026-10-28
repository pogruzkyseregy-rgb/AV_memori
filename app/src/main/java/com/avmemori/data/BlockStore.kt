package com.avmemori.data

import android.content.Context
import com.avmemori.model.Block
import com.avmemori.model.Media
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
        put("media", JSONArray(media.map { it.toJson() }))
        put("stickers", JSONArray(stickers.map { it.toJson() }))
        musicPath?.let { put("musicPath", it) }
        voicePath?.let { put("voicePath", it) }
    }

    private fun JSONObject.toBlock(): Block {
        val media: List<Media> = when {
            has("media") -> getJSONArray("media").toMediaList()
            // Миграция: раньше фото хранились строками.
            has("photos") -> getJSONArray("photos").let { arr ->
                List(arr.length()) { Media(path = arr.getString(it)) }
            }
            optStr("photoPath") != null -> listOf(Media(path = optStr("photoPath")!!))
            else -> emptyList()
        }
        return Block(
            id = getLong("id"),
            note = optString("note", ""),
            createdAt = getLong("createdAt"),
            colorArgb = getLong("colorArgb"),
            media = media,
            stickers = if (has("stickers")) getJSONArray("stickers").toPlacedStickerList() else emptyList(),
            musicPath = optStr("musicPath"),
            voicePath = optStr("voicePath"),
        )
    }
}
