package com.avmemori.data

import android.content.Context
import android.net.Uri
import java.io.File

/**
 * Копирует выбранные файлы (фото, музыка) во внутреннее хранилище приложения.
 * После копирования не нужны ни разрешения на чтение, ни постоянные доступы к Uri.
 */
object MediaStorage {

    fun newFile(context: Context, prefix: String, ext: String): File {
        val dir = File(context.filesDir, "media").apply { mkdirs() }
        return File(dir, "${prefix}_${System.currentTimeMillis()}.$ext")
    }

    /** Возвращает путь к скопированному файлу или null, если не получилось. */
    fun copyFromUri(context: Context, uri: Uri, prefix: String, ext: String): String? {
        val target = newFile(context, prefix, ext)
        return try {
            val input = context.contentResolver.openInputStream(uri) ?: return null
            input.use { src -> target.outputStream().use { dst -> src.copyTo(dst) } }
            target.absolutePath
        } catch (e: Exception) {
            target.delete()
            null
        }
    }
}
