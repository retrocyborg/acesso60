package com.acesso60.ui.system

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.acesso60.ui.state.AccessibilityStatus
import com.acesso60.ui.state.PermissionStatus

/**
 * Apenas LÊ a situação do microfone e do serviço de acessibilidade para a tela mostrar.
 * Não habilita nada e não executa ações. A frente Android pode substituir por algo próprio.
 */
class SystemStatusReader(
    context: Context,
    private val readAccessibilityStatus: () -> AccessibilityStatus,
) {
    private val appContext = context.applicationContext

    fun read(): PermissionStatus = PermissionStatus(
        microphoneGranted = ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.RECORD_AUDIO,
        ) == PackageManager.PERMISSION_GRANTED,
        accessibilityStatus = readAccessibilityStatus(),
    )
}
