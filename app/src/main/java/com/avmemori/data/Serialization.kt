package com.avmemori.data

import com.avmemori.model.Media
import com.avmemori.model.PlacedSticker
import org.json.JSONArray
import org.json.JSONObject

/** Общие функции превращения моделей в JSON и обратно. */

internal fun JSONObject.optStr(key: String): String? =
    if (has(key) && !isNull(key)) getString(key) else null

internal fun Media.toJson(): JSONObject = JSONObject().apply {
    put("path", path)
    put("isVideo", isVideo)
    thumbPath?.let { put("thumb", it) }
    if (caption.isNotEmpty()) put("caption", caption)
    if (stickers.isNotEmpty()) put("stickers", JSONArray(stickers.map { it.toJson() }))
}

internal fun JSONObject.toMedia(): Media = Media(
    path = getString("path"),
    isVideo = optBoolean("isVideo", false),
    thumbPath = optStr("thumb"),
    caption = optStr("caption").orEmpty(),
    stickers = optJSONArray("stickers")?.toPlacedStickerList().orEmpty(),
)

internal fun JSONArray.toMediaList(): List<Media> =
    List(length()) { getJSONObject(it).toMedia() }

internal fun PlacedSticker.toJson(): JSONObject = JSONObject().apply {
    put("stickerId", stickerId)
    put("x", x.toDouble())
    put("y", y.toDouble())
    put("scale", scale.toDouble())
    put("rotation", rotation.toDouble())
}

internal fun JSONObject.toPlacedSticker(): PlacedSticker = PlacedSticker(
    stickerId = getLong("stickerId"),
    x = optDouble("x", 0.5).toFloat(),
    y = optDouble("y", 0.5).toFloat(),
    scale = optDouble("scale", 1.0).toFloat(),
    rotation = optDouble("rotation", 0.0).toFloat(),
)

internal fun JSONArray.toPlacedStickerList(): List<PlacedSticker> =
    List(length()) { getJSONObject(it).toPlacedSticker() }
