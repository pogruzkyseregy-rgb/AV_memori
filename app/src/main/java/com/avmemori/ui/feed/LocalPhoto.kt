package com.avmemori.ui.feed

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Показывает фото из локального файла. Декодирует уменьшенную копию
 * и учитывает поворот из EXIF, чтобы снимки с телефона не лежали боком.
 */
@Composable
fun LocalPhoto(
    path: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.FillWidth,
) {
    val bitmap by produceState<ImageBitmap?>(initialValue = null, key1 = path) {
        value = withContext(Dispatchers.IO) { decodeScaled(path, 900)?.asImageBitmap() }
    }
    bitmap?.let {
        Image(
            bitmap = it,
            contentDescription = null,
            modifier = modifier,
            contentScale = contentScale,
        )
    }
}

private fun decodeScaled(path: String, maxSide: Int): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(path, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    var sample = 1
    while (bounds.outWidth / (sample * 2) >= maxSide || bounds.outHeight / (sample * 2) >= maxSide) {
        sample *= 2
    }
    val decoded = BitmapFactory.decodeFile(
        path,
        BitmapFactory.Options().apply { inSampleSize = sample },
    ) ?: return null

    val degrees = rotationDegrees(path)
    if (degrees == 0f) return decoded
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
}

private fun rotationDegrees(path: String): Float = try {
    when (ExifInterface(path).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
        else -> 0f
    }
} catch (e: Exception) {
    0f
}
