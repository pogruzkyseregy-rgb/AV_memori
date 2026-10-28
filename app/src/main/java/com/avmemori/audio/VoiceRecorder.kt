package com.avmemori.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import com.avmemori.data.MediaStorage
import java.io.File

/**
 * Запись голосового в AAC/M4A. Работает на Android 8.0+.
 * Требует разрешение RECORD_AUDIO (запрашивается в экране добавления).
 */
class VoiceRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var outFile: File? = null

    /** true, если запись началась. */
    fun start(): Boolean {
        val file = MediaStorage.newFile(context, "m4a")
        val r = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
        return try {
            r.setAudioSource(MediaRecorder.AudioSource.MIC)
            r.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            r.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            r.setAudioEncodingBitRate(96_000)
            r.setAudioSamplingRate(44_100)
            r.setOutputFile(file.absolutePath)
            r.prepare()
            r.start()
            recorder = r
            outFile = file
            true
        } catch (e: Exception) {
            r.release()
            file.delete()
            false
        }
    }

    /** Останавливает запись и возвращает путь к файлу (или null). */
    fun stop(): String? {
        val r = recorder ?: return null
        recorder = null
        val file = outFile
        return try {
            r.stop()
            r.release()
            file?.absolutePath
        } catch (e: Exception) {
            r.release()
            file?.delete()
            null
        }
    }

    /** Отменяет запись, если она идёт. Уже сохранённые файлы не трогает. */
    fun cancel() {
        val r = recorder ?: return
        recorder = null
        try {
            r.stop()
        } catch (_: Exception) {
        }
        r.release()
        outFile?.delete()
    }
}
