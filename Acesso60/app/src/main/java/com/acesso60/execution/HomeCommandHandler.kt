package com.acesso60.execution

import br.com.acesso60.core.executor.CommandHandler
import br.com.acesso60.core.model.Command
import br.com.acesso60.core.model.ExecutionResult
import com.acesso60.accessibility.AccessibilityGateway
import com.acesso60.accessibility.AndroidAccessibilityGateway

/** Handler para navegação HOME via serviço de acessibilidade. */
class HomeCommandHandler(
    private val accessibility: AccessibilityGateway = AndroidAccessibilityGateway()
) : CommandHandler {

    override fun supports(command: Command): Boolean = command == Command.Home

    override fun handle(command: Command): ExecutionResult = when (command) {
        Command.Home -> if (accessibility.performHome()) {
            ExecutionResult.Success("Voltei para a tela inicial.")
        } else {
            ExecutionResult.Unavailable
        }
        else -> ExecutionResult.Unavailable
    }
}
