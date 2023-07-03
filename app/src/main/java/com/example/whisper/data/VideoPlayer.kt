package com.example.whisper.data

import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import com.example.whisper.utils.common.ZERO
import timber.log.Timber
import java.io.IOException

class VideoPlayer {

    private var mediaPlayer: MediaPlayer? = null
    private var isPaused: Boolean = false
    private var filePath: String? = null

    fun play() {
        Timber.d("PLAY")
        if (isPaused) {
            resume()
        } else {
            start(filePath)
        }
    }

    fun pause() {
        Timber.d("PAUSE")

        if (mediaPlayer != null && mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
            isPaused = true
        }
    }

    fun onStop() {
        Timber.d("STOP")
        isPaused = false
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.stop()
                player.release()
            }
        }
    }

    fun getPlayedTime() = mediaPlayer?.currentPosition ?: ZERO

    private fun start(path: String?) {
        path ?: return

        Timber.d("START")

        mediaPlayer = MediaPlayer().apply {
            try {
                filePath = path
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setLegacyStreamType(AudioManager.STREAM_VOICE_CALL)
                        .build()
                )
                setOnCompletionListener { onStop() }
                setOnPreparedListener { onPrepare() }
                setDataSource(path)
                prepare()
                start()
            } catch (e: IOException) {
                filePath = null
                Timber.tag("MediaRecorder").e("IOException. Could not prepare MediaPlayer $e")
            }
        }

        isPaused = false
    }

    private fun resume() {
        Timber.d("RESUME")
        mediaPlayer?.let { player ->
            if (player.isPlaying) return@let
            player.seekTo(player.currentPosition)
            player.start()
        }
    }

    private fun onPrepare() {
        mediaPlayer?.start()
    }
}