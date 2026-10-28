package com.avmemori.data

import android.content.Context
import com.avmemori.model.Milestone
import com.avmemori.model.MilestoneKind
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/** Хранит события и достижения в JSON. */
object MilestoneStore {
    private const val FILE_NAME = "milestones.json"

    fun load(context: Context): List<Milestone> {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) return emptyList()
        return try {
            val array = JSONArray(file.readText())
            List(array.length()) { i -> array.getJSONObject(i).toMilestone() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun save(context: Context, items: List<Milestone>) {
        val array = JSONArray()
        items.forEach { array.put(it.toJson()) }
        File(context.filesDir, FILE_NAME).writeText(array.toString())
    }

    private fun Milestone.toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("kind", kind.name)
        put("title", title)
        put("note", note)
        put("dateMillis", dateMillis)
        put("colorArgb", colorArgb)
        put("media", JSONArray(media.map { it.toJson() }))
    }

    private fun JSONObject.toMilestone(): Milestone = Milestone(
        id = getLong("id"),
        kind = MilestoneKind.valueOf(getString("kind")),
        title = getString("title"),
        note = optString("note", ""),
        dateMillis = getLong("dateMillis"),
        colorArgb = getLong("colorArgb"),
        media = if (has("media")) getJSONArray("media").toMediaList() else emptyList(),
    )
}
