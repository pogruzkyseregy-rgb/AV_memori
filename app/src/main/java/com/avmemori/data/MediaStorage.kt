package com.avmemori.data

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.avmemori.model.Media
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * Копирует выбранные файлы (фото, видео, музыка) во внутреннее хранилище приложения.
 * Имена файлов — UUID: раньше использовалось время, и при быстром выборе
 * нескольких фото файлы перезаписывали друг друга.
 */
object MediaStorage {

    fun mediaDir(context: Context): File = File(context.filesDir, "media").apply { mkdirs() }

    fun newFile(context: Context, ext: String): File =
        File(mediaDir(context), "${UUID.randomUUID()}.$ext")

    /** Возвращает путь к скопированному файлу или null, если не получилось. */
    fun copyFromUri(context: Context, uri: Uri, ext: String): String? {
        val target = newFile(context, ext)
        return try {
            val input = context.contentResolver.openInputStream(uri) ?: return null
            input.use { src -> target.outputStream().use { dst -> src.copyTo(dst) } }
            target.absolutePath
        } catch (e: Exception) {
            target.delete()
            null
        }
    }

    /** Импорт фото и видео из системного выбора. Для видео сразу делаем кадр-превью. */
    fun importMedia(context: Context, uris: List<Uri>): List<Media> = uris.mapNotNull { uri ->
        val type = context.contentResolver.getType(uri).orEmpty()
        if (type.startsWith("video")) {
            val path = copyFromUri(context, uri, "mp4") ?: return@mapNotNull null
            Media(path = path, isVideo = true, thumbPath = videoThumb(context, path))
        } else {
            copyFromUri(context, uri, "jpg")?.let { Media(path = it) }
        }
    }

    /** Сохраняет первый кадр видео как jpg. Возвращает путь или null. */
    fun videoThumb(context: Context, videoPath: String): String? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(videoPath)
            val frame: Bitmap = retriever.getFrameAtTime(0) ?: return null
            val file = newFile(context, "jpg")
            FileOutputStream(file).use { frame.compress(Bitmap.CompressFormat.JPEG, 85, it) }
            file.absolutePath
        } catch (e: Exception) {
            null
        } finally {
            retriever.release()
        }
    }
}
