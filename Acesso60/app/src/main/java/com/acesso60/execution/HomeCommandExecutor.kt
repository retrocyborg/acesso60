package com.acesso60.execution

import br.com.acesso60.core.model.Command
import br.com.acesso60.core.model.ExecutionResult
import com.acesso60.accessibility.AccessibilityGateway
import com.acesso60.accessibility.AndroidAccessibilityGateway

/** Executor especializado para navegação HOME via acessibilidade. */
class HomeCommandExecutor(
    private val accessibility: AccessibilityGateway = AndroidAccessibilityGateway(),
) : SpecializedCommandExecutor {
    override fun supports(command: Command): Boolean = command == Command.Home

    override fun execute(command: Command): ExecutionResult = when (command) {
        Command.Home -> if (accessibility.performHome()) {
            ExecutionResult.Success("Voltei para a tela inicial.")
        } else {
            ExecutionResult.Unavailable
        }
        else -> ExecutionResult.Unavailable
    }
}
