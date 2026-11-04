package com.acesso60.accessibility

/** Adaptador Android do serviço de acessibilidade ativo. */
class AndroidAccessibilityGateway : AccessibilityGateway {
    override fun performBack(): Boolean = Acesso60AccessibilityService.performBack()
}
