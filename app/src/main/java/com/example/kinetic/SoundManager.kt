package com.example.kinetic

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.VibrationEffect
import android.os.Vibrator
import kotlin.math.max

/**
 * Plays short UI/feedback sounds (PR chime) via SoundPool.
 *
 * Respects the device ringer mode:
 * - Normal  -> plays the sound at low volume
 * - Vibrate -> vibrates briefly instead of playing sound
 * - Silent  -> does nothing
 */
class SoundManager(context: Context) {

    private val audioManager: AudioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val vibrator: Vibrator? =
        if (context.getSystemService(Context.VIBRATOR_SERVICE) is Vibrator) {
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        } else {
            null
        }

    private var prSoundId: Int = 0

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(2)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build().also { pool ->
            pool.setOnLoadCompleteListener { _, _, status ->
                if (status == 0) {
                    prSoundId = pool.load(context, R.raw.pr_success, 1)
                }
            }
            prSoundId = pool.load(context, R.raw.pr_success, 1)
        }

    fun playPrSound() {
        when (audioManager.ringerMode) {
            AudioManager.RINGER_MODE_NORMAL -> {
                if (prSoundId != 0) {
                    soundPool.play(prSoundId, 0.7f, 0.7f, 1, 0, 1f)
                }
            }
            AudioManager.RINGER_MODE_VIBRATE -> {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(90, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(90)
                }
            }
            AudioManager.RINGER_MODE_SILENT -> {
                // Nothing.
            }
        }
    }

    fun release() {
        soundPool.release()
    }
}
