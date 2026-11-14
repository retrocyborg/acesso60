package com.acesso60.execution

import android.content.Context
import android.media.AudioManager

/** Adaptador Android de AudioManager para controle de volume. */
class AndroidVolumeController(
    private val context: Context,
) : VolumeController {

    private val audioManager: AudioManager?
        get() = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    override fun raiseVolume(): Boolean = adjust(AudioManager.ADJUST_RAISE)

    override fun lowerVolume(): Boolean = adjust(AudioManager.ADJUST_LOWER)

    private fun adjust(direction: Int): Boolean {
        val am = audioManager ?: return false
        return try {
            am.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                direction,
                AudioManager.FLAG_SHOW_UI,
            )
            true
        } catch (_: Exception) {
            false
        }
    }
}
