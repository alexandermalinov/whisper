package com.example.whisper.data

import android.content.Context
import android.media.MediaRecorder
import android.os.Environment
import timber.log.Timber
import java.io.File
import java.io.IOException


class AudioRecorder(context: Context) {

    var mediaRecorder: MediaRecorder? = null
    private var audioFile: File = File("${context.filesDir}/audioTest.3gp")

    fun startRecording() {
        mediaRecorder = MediaRecorder().apply {
            try {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setAudioEncodingBitRate(16)
                setAudioSamplingRate(44100)
                setOutputFile(audioFile)
                prepare()
                start()
            } catch (e: IOException) {
                Timber.tag("MediaRecorder").e("IOException. Could not prepare MediaRecorder $e")
                return
            }
        }
    }

    fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
            mediaRecorder = null
        }
    }
}