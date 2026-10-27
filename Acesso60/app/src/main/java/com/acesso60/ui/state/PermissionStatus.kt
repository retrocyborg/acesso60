package com.acesso60.ui.state

/** Situação de permissões/serviço exibida na interface (somente leitura para a UI). */
data class PermissionStatus(
    val microphoneGranted: Boolean,
    val accessibilityServiceEnabled: Boolean,
)
