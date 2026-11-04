package com.acesso60.ui.state

/** Situação de permissões/serviço exibida na interface (somente leitura para a UI). */
enum class AccessibilityStatus {
    DISABLED,
    CONNECTING,
    CONNECTED,
    CONNECTION_FAILED,
}

data class PermissionStatus(
    val microphoneGranted: Boolean,
    val accessibilityStatus: AccessibilityStatus,
) {
    constructor(microphoneGranted: Boolean, accessibilityServiceEnabled: Boolean) : this(
        microphoneGranted = microphoneGranted,
        accessibilityStatus = if (accessibilityServiceEnabled) {
            AccessibilityStatus.CONNECTED
        } else {
            AccessibilityStatus.DISABLED
        },
    )

    val accessibilityServiceEnabled: Boolean
        get() = accessibilityStatus == AccessibilityStatus.CONNECTED
}
