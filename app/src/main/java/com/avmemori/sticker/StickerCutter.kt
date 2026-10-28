package com.avmemori.sticker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.avmemori.data.MediaStorage
import com.avmemori.data.StickerStore
import java.io.File
import java.io.FileOutputStream
import java.util.ArrayDeque
import java.util.UUID

/**
 * Автоматическое вырезание стикера из картинки.
 *
 * Фон определяется по углам картинки. Всё, что похоже на фон и связано с краем,
 * становится прозрачным. Объекты, которые выступают за край, не трогаются,
 * потому что они не связаны с фоном по цвету.
 *
 * Работает полностью на устройстве, без интернета и сторонних библиотек.
 */
object StickerCutter {
    private const val MAX_SIDE = 800
    private const val TOLERANCE = 60

    /** Возвращает путь к PNG с прозрачным фоном или null, если не получилось. */
    fun cutOutFromUri(context: Context, uri: Uri): String? {
        val temp = MediaStorage.copyFromUri(context, uri, "img") ?: return null
        return try {
            val source = decodeSampled(temp, MAX_SIDE) ?: return null
            val cut = removeBackground(source)
            val file = File(StickerStore.stickersDir(context), "${UUID.randomUUID()}.png")
            FileOutputStream(file).use { cut.compress(Bitmap.CompressFormat.PNG, 100, it) }
            file.absolutePath
        } catch (e: Exception) {
            null
        } finally {
            File(temp).delete()
        }
    }

    private fun decodeSampled(path: String, maxSide: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        var sample = 1
        while (bounds.outWidth / (sample * 2) >= maxSide || bounds.outHeight / (sample * 2) >= maxSide) {
            sample *= 2
        }
        return BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample })
    }

    private fun removeBackground(src: Bitmap): Bitmap {
        val w = src.width
        val h = src.height
        val pixels = IntArray(w * h)
        src.getPixels(pixels, 0, w, 0, 0, w, h)

        // Если угол уже прозрачный — картинка с вырезанным фоном, ничего не делаем.
        if ((pixels[0] ushr 24) < 250) return src

        val bg = estimateBackground(pixels, w, h)
        val removed = BooleanArray(w * h)
        val queue = ArrayDeque<Int>()

        fun consider(idx: Int) {
            if (!removed[idx] && isSimilar(pixels[idx], bg)) {
                removed[idx] = true
                queue.add(idx)
            }
        }

        for (x in 0 until w) {
            consider(x)
            consider((h - 1) * w + x)
        }
        for (y in 0 until h) {
            consider(y * w)
            consider(y * w + w - 1)
        }

        while (queue.isNotEmpty()) {
            val idx = queue.poll()
            val x = idx % w
            val y = idx / w
            if (x > 0) consider(idx - 1)
            if (x < w - 1) consider(idx + 1)
            if (y > 0) consider(idx - w)
            if (y < h - 1) consider(idx + w)
        }

        var minX = w
        var minY = h
        var maxX = -1
        var maxY = -1
        for (idx in pixels.indices) {
            if (removed[idx]) {
                pixels[idx] = 0
                continue
            }
            val x = idx % w
            val y = idx / w
            if (x < minX) minX = x
            if (x > maxX) maxX = x
            if (y < minY) minY = y
            if (y > maxY) maxY = y
        }
        if (maxX < 0) return src

        val full = Bitmap.createBitmap(pixels, w, h, Bitmap.Config.ARGB_8888)
        val pad = 4
        val left = (minX - pad).coerceAtLeast(0)
        val top = (minY - pad).coerceAtLeast(0)
        val right = (maxX + pad).coerceAtMost(w - 1)
        val bottom = (maxY + pad).coerceAtMost(h - 1)
        return Bitmap.createBitmap(full, left, top, right - left + 1, bottom - top + 1)
    }

    /** Средний цвет по углам картинки — это и есть фон. */
    private fun estimateBackground(pixels: IntArray, w: Int, h: Int): Int {
        val s = minOf(4, w, h)
        var r = 0L
        var g = 0L
        var b = 0L
        var n = 0L
        for (dy in 0 until s) {
            for (dx in 0 until s) {
                val indices = intArrayOf(
                    dy * w + dx,
                    dy * w + (w - 1 - dx),
                    (h - 1 - dy) * w + dx,
                    (h - 1 - dy) * w + (w - 1 - dx),
                )
                for (idx in indices) {
                    val c = pixels[idx]
                    r += (c shr 16) and 0xFF
                    g += (c shr 8) and 0xFF
                    b += c and 0xFF
                    n++
                }
            }
        }
        return ((r / n).toInt() shl 16) or ((g / n).toInt() shl 8) or (b / n).toInt()
    }

    private fun isSimilar(c: Int, bg: Int): Boolean {
        val dr = ((c shr 16) and 0xFF) - ((bg shr 16) and 0xFF)
        val dg = ((c shr 8) and 0xFF) - ((bg shr 8) and 0xFF)
        val db = (c and 0xFF) - (bg and 0xFF)
        return dr * dr + dg * dg + db * db <= TOLERANCE * TOLERANCE
    }
}
