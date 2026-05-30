package com.ruchitech.quicklinkcaller.persistence

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import com.ruchitech.quicklinkcaller.R
import java.io.IOException

class AlarmPlayer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private val audioManager: AudioManager by lazy {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
    private var previousVolumeLevel: Int = 0

    fun playAlarm() {
        previousVolumeLevel = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        stopAlarm()
        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
            0
        )

        mediaPlayer = MediaPlayer().apply {
            try {
                val assetFileDescriptor = context.resources.openRawResourceFd(R.raw.alarm)
                setDataSource(
                    assetFileDescriptor.fileDescriptor,
                    assetFileDescriptor.startOffset,
                    assetFileDescriptor.length
                )
                assetFileDescriptor.close()
                prepare()
               // isLooping = true
                start()
                setOnCompletionListener {
                    // When the sound completes, restore the previous volume level
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, previousVolumeLevel, 0)
                    it.release()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun stopAlarm() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, previousVolumeLevel, 0)
    }
}

