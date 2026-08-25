package com.example.kinetic

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool

/**
 * Plays real audio files for the WelcomeScreen:
 * - welcome_bg.mp3: ambient background (full 8s)
 * - sfx_whoosh.mp3: letter reveal SFX
 * - sfx_bassdrop.mp3: DNA bass drop
 * - sfx_reveal.mp3: card reveal chime
 */
class WelcomeSoundPlayer(context: Context, private val enabled: Boolean = true) {

    private var bgPlayer: MediaPlayer? = null
    private var sfxPool: SoundPool? = null
    private var whooshId = 0
    private var bassId = 0
    private var revealId = 0

    init {
        if (enabled) {
            try {
                // Background ambient player
                bgPlayer = MediaPlayer.create(context, R.raw.welcome_bg)?.apply {
                    setVolume(0.35f, 0.35f)
                    isLooping = false
                }

                // SFX pool for short sounds
                val attrs = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                sfxPool = SoundPool.Builder()
                    .setMaxStreams(4)
                    .setAudioAttributes(attrs)
                    .build()

                whooshId = sfxPool!!.load(context, R.raw.sfx_whoosh, 1)
                bassId = sfxPool!!.load(context, R.raw.sfx_bassdrop, 1)
                revealId = sfxPool!!.load(context, R.raw.sfx_reveal, 1)
            } catch (_: Exception) {}
        }
    }

    /** Start the ambient background music — call once when WelcomeScreen appears */
    fun startBackground() {
        if (!enabled) return
        try { bgPlayer?.start() } catch (_: Exception) {}
    }

    /** Play whoosh SFX — call on each KINETIC letter reveal */
    fun playWhoosh() {
        if (!enabled) return
        try {
            sfxPool?.play(whooshId, 0.7f, 0.7f, 1, 0, 1f)
        } catch (_: Exception) {}
    }

    /** Play bass drop — call when DNA helix appears */
    fun playBassDrop() {
        if (!enabled) return
        try {
            sfxPool?.play(bassId, 0.9f, 0.9f, 1, 0, 1f)
        } catch (_: Exception) {}
    }

    /** Play card reveal chime — call when welcome card appears */
    fun playCardReveal() {
        if (!enabled) return
        try {
            sfxPool?.play(revealId, 0.8f, 0.8f, 1, 0, 1f)
        } catch (_: Exception) {}
    }

    /** Gradually fade out the background over [durationMs] milliseconds, then call [onDone] */
    fun fadeOut(durationMs: Long = 2000, onDone: (() -> Unit)? = null) {
        val player = bgPlayer ?: run { onDone?.invoke(); return }
        val steps = 60
        val stepDelay = durationMs / steps
        val startVol = 0.35f
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        for (i in 1..steps) {
            val factor = 1f - (i.toFloat() / steps)
            val vol = startVol * factor * factor * factor  // cubic ease-out — smoother tail
            handler.postDelayed({
                try {
                    player.setVolume(vol, vol)
                    if (i == steps) {
                        // silence before release to avoid click
                        player.setVolume(0f, 0f)
                        onDone?.invoke()
                    }
                } catch (_: Exception) {}
            }, i * stepDelay)
        }
    }

    /** Stop background and release everything — call when WelcomeScreen finishes */
    fun release() {
        try {
            bgPlayer?.stop()
            bgPlayer?.release()
        } catch (_: Exception) {}
        bgPlayer = null
        try { sfxPool?.release() } catch (_: Exception) {}
        sfxPool = null
    }
}
