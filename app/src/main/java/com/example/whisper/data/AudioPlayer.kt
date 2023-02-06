package com.example.whisper.data

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import com.example.whisper.utils.common.ZERO
import timber.log.Timber
import java.io.IOException

class AudioPlayer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private var isPaused: Boolean = false

    fun play() {
        Timber.d("PLAY")
        if (isPaused) {
            resume()
        } else {
            start()
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

    private fun start() {
        Timber.d("START")
        mediaPlayer = MediaPlayer().apply {
            try {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setLegacyStreamType(AudioManager.STREAM_VOICE_CALL)
                        .build()
                )
                setOnCompletionListener { onStop() }
                setOnPreparedListener { onPrepare() }
                setDataSource("${context.filesDir}/audioTest.3gp")
                prepare()
                start()
            } catch (e: IOException) {
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