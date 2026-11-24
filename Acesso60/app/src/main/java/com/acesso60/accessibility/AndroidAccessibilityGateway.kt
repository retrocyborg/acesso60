package com.acesso60.accessibility

import br.com.acesso60.core.screen.ScreenReadOutcome

/** Adaptador Android do serviço de acessibilidade ativo. */
class AndroidAccessibilityGateway : AccessibilityGateway {
    override fun performBack(): Boolean = Acesso60AccessibilityService.performBack()
    override fun performHome(): Boolean = Acesso60AccessibilityService.performHome()
    override fun readScreen(): ScreenReadOutcome = Acesso60AccessibilityService.readScreen()
}
