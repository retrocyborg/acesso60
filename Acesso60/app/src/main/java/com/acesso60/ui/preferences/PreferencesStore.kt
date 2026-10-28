package com.acesso60.ui.preferences

import android.content.Context

/** P1-03: persistência local mínima de preferências NÃO sensíveis. */
interface PreferencesStore {
    fun loadTextScale(): TextScale
    fun saveTextScale(scale: TextScale)
}

class SharedPreferencesStore(context: Context) : PreferencesStore {
    private val prefs = context.applicationContext
        .getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    override fun loadTextScale(): TextScale {
        val saved = prefs.getString(KEY_TEXT_SCALE, null)
        return TextScale.entries.firstOrNull { it.name == saved } ?: TextScale.NORMAL
    }

    override fun saveTextScale(scale: TextScale) {
        prefs.edit().putString(KEY_TEXT_SCALE, scale.name).apply()
    }

    private companion object {
        const val FILE_NAME = "acesso60_ui_prefs"
        const val KEY_TEXT_SCALE = "text_scale"
    }
}
