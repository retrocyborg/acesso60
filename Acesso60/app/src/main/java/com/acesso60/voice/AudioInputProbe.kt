package com.acesso60.voice

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager

/** Leitura mínima da rota de entrada Android, sem capturar ou persistir áudio. */
fun interface AudioInputProbe {
    fun hasInputDevice(): Boolean

    companion object {
        fun from(context: Context): AudioInputProbe {
            val audioManager = context.getSystemService(AudioManager::class.java)
            return AudioInputProbe {
                audioManager?.getDevices(AudioManager.GET_DEVICES_INPUTS)
                    ?.any { it.isSource } == true
            }
        }
    }
}
