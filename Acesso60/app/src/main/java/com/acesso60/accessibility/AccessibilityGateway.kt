package com.acesso60.accessibility

/** Contrato de ações de acessibilidade consumido pelo executor. */
interface AccessibilityGateway {
    fun performBack(): Boolean
}
