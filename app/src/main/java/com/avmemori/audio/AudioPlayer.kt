package com.avmemori.audio

import android.media.MediaPlayer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Один плеер на всё приложение: при старте нового трека предыдущий останавливается.
 * playingKey нужен, чтобы кнопки знали, что сейчас играет.
 */
class AudioPlayer {
    private var player: MediaPlayer? = null

    var playingKey: String? by mutableStateOf(null)
        private set

    fun toggle(key: String, path: String) {
        if (playingKey == key) {
            stop()
            return
        }
        stop()
        try {
            val mp = MediaPlayer().also { player = it }
            mp.setDataSource(path)
            mp.setOnCompletionListener { stop() }
            mp.prepare()
            mp.start()
            playingKey = key
        } catch (e: Exception) {
            stop()
        }
    }

    fun stop() {
        player?.let {
            try {
                it.stop()
            } catch (_: Exception) {
            }
            it.release()
        }
        player = null
        playingKey = null
    }
}
