package com.avmemori.util

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.avmemori.model.Media
import java.io.File

/** Отправка фото и видео через системное меню «поделиться». */
object Share {
    fun shareMedia(context: Context, media: List<Media>, text: String?) {
        val uris = media
            .map { File(it.path) }
            .filter { it.exists() }
            .map { FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", it) }
        if (uris.isEmpty() && text.isNullOrBlank()) return

        val intent = when {
            uris.isEmpty() -> Intent(Intent.ACTION_SEND).setType("text/plain")
            uris.size == 1 -> Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_STREAM, uris.first())
                type = typeOf(media.first { File(it.path).exists() })
            }
            else -> Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
                type = if (media.all { it.isVideo }) "video/*" else if (media.none { it.isVideo }) "image/*" else "*/*"
            }
        }
        if (uris.isNotEmpty()) {
            // Права на чтение файлов передаём явно, иначе приложение получателя их не увидит.
            intent.clipData = ClipData.newUri(context.contentResolver, "media", uris.first()).apply {
                uris.drop(1).forEach { addItem(ClipData.Item(it)) }
            }
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        if (!text.isNullOrBlank()) intent.putExtra(Intent.EXTRA_TEXT, text)
        context.startActivity(Intent.createChooser(intent, "поделиться"))
    }

    private fun typeOf(m: Media): String = if (m.isVideo) "video/*" else "image/*"
}
