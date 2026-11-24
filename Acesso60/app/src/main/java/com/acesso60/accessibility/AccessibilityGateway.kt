package com.acesso60.accessibility

import br.com.acesso60.core.screen.ScreenReadOutcome

/** Contrato de ações de acessibilidade consumido pelo executor. */
interface AccessibilityGateway {
    fun performBack(): Boolean
    fun performHome(): Boolean

    /** Lê o texto da tela ativa já filtrado pela política de privacidade. */
    fun readScreen(): ScreenReadOutcome = ScreenReadOutcome.Unavailable
}
