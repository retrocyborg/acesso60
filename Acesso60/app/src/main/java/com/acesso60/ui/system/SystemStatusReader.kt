package com.acesso60.ui.system

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.acesso60.accessibility.Acesso60AccessibilityService
import com.acesso60.ui.state.PermissionStatus

/**
 * Apenas LÊ a situação do microfone e do serviço de acessibilidade para a tela mostrar.
 * Não habilita nada e não executa ações. A frente Android pode substituir por algo próprio.
 */
class SystemStatusReader(context: Context) {
    private val appContext = context.applicationContext

    fun read(): PermissionStatus = PermissionStatus(
        microphoneGranted = ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.RECORD_AUDIO,
        ) == PackageManager.PERMISSION_GRANTED,
        accessibilityServiceEnabled = isAccessibilityServiceEnabled(),
    )

    private fun isAccessibilityServiceEnabled(): Boolean {
        val expected = ComponentName(appContext, Acesso60AccessibilityService::class.java)
        val enabled = Settings.Secure.getString(
            appContext.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
        ) ?: return false
        return enabled.split(':').any { ComponentName.unflattenFromString(it) == expected }
    }
}
